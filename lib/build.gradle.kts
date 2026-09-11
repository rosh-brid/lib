plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish") // 1. WAJIB DITAMBAHKAN
}

version = "1.0.1"

android {
    namespace = "lib"
    compileSdk = 36
    ndkVersion = "28.2.13676358"

    defaultConfig {
        multiDexEnabled = true
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
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
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
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.annotation:annotation:1.7.0")
}

// 2. SINTAKS KOTLIN DSL YANG BENAR
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                // Opsional: Jitpack biasanya akan menimpa ini,
                // tapi sangat disarankan untuk ditulis agar build lokal juga aman.
                groupId = "com.github.rosh-brid" 
                artifactId = "lib"
                version = "1.0.1"
            }
        }
    }
}
