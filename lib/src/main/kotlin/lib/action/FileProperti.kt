package lib.action

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import lib.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileProperti(private val kelas: Activity) {

    private var target: File? = null

    private val item: View =
        LayoutInflater.from(kelas)
            .inflate(R.layout.pop_properti, null)

    private val pop: PopupWindow

    private val nama: TextView
    private val ukuran: TextView
    private val lokasi: TextView
    private val waktu: TextView
    private val inti: LinearLayout
    private val anak: LinearLayout
    private val close: TextView

    init {

        pop = PopupWindow(
            item,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        nama = item.findViewById(R.id.nama)
        ukuran = item.findViewById(R.id.ukuran)
        lokasi = item.findViewById(R.id.lokasi)
        waktu = item.findViewById(R.id.waktu)

        inti = item.findViewById(R.id.inti)
        anak = item.findViewById(R.id.anak)
        close = item.findViewById(R.id.n)

        // Klik area luar untuk menutup
        inti.setOnClickListener {
            pop.dismiss()
        }

        // Jangan tutup ketika area popup diklik
        anak.setOnClickListener { }

        close.setOnClickListener {
            pop.dismiss()
        }
    }

    fun setFile(terima: File?): FileProperti {
        target = terima
        return this
    }

    fun start() {

        val file = target ?: return

        if (!file.exists()) {
            return
        }

        nama.text = file.name
        ukuran.text = ukuranFile(file)
        lokasi.text = file.absolutePath
        waktu.text = waktuFile(file)

        pop.showAtLocation(
            kelas.window.decorView.rootView,
            Gravity.CENTER,
            0,
            0
        )
    }

    private fun ukuranFile(file: File): String {

        if (file.isDirectory) {
            return "Folder"
        }

        val bytes = file.length()

        if (bytes < 1024) {
            return "$bytes B"
        }

        if (bytes < 1024 * 1024) {
            return "%.2f KB".format(
                Locale.US,
                bytes / 1024.0
            )
        }

        if (bytes < 1024L * 1024L * 1024L) {
            return "%.2f MB".format(
                Locale.US,
                bytes / (1024.0 * 1024.0)
            )
        }

        return "%.2f GB".format(
            Locale.US,
            bytes / (1024.0 * 1024.0 * 1024.0)
        )
    }

    private fun waktuFile(file: File): String {

        return SimpleDateFormat(
            "dd MMMM yyyy, HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date(file.lastModified())
        )
    }
}