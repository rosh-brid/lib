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

class Delete(private val kelas: Activity) {

    private var targetFiles: List<File> = emptyList()
    private var selesai: (() -> Unit)? = null

    @Volatile
    private var isDeleting = false
    private var lastUpdateTime = 0L // Untuk mencegah lag saat update UI
    private val mainHandler = Handler(Looper.getMainLooper())

    private var item: View = LayoutInflater.from(kelas).inflate(R.layout.pop_delete, null)
    private var pop: PopupWindow

    // View Cache
    private val txtNamaFile: TextView
    private val txtNamaProses: TextView
    private val progressBar: ProgressBar
    private val btnN: TextView
    private val inti: LinearLayout
    private val anak: LinearLayout

    init {
        // Setup PopupWindow (Full Screen & Focusable)
        pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)

        txtNamaFile = item.findViewById(R.id.nama_file)
        txtNamaProses = item.findViewById(R.id.nama_proses)
        progressBar = item.findViewById(R.id.proses)
        btnN = item.findViewById(R.id.n)
        inti = item.findViewById(R.id.inti)
        anak = item.findViewById(R.id.anak)

        inti.setOnClickListener { cancelDeletion() }
        anak.setOnClickListener { } // Mencegah popup tertutup saat area dalam diklik
        btnN.setOnClickListener { cancelDeletion() }
    }

    // Menerima list file atau folder yang akan dihapus
    fun setFile(terima: List<File>): Delete {
        this.targetFiles = terima
        return this
    }

    // Callback saat selesai
    fun onFinished(action: () -> Unit): Delete {
        this.selesai = action
        return this
    }

    fun start() {
        if (targetFiles.isEmpty()) {
            Toast.makeText(kelas, "Tidak ada file yang dipilih untuk dihapus!", Toast.LENGTH_SHORT).show()
            return
        }

        txtNamaFile.text = "Menghapus ${targetFiles.size} item..."
        txtNamaProses.text = "Memulai..."
        progressBar.visibility = View.VISIBLE
        btnN.text = "Batal"

        // Menampilkan popup di tengah layar
        pop.showAtLocation(kelas.window.decorView.rootView, Gravity.CENTER, 0, 0)

        isDeleting = true
        Thread { processDeletion() }.start()
    }

    private fun processDeletion() {
        try {
            var totalDeleted = 0
            for (file in targetFiles) {
                if (!isDeleting) break

                updateMainText("Target: ${file.name}")
                deleteRecursive(file)
                totalDeleted++
            }

            if (isDeleting) {
                finishSuccess(totalDeleted)
            }
        } catch (e: Exception) {
            showError("Gagal: ${e.message}")
        }
    }

    // Fungsi rekursif untuk menghapus folder beserta isinya
    private fun deleteRecursive(fileOrDirectory: File) {
        if (!isDeleting) return

        if (fileOrDirectory.isDirectory) {
            val children = fileOrDirectory.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteRecursive(child)
                }
            }
        }

        // Tampilkan file yang sedang dihapus ke UI
        updateProgress(fileOrDirectory.name)
        
        // Eksekusi penghapusan file / folder kosong
        fileOrDirectory.delete()
    }

    // ================== UPDATE UI ==================

    private fun updateMainText(text: String) {
        mainHandler.post { txtNamaFile.text = text }
    }

    private fun updateProgress(fileName: String) {
        val currentTime = System.currentTimeMillis()
        // THROTTLE: Update UI maksimal 10 kali per detik agar aplikasi tidak LAG!
        if (currentTime - lastUpdateTime > 100) {
            lastUpdateTime = currentTime
            
            mainHandler.post {
                val shortName = if (fileName.length > 40) "...${fileName.takeLast(35)}" else fileName
                txtNamaProses.text = shortName
            }
        }
    }

    private fun finishSuccess(totalDeleted: Int) {
        mainHandler.post {
            isDeleting = false
            progressBar.visibility = View.GONE
            txtNamaFile.text = "Selesai"
            txtNamaProses.text = "$totalDeleted item utama berhasil dihapus."
            btnN.text = "Tutup"
            
            btnN.setOnClickListener {
                selesai?.invoke()
                pop.dismiss()
            }
        }
    }

    private fun showError(msg: String?) {
        mainHandler.post {
            isDeleting = false
            progressBar.visibility = View.GONE
            txtNamaProses.text = msg ?: "Terjadi kesalahan tidak dikenal."
            btnN.text = "Tutup"
            btnN.setOnClickListener { pop.dismiss() }
        }
    }

    private fun cancelDeletion() {
        isDeleting = false
        pop.dismiss()
    }
}
