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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CopyMove(private val kelas: Activity) {

    private var targetList: List<File> = emptyList()
    private var dir: File? = null
    private var isMove: Boolean = false // false = Salin, true = Pindah
    private var selesai: (() -> Unit)? = null

    @Volatile
    private var isProcessing = false
    private var lastUpdateTime = 0L
    private val mainHandler = Handler(Looper.getMainLooper())

    private var item: View = LayoutInflater.from(kelas).inflate(R.layout.pop_copy_move, null)
    private var pop: PopupWindow

    // View Cache
    private val txtJudul: TextView
    private val txtNamaFile: TextView
    private val txtNamaProses: TextView
    private val progressBar: ProgressBar
    private val btnN: TextView
    private val inti: LinearLayout
    private val anak: LinearLayout

    init {
        pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)

        txtJudul = item.findViewById(R.id.judul_pop)
        txtNamaFile = item.findViewById(R.id.nama_file)
        txtNamaProses = item.findViewById(R.id.nama_proses)
        progressBar = item.findViewById(R.id.proses)
        btnN = item.findViewById(R.id.n)
        inti = item.findViewById(R.id.inti)
        anak = item.findViewById(R.id.anak)

        inti.setOnClickListener { cancelProcess() }
        anak.setOnClickListener { } // Mencegah dismiss area kotak
        btnN.setOnClickListener { cancelProcess() }
    }

    fun setDir(terima: File): CopyMove {
        this.dir = terima
        return this
    }

    fun setList(terima: List<File>): CopyMove {
        this.targetList = terima
        return this
    }

    fun setStatus(terima: Boolean): CopyMove {
        this.isMove = terima
        return this
    }

    fun onFinished(action: () -> Unit): CopyMove {
        this.selesai = action
        return this
    }

    fun start() {
        if (targetList.isEmpty()) {
            Toast.makeText(kelas, "Tidak ada file yang dipilih!", Toast.LENGTH_SHORT).show()
            return
        }
        if (dir == null) {
            Toast.makeText(kelas, "Folder tujuan belum ditentukan!", Toast.LENGTH_SHORT).show()
            return
        }
        // Pastikan folder tujuan ada
        if (!dir!!.exists()) {
            dir!!.mkdirs()
        }

        val modeText = if (isMove) "Memindah" else "Menyalin"
        txtJudul.text = modeText
        txtNamaFile.text = "$modeText ${targetList.size} item..."
        txtNamaProses.text = "Memulai..."
        progressBar.visibility = View.VISIBLE
        btnN.text = "Batal"

        pop.showAtLocation(kelas.window.decorView.rootView, Gravity.CENTER, 0, 0)

        isProcessing = true
        Thread { executeProcess() }.start()
    }

    private fun executeProcess() {
        try {
            var countSuccess = 0

            for (sourceFile in targetList) {
                if (!isProcessing) break

                updateMainText("Memproses: ${sourceFile.name}")
                val destFile = File(dir, sourceFile.name)

                processRecursive(sourceFile, destFile)
                countSuccess++
            }

            if (isProcessing) {
                finishSuccess(countSuccess)
            }
        } catch (e: Exception) {
            showError("Gagal: ${e.message}")
        }
    }

    private fun processRecursive(source: File, dest: File) {
        if (!isProcessing) return

        // TRIK CEPAT: Jika Pindah (Move), coba ganti path saja (hanya berfungsi jika di storage yang sama)
        if (isMove && source.renameTo(dest)) {
            updateProgress(source.name)
            return
        }

        // Jika renameTo gagal (karena beda storage), lakukan Copy/Salin secara manual
        if (source.isDirectory) {
            dest.mkdirs()
            val children = source.listFiles()
            if (children != null) {
                for (child in children) {
                    processRecursive(child, File(dest, child.name))
                }
            }
            // Jika mode Move, hapus folder asal setelah isinya disalin semua
            if (isMove && isProcessing) source.delete()
        } else {
            updateProgress(source.name)
            copyFileStream(source, dest)
            
            // Jika mode Move, hapus file asal setelah selesai disalin
            if (isMove && isProcessing) source.delete()
        }
    }

    // Fungsi salin byte per byte manual agar proses bisa di-batal-kan (Cancelable)
    private fun copyFileStream(source: File, dest: File) {
        dest.parentFile?.mkdirs()
        try {
            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        if (!isProcessing) break // Berhenti instan jika dibatalkan
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Bersihkan file setengah jadi jika user menekan tombol batal
        if (!isProcessing && dest.exists()) dest.delete()
    }

    // ================== UPDATE UI ==================

    private fun updateMainText(text: String) {
        mainHandler.post { txtNamaFile.text = text }
    }

    private fun updateProgress(fileName: String) {
        val currentTime = System.currentTimeMillis()
        // THROTTLE UI: Update 10 kali per detik agar tidak lag
        if (currentTime - lastUpdateTime > 100) {
            lastUpdateTime = currentTime
            mainHandler.post {
                val shortName = if (fileName.length > 40) "...${fileName.takeLast(35)}" else fileName
                txtNamaProses.text = shortName
            }
        }
    }

    private fun finishSuccess(count: Int) {
        mainHandler.post {
            isProcessing = false
            progressBar.visibility = View.GONE
            val status = if (isMove) "dipindah" else "disalin"
            txtNamaFile.text = "Selesai"
            txtNamaProses.text = "$count item utama berhasil $status."
            btnN.text = "Tutup"

            btnN.setOnClickListener {
                selesai?.invoke()
                pop.dismiss()
            }
        }
    }

    private fun showError(msg: String?) {
        mainHandler.post {
            isProcessing = false
            progressBar.visibility = View.GONE
            txtNamaProses.text = msg ?: "Terjadi kesalahan."
            btnN.text = "Tutup"
            btnN.setOnClickListener { pop.dismiss() }
        }
    }

    private fun cancelProcess() {
        isProcessing = false
        pop.dismiss()
    }
}
