package lib.aksi

import lib.R
import lib.aksi.Klik

import android.widget.*
import android.view.*
import android.app.Activity

import java.io.*

class Download(private val kelas: Activity) {

    private val item: View
    private val pop: PopupWindow
    private val namaLink: TextView
    private val prosesBar: ProgressBar
    private val persen: TextView
    private val ukuran: TextView

    private var lokasiFile: File? = null
    private var namaFile: String? = null
    private var link: String = ""
    @Volatile private var batal = false
    private var threadAktif = false

    init {
        item = LayoutInflater.from(kelas).inflate(R.layout.pop_download, null)
        pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT,
                          ViewGroup.LayoutParams.MATCH_PARENT, true)
        namaLink = item.findViewById(R.id.nama)
        prosesBar = item.findViewById(R.id.proses_bar)
        persen = item.findViewById(R.id.persen)
        ukuran = item.findViewById(R.id.ukuran)

        Klik(item.findViewById<TextView>(R.id.n)).sekali {
            batal = true
            pop.dismiss()
        }
    }

    fun setLink(terima: String?) { terima?.let { link = it } }
    fun setDir(terima: String?) { terima?.let { lokasiFile = File(it) } }  // ✅ fix
    fun setName(terima: String?) { terima?.let { namaFile = it } }

    fun start() {
        if (threadAktif) return          // cegah double-start
        if (link.isBlank()) {
            Toast.makeText(kelas, "Link kosong", Toast.LENGTH_SHORT).show()
            return
        }
        pop.showAtLocation(kelas.window.decorView.rootView, Gravity.CENTER, 0, 0)
        mulaiUnduh()
    }

    private fun mulaiUnduh() {
        threadAktif = true
        batal = false
        namaLink.text = link
        persen.text = "0%"
        ukuran.text = "0 B / —"
        prosesBar.progress = 0

        val dir = lokasiFile
        if (dir != null && !dir.exists()) dir.mkdirs()

        val fileTujuan = File(dir, namaFile ?: "download_file")
        val fileTemp = File(fileTujuan.absolutePath + ".part")  // tulis ke .part dulu

        Thread {
            var koneksi: java.net.HttpURLConnection? = null
            var input: InputStream? = null
            var output: FileOutputStream? = null
            try {
                koneksi = bukaKoneksi(link)   // tangani redirect manual
                val kode = koneksi.responseCode
                if (kode !in 200..299) throw IOException("HTTP $kode")

                val total = koneksi.contentLengthLong.let { if (it < 0) -1L else it }
                input = koneksi.inputStream
                output = FileOutputStream(fileTemp)

                val buffer = ByteArray(8192)
                var terbaca = 0L
                var terakhirUpdate = 0L
                var len: Int

                while (input.read(buffer).also { len = it } > 0) {
                    if (batal) throw IOException("Dibatalkan")  // ✅ cek batal
                    output.write(buffer, 0, len)
                    terbaca += len

                    val now = System.currentTimeMillis()
                    if (now - terakhirUpdate >= 100) {
                        terakhirUpdate = now
                        val p = if (total > 0) ((terbaca * 100) / total).toInt() else 0
                        kelas.runOnUiThread {
                            prosesBar.progress = p
                            persen.text = "$p%"
                            ukuran.text = if (total > 0)
                                "${formatUkuran(terbaca)} / ${formatUkuran(total)}"
                            else formatUkuran(terbaca)
                        }
                    }
                }
                output.flush()

                if (!batal && fileTemp.renameTo(fileTujuan)) {  // ✅ rename atomik
                    kelas.runOnUiThread {
                        persen.text = "100%"
                        Toast.makeText(kelas, "Selesai: ${fileTujuan.name}",
                                       Toast.LENGTH_LONG).show()
                        pop.dismiss()
                    }
                }
            } catch (e: Exception) {
                fileTemp.delete()   // ✅ hapus file parsial
                if (!batal) {
                    kelas.runOnUiThread {
                        if (!kelas.isFinishing) {
                            Toast.makeText(kelas, "Gagal: ${e.message}",
                                           Toast.LENGTH_LONG).show()
                            pop.dismiss()
                        }
                    }
                }
            } finally {
                runCatching { output?.close() }
                runCatching { input?.close() }
                runCatching { koneksi?.disconnect() }
                threadAktif = false
            }
        }.start()
    }

    // Tangani redirect termasuk HTTP→HTTPS (maks 5x)
    private fun bukaKoneksi(urlAwal: String): java.net.HttpURLConnection {
        var url = java.net.URL(urlAwal)
        repeat(5) {
            val k = url.openConnection() as java.net.HttpURLConnection
            k.connectTimeout = 15000
            k.readTimeout = 15000
            k.instanceFollowRedirects = false
            val kode = k.responseCode
            if (kode in 301..308) {
                val lokasi = k.getHeaderField("Location") ?: throw IOException("Redirect tanpa Location")
                k.disconnect()
                url = java.net.URL(url, lokasi)
            } else return k
        }
        throw IOException("Terlalu banyak redirect")
    }

    private fun formatUkuran(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0; val mb = kb / 1024.0; val gb = mb / 1024.0
        return when {
            gb >= 1 -> String.format(java.util.Locale.US, "%.2f GB", gb)
            mb >= 1 -> String.format(java.util.Locale.US, "%.2f MB", mb)
            kb >= 1 -> String.format(java.util.Locale.US, "%.2f KB", kb)
            else -> "$bytes B"
        }
    }
}
