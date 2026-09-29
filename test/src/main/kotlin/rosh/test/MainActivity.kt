package rosh.test

import lib.action.Click
import lib.action.Download

import android.os.Bundle
import android.widget.*
import android.view.*
import android.net.Uri
import android.content.*

import java.io.File

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.*
import androidx.core.view.*
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var pusat: DrawerLayout

    private var file: File? = null

    private lateinit var pilihButton: Button
    private lateinit var hasil: TextView

    private val pilihFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val uri = result.data?.data ?: return@registerForActivityResult

        file = uri.toFile()

        if (file != null) {
            pilihButton.text = file!!.name
            hasil.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PasangId()
        Awal()
        Tombol()
    }

    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
    }

    override fun onBackPressed() {
        if (pusat.isDrawerOpen(GravityCompat.START)) {
            pusat.closeDrawer(GravityCompat.START)
        } else {
            Keluar()
        }
    }

    private fun Awal() {

    }

    private fun Keluar() {
        val m = lib.action.Massage(this)

        m.setTitle("Exit")
        m.setMassage("You will exit app")
        m.setPositiveButton("Sure") {
            finish()
        }
        m.setNegativeButton("Cencel", null)
        m.show()
    }
    
    private fun DeleteFeature() {
        val item = LayoutInflater.from(this).inflate(R.layout.pop_hapus, null)
        val pop = PopupWindow( item,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true )
        val inti = item.findViewById<LinearLayout>(R.id.inti)
        val anak = item.findViewById<LinearLayout>(R.id.anak)
        val pilih = item.findViewById<Button>(R.id.pilih)
        val y = item.findViewById<TextView>(R.id.y)

        pilihButton = pilih
        hasil = y

        y.visibility = View.GONE

        Click(item.findViewById<TextView>(R.id.n)).one {
            pop.dismiss()
        }

        Click(pilih).one {

            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            pilihFile.launch(i)
        }

        val hapus = lib.action.Delete(this)

        Click(y).one {
            val target = file ?: return@one
            pop.dismiss()
            hapus.setFile(listOf(target))
            hapus.start()
        }

        anak.setOnClickListener {}

        inti.setOnClickListener {
            pop.dismiss()
        }

        pop.showAtLocation( window.decorView.rootView, Gravity.CENTER, 0, 0 )
    }
    
    private fun Uri.toFile(): File {

        val nama = "hapus_${System.currentTimeMillis()}"

        val hasil = File(cacheDir, nama)

        contentResolver.openInputStream(this)?.use { input ->

            hasil.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return hasil
    }

    private fun Tombol() {

        Click(findViewById<ImageView>(R.id.nav)).one {
            pusat.openDrawer(GravityCompat.START)
        }

        Click(findViewById<ImageView>(R.id.tutup)).one {
            pusat.closeDrawer(GravityCompat.START)
        }

        Click(findViewById<ImageView>(R.id.keluar)).one {
            Keluar()
        }

        Click(findViewById<ImageView>(R.id.github)).one {

        }

        val l = lib.os.Display(this)
        val download = findViewById<LinearLayout>(R.id.download)

        download.layoutParams.apply {
            width = l.width() / 3
            height = l.height() / 5
        }

        Click(download).one {

        }

        val unzip = findViewById<LinearLayout>(R.id.unzip)

        unzip.layoutParams.apply {
            width = l.width() / 3
            height = l.height() / 5
        }

        Click(unzip).one {

        }

        val delete = findViewById<LinearLayout>(R.id.delete)

        delete.layoutParams.apply {
            width = l.width() / 3
            height = l.height() / 5
        }

        Click(delete).one {
            DeleteFeature()
        }
        
        val code = findViewById<LinearLayout>(R.id.code)
        code.layoutParams.apply {
            width = l.width() / 3
            height = l.height() / 5
        }
        Click(code).one{
            startActivity(Intent(this, Code::class.java))
        }
    }
}