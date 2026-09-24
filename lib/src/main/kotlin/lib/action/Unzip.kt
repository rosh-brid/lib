package lib.action

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import lib.R
import java.io.*
import java.util.zip.ZipInputStream

class Unzip(private val kelas: Activity) {

    private var dir: File? = null
    private var target: File? = null
    
    @Volatile
    private var isExtracting = false
    private var process: Process? = null

    private var item: View = LayoutInflater.from(kelas).inflate(R.layout.pop_unzip, null)
    private var pop: PopupWindow

    // View Cache
    private val txtNamaTarget: TextView
    private val txtNamaProses: TextView
    private val txtLokasi: TextView
    private val progressBar: ProgressBar
    private val btnN: TextView
    private val inti: LinearLayout
    private val anak: LinearLayout

    private var selesai: (() -> Unit)? = null

    init {
        // Setup PopupWindow (Full Screen & Focusable)
        pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        
        txtNamaTarget = item.findViewById(R.id.nama_target)
        txtNamaProses = item.findViewById(R.id.nama_proses)
        txtLokasi = item.findViewById(R.id.lokasi)
        progressBar = item.findViewById(R.id.proses)
        btnN = item.findViewById(R.id.n)
        inti = item.findViewById(R.id.inti)
        anak = item.findViewById(R.id.anak)

        inti.setOnClickListener { cancelExtraction() }
        anak.setOnClickListener { } // Mencegah dismiss jika area dalam kotak diklik
        btnN.setOnClickListener { cancelExtraction() }
    }

    fun setDir(terima: File?): Unzip {
        dir = terima
        return this
    }

    fun setFile(terima: File?): Unzip {
        target = terima
        return this
    }

    fun onFinished(action: () -> Unit): Unzip {
        this.selesai = action
        return this
    }

    private fun showPop() {
        if (target == null || !target!!.exists()) {
            Toast.makeText(kelas, "File target tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }
        if (dir == null) dir = kelas.filesDir

        txtNamaTarget.text = target!!.name
        txtLokasi.text = dir!!.absolutePath
        txtNamaProses.text = "Memulai..."
        progressBar.visibility = View.VISIBLE
        btnN.text = "Batal"

        // Menampilkan popup di tengah layar
        pop.showAtLocation(kelas.window.decorView.rootView, Gravity.CENTER, 0, 0)
    }

    // --- ZIP MENGGUNAKAN JAVA BAWAAN (Lebih Stabil, Tidak Butuh Command Shell) ---
    fun startZip() {
        showPop()
        if (target != null && dir != null) {
            Thread { extractZipJava(target!!, dir!!) }.start()
        }
    }

    // --- GZ / XZ MENGGUNAKAN SISTEM COMMAND ---
    fun startGz() {
        showPop()
        if (target != null && dir != null) {
            val cmd = arrayOf("tar", "-xzvf", target!!.absolutePath, "-C", dir!!.absolutePath)
            Thread { extractWithSystem(cmd) }.start()
        }
    }

    fun startXz() {
        showPop()
        if (target != null && dir != null) {
            val cmd = arrayOf("tar", "-xJvf", target!!.absolutePath, "-C", dir!!.absolutePath)
            Thread { extractWithSystem(cmd) }.start()
        }
    }

    // ================== LOGIKA PROSES ==================

    private fun extractWithSystem(command: Array<String>) {
        isExtracting = true
        try {
            val pb = ProcessBuilder(*command)
            pb.redirectErrorStream(true) // Gabungkan output error dan info
            process = pb.start()

            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            var line: String?

            // Membaca baris terminal untuk menampilkan file yang sedang diekstrak di UI
            while (reader.readLine().also { line = it } != null) {
                if (!isExtracting) {
                    process?.destroy()
                    break
                }
                line?.let { updateProgress(it) }
            }

            val exitCode = process?.waitFor()
            if (isExtracting) {
                if (exitCode == 0) finishSuccess()
                else showError("Gagal ekstrak. (Kode Sistem: $exitCode)")
            }
        } catch (e: Exception) {
            showError("Sistem Command Error: ${e.message}")
        }
    }

    private fun extractZipJava(zipFile: File, destDir: File) {
        isExtracting = true
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: java.util.zip.ZipEntry?
                var count = 0
                
                while (zis.nextEntry.also { entry = it } != null) {
                    if (!isExtracting) break
                    
                    val file = File(destDir, entry!!.name)
                    
                    // Keamanan: Proteksi Zip Slip Vulnerability
                    if (!file.canonicalPath.startsWith(destDir.canonicalPath + File.separator)) {
                        continue
                    }

                    // Update UI setiap 3 file saja agar aplikasi tidak lag
                    count++
                    if (count % 3 == 0) updateProgress(entry!!.name)

                    if (entry!!.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            val buffer = ByteArray(8192)
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                if (!isExtracting) break
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                }
            }
            if (isExtracting) finishSuccess()
        } catch (e: Exception) {
            showError("Java Zip Error: ${e.message}")
        }
    }

    // ================== UPDATE UI ==================

    private fun updateProgress(fileName: String) {
        Handler(Looper.getMainLooper()).post {
            // Potong teks jika terlalu panjang agar layout tidak rusak
            val shortName = if (fileName.length > 40) "...${fileName.takeLast(35)}" else fileName
            txtNamaProses.text = shortName
        }
    }

    private fun finishSuccess() {
        Handler(Looper.getMainLooper()).post {
            isExtracting = false
            progressBar.visibility = View.GONE
            txtNamaProses.text = "Ekstrak Selesai!"
            btnN.text = "Tutup"
            btnN.setOnClickListener {
                selesai?.invoke()
                pop.dismiss()
            }
        }
    }

    private fun showError(msg: String?) {
        Handler(Looper.getMainLooper()).post {
            isExtracting = false
            progressBar.visibility = View.GONE
            txtNamaProses.text = "Error: $msg"
            btnN.text = "Tutup"
            btnN.setOnClickListener { pop.dismiss() }
        }
    }

    private fun cancelExtraction() {
        isExtracting = false
        process?.destroy() // Membunuh command terminal (jika ada)
        pop.dismiss()
    }
}
