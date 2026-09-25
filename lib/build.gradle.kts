plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

group = "com.github.rosh-brid"
version = "1.2.6"

android {
    namespace = "lib"
    compileSdk = 36
    ndkVersion = "30.0.16138531"

  defaultConfig {
    minSdk = 24
    
        ndk {
            abiFilters += "arm64-v8a"
        }
        
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
  }
  
  packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
  
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.rosh-brid"
            artifactId = "lib"
            version = "1.2.6"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}