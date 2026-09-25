package rosh.test

import lib.action.Click
import lib.action.Download

import android.os.Bundle
import android.widget.*

import java.io.File

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.*
import androidx.core.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var pusat : DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        PasangId()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
    }
    
    override fun onBackPressed(){
        if(pusat.isDrawerOpen(GravityCompat.START)){
            pusat.closeDrawer(GravityCompat.START)
        }else{Keluar()}
    }
    
    private fun Keluar(){
        val m = lib.action.Massage(this)
            m.setTitle("Exit")
            m.setMassage("You will exit app")
            m.setPositiveButton("Sure"){finish()}
            m.setNegativeButton("Cencel", null)
            m.show()
    }
    
    private fun Tombol(){
        Click(findViewById<ImageView>(R.id.nav)).one{
            pusat.openDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.tutup)).one{
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.keluar)).one{
            Keluar()
        }
    }
}