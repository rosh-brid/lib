package lib.view

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import java.io.File

/**
 * Helper untuk menentukan ikon file berdasarkan ekstensi.
 *
 * Mendukung 3 sumber: [File], [Uri], dan [DocumentFile].
 * Untuk file media (gambar/video), thumbnail otomatis di-load
 * ke [ImageView] yang sudah di-set via [setGlide].
 */
class IconFile(private val kelas: Activity) {

    private var tipeImage: ImageView? = null

    /** Set [ImageView] tujuan untuk load thumbnail media. */
    fun setGlide(letak: ImageView?) {
        tipeImage = letak
    }

    // ------------------------------------------------------------
    // Overload — terima File / Uri / DocumentFile / nama mentah
    // ------------------------------------------------------------

    fun Type(terima: File): Int = bind(terima.name, terima)
    fun Type(terima: Uri): Int = bind(terima.lastPathSegment ?: "", terima)
    fun Type(terima: DocumentFile): Int = bind(terima.name ?: "", terima.uri)

    /** Cuma butuh ikon, tanpa load thumbnail. */
    fun Type(nama: String): Int = bind(nama, null)

    // ------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------

    private fun bind(nama: String, source: Any?): Int {
        val ext = nama.substringAfterLast('.', "").lowercase()

        // ViewHolder di RecyclerView di-reuse → wajib clear dulu
        // biar gak ada "ghost image" dari item sebelumnya.
        tipeImage?.let { Glide.with(kelas).clear(it) }

        return when (ext) {
            // --- Kode ---
            "py", "pyc" -> lib.R.drawable.file_python
            "js"        -> lib.R.drawable.file_javascript
            //"c","cpp","h" -> lib.R.drawable.file_cpp
            "kt"        -> lib.R.drawable.file_kotlin
            //"java"      -> lib.R.drawable.file_java
            //"kts","gradle" -> lib.R.drawable.file_gradle
            "html"      -> lib.R.drawable.file_html
            "css"       -> lib.R.drawable.file_css
            //"php"       -> lib.R.drawable.file_php
            //"json"      -> lib.R.drawable.file_json
            "dart"      -> lib.R.drawable.file_flutter
            //"cs"        -> lib.R.drawable.file_c_sharp

            // --- Media (load thumbnail) ---
            "jpg", "png", "webp", "jpeg" -> load(source, lib.R.drawable.image)
            "3gp", "mp4"                 -> load(source, lib.R.drawable.video)
            //"mp3","wav" -> load(source, lib.R.drawable.musik)

            // --- Binary / Arsip ---
            //"apk"       -> lib.R.drawable.android
            //"so","iso"  -> lib.R.drawable.file_binary
            //"xz","gz","zip","tar","rar","pac" -> lib.R.drawable.file_archive

            else -> lib.R.drawable.file
        }
    }

    /**
     * Load thumbnail media via Glide, kembalikan [placeholder]
     * sebagai ikon default. Glide akan menimpa begitu selesai load.
     */
    private fun load(source: Any?, placeholder: Int): Int {
        val iv = tipeImage
        if (iv != null && source != null) {
            Glide.with(kelas)
                .load(source)
                .placeholder(placeholder)
                .error(placeholder)
                .into(iv)
        }
        return placeholder
    }
}