package lib.view

import lib.R

import java.io.File

import android.app.Activity
import android.widget.ImageView

import com.bumptech.glide.Glide

class IconFile(private val kelas:Activity) {

    private var tipeImage : ImageView? = null

    fun Type(terima : File):Int{
        return when(terima.extension.lowercase()){
            "py","pyc" -> lib.R.drawable.file_python
            "js" -> lib.R.drawable.file_javascript
            "c","cpp","h" -> lib.R.drawable.file_cpp
            "kt" -> lib.R.drawable.file_kotlin
            "java" -> lib.R.drawable.file_java
            "kts","gradle" -> lib.R.drawable.file_gradle
            "jpg","png","webp","jpeg" -> {
                muatGlide(terima)
                lib.R.drawable.file_image}
            "3gp","mp4" -> {
                muatGlide(terima)
                lib.R.drawable.file_video}
            "mp3","wav" -> lib.R.drawable.file_music
            "html" -> lib.R.drawable.file_html
            "css" -> lib.R.drawable.file_css
            "php" -> lib.R.drawable.file_php
            "json" -> lib.R.drawable.file_json
            "dart" -> lib.R.drawable.file_flutter
            "cs" -> lib.R.drawable.file_c_sharp
            "apk" -> lib.R.drawable.file_apk
            "txt","properties","pro" -> lib.R.drawable.file_properties
            "so","iso" -> lib.R.drawable.file_binary
            "exe" -> lib.R.drawable.file_windows
            "sh" -> lib.R.drawable.file_terminal
            "doc","docx" -> lib.R.drawable.file_docx
            "pdf" -> lib.R.drawable.file_pdf
            "xz","gz","zip","tar","rar","pac" -> lib.R.drawable.file_zip
            else -> lib.R.drawable.file
        }
    }
    
     fun setGlide(letak:ImageView?){
        tipeImage = letak
    }
    
    private fun muatGlide(terima: File) {

        val gambar = tipeImage ?: return

        Glide.with(kelas)
            .load(terima)
            .into(gambar)
    }
}
