package rosh.test

import lib.action.Click
import lib.action.Download

import android.os.Bundle
import android.widget.*

import java.io.File

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        PasangId()
        Tombol()
    }
    
    private fun PasangId(){
    
    }
    
    private fun Tombol(){
        Click(findViewById<ImageView>(R.id.nav)).one{
            DownloadUbuntu()
        }
    }
    
    private fun DownloadUbuntu(){
        val u = lib.action.Message(this)
        u.setTitle("warning")
        u.setMessage("You will exit app")
        u.setPositiveButton("sure"){finish()}
        u.setNegativeButton("cencel", null)
        u.show()
    }
}