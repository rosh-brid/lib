package lib.action

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import lib.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class Download(private val activity: Activity) {

    private var dialog: Dialog? = null
    private var link: String? = null
    private var dir: File? = null
    private var fileName: String? = null
    private var selesai: (() -> Unit)? = null 
    
    @Volatile
    private var isDownloading = false
    
    private var btnY: TextView? = null
    private var txtSekarang: TextView? = null
    private var txtTotal: TextView? = null
    private var uiProses: LinearLayout? = null
    private var rootProses: LinearLayout? = null
    private var progressBar: ProgressBar? = null

    fun setLink(url: String): Download {
        this.link = url
        return this
    }

    fun setDir(folder: File): Download {
        this.dir = folder
        return this
    }

    fun setName(name: String): Download {
        this.fileName = name
        return this
    }

    fun onFinished(action: () -> Unit): Download {
        this.selesai = action
        return this
    }

    fun show() {
        if (link.isNullOrBlank()) {
            Toast.makeText(activity, "Need link to start download!", Toast.LENGTH_SHORT).show()
            return
        }

        if (dir == null) dir = activity.filesDir // Default aman

        if (fileName.isNullOrBlank()) {
            fileName = link!!.substringAfterLast('/')
            if (fileName!!.isBlank()) fileName = "downloaded_file"
        }

        dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar).apply {
            setContentView(R.layout.pop_download)
            setCancelable(false)
            window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }

        val inti = dialog!!.findViewById<View>(R.id.inti)
        val anak = dialog!!.findViewById<View>(R.id.anak)
        val txtJudul = dialog!!.findViewById<TextView>(R.id.judul)
        
        btnY = dialog!!.findViewById(R.id.y)
        txtSekarang = dialog!!.findViewById(R.id.sekarang)
        txtTotal = dialog!!.findViewById(R.id.total)
        uiProses = dialog!!.findViewById(R.id.ui_proses)
        rootProses = dialog!!.findViewById(R.id.root_proses)
        progressBar = dialog!!.findViewById(R.id.proses)

        rootProses?.orientation = LinearLayout.HORIZONTAL
        rootProses?.weightSum = 100f 

        txtJudul.text = fileName

        inti.setOnClickListener { cancelDownload() }
        anak.setOnClickListener { } 

        dialog?.show()
        
        startDownloadProcess()
    }

    private fun startDownloadProcess() {
        isDownloading = true
        updateUI(0, 0, 0) 

        Thread {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(link)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                val fileLength = connection.contentLength.toLong()
                val file = File(dir, fileName)

                connection.inputStream.use { input: InputStream ->
                    FileOutputStream(file).use { output ->
                        val data = ByteArray(8192)
                        var totalDownloaded: Long = 0
                        var count: Int
                        var lastProgress = -1

                        while (input.read(data).also { count = it } != -1) {
                            if (!isDownloading) {
                                file.delete() 
                                break
                            }
                            totalDownloaded += count
                            output.write(data, 0, count)

                            val progress = if (fileLength > 0) ((totalDownloaded * 100) / fileLength).toInt() else 0
                            
                            if (progress != lastProgress) {
                                lastProgress = progress
                                updateUI(progress, totalDownloaded, fileLength)
                            }
                        }
                    }
                }

                if (isDownloading) {
                    updateUI(100, fileLength, fileLength) 
                }
            } catch (e: Exception) {
                isDownloading = false
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(activity, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnYToCloseOnly()
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun cancelDownload() {
        isDownloading = false
        dialog?.dismiss()
    }

    private fun updateUI(progress: Int, downloaded: Long, total: Long) {
        Handler(Looper.getMainLooper()).post {
            
            txtSekarang?.text = formatBytes(downloaded)
            txtTotal?.text = if (total > 0) formatBytes(total) else "Unknown"

            val params = uiProses?.layoutParams as? LinearLayout.LayoutParams
            if (params != null) {
                params.width = 0
                params.weight = progress.toFloat() 
                uiProses?.layoutParams = params
            }

            if (progress >= 100 && total > 0) {
                btnY?.text = "Close"
                btnY?.setOnClickListener { 
                    selesai?.invoke() 
                    dialog?.dismiss()
                }
                progressBar?.visibility = View.GONE
            } else {
                btnY?.text = "Cencel"
                btnY?.setOnClickListener { cancelDownload() }
                progressBar?.visibility = View.VISIBLE
            }
        }
    }

    private fun btnYToCloseOnly() {
        btnY?.text = "Close"
        btnY?.setOnClickListener { dialog?.dismiss() }
        progressBar?.visibility = View.GONE
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
