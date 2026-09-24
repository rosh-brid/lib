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
        val u = lib.action.FileProperti(this)
        u.setFile(File("/storage/FDFF-B5F2/firmware/g9_pro/SC9863A_E78_Android10_6501_vsim_V1.0_20210616.pac"))
        u.start()
    }
}