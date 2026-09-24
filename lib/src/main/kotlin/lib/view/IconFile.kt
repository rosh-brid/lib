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
            //"c","cpp","h" -> lib.R.drawable.file_cpp
            "kt" -> lib.R.drawable.file_kotlin
            //"java" -> lib.R.drawable.file_java
            //"kts","gradle" -> lib.R.drawable.file_gradle
            "jpg","png","webp","jpeg" -> {
                muatGlide(terima)
                lib.R.drawable.image}
            "3gp","mp4" -> {
                muatGlide(terima)
                lib.R.drawable.video}
            //"mp3","wav" -> rosh.lib.R.drawable.musik
            "html" -> lib.R.drawable.file_html
            "css" -> lib.R.drawable.file_css
            //"php" -> rosh.lib.R.drawable.file_php
            //"json" -> rosh.lib.R.drawable.file_json
            "dart" -> lib.R.drawable.file_flutter
            //"cs" -> rosh.lib.R.drawable.file_c_sharp
            //"apk" -> lib.R.drawable.android
            //"so","iso" -> rosh.lib.R.drawable.file_binary
            //"xz","gz","zip","tar","rar","pac" -> rosh.lib.R.drawable.file_archive
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
