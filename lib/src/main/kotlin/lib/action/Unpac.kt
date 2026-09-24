package lib.action

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.util.Log

import lib.R

import java.io.File

class Unpac(private val kelas: Activity) {

    private lateinit var pop: PopupWindow

    private lateinit var namaFile: TextView
    private lateinit var namaProses: TextView
    private lateinit var proses: ProgressBar
    private lateinit var cancel: TextView

    private var dir: File? = null
    private var pac: File? = null

    private var process: Process? = null

    init {
        System.loadLibrary("unpac")

        val item = LayoutInflater.from(kelas)
            .inflate(R.layout.pop_unpac, null)

        namaFile = item.findViewById(R.id.nama_file)
        namaProses = item.findViewById(R.id.nama_proses)
        proses = item.findViewById(R.id.proses)
        cancel = item.findViewById(R.id.n)

        pop = PopupWindow(
            item,
            -1,
            -1,
            true
        )

        cancel.setOnClickListener {

            process?.destroy()
            process = null

            if (pop.isShowing) {
                pop.dismiss()
            }
        }
    }

    fun setDir(terima: File) {
        dir = terima
    }

    fun setPac(terima: File) {
        pac = terima
    }

    fun start() {

        val filePac = pac

        if (filePac == null) {
            Toast.makeText(
                kelas,
                "PAC belum dipilih",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val folder = dir

        if (folder == null) {
            Toast.makeText(
                kelas,
                "Folder tujuan belum dipilih",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!filePac.exists()) {
            Toast.makeText(
                kelas,
                "File PAC tidak ditemukan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!folder.exists()) {
            folder.mkdirs()
        }

        namaFile.text = filePac.name
        namaProses.text = "Menyiapkan..."

        proses.isIndeterminate = true

        pop.showAtLocation(
            kelas.window.decorView,
            Gravity.CENTER,
            0,
            0
        )

        Thread {

            try {

                /*
                 * unpac adalah executable ELF ARM64
                 * yang dimasukkan melalui jniLibs.
                 */
                val executable = File(
                    kelas.applicationInfo.nativeLibraryDir,
                    "libunpac.so"
                )

                Log.d(
                    "UNPAC",
                    "path = ${executable.absolutePath}"
                )

                Log.d(
                    "UNPAC",
                    "exists = ${executable.exists()}"
                )

                Log.d(
                    "UNPAC",
                    "canExecute = ${executable.canExecute()}"
                )

                if (!executable.exists()) {
                    throw Exception(
                        "Executable unpac tidak ditemukan"
                    )
                }

                if (!executable.canExecute()) {
                    throw Exception(
                        "Executable unpac tidak dapat dijalankan"
                    )
                }

                val command = arrayOf(
                    executable.absolutePath,
                    filePac.absolutePath,
                    folder.absolutePath
                )

                kelas.runOnUiThread {
                    namaProses.text = "Extracting..."
                }

                process = ProcessBuilder(*command)
                    .redirectErrorStream(true)
                    .start()

                val reader = process!!
                    .inputStream
                    .bufferedReader()

                while (true) {

                    val line = reader.readLine()
                        ?: break

                    kelas.runOnUiThread {
                        namaProses.text = line
                    }
                }

                val hasil = process!!.waitFor()

                process = null

                kelas.runOnUiThread {

                    if (hasil == 0) {

                        namaProses.text = "Selesai"

                        Toast.makeText(
                            kelas,
                            "Extract selesai",
                            Toast.LENGTH_SHORT
                        ).show()

                        pop.dismiss()

                    } else {

                        namaProses.text =
                            "Gagal (exit $hasil)"

                        cancel.text = "Tutup"
                    }
                }

            } catch (e: Exception) {

                process = null

                Log.e(
                    "UNPAC",
                    "Gagal menjalankan unpac",
                    e
                )

                kelas.runOnUiThread {

                    namaProses.text =
                        e.message ?: "Terjadi kesalahan"

                    cancel.text = "Tutup"
                }
            }

        }.start()
    }
}