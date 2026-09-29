package rosh.test

import lib.widget.roshcode.CodeEditor
import lib.widget.FolderView
import lib.action.Click

import android.os.*
import android.net.Uri
import android.widget.*

import java.io.File

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.*
import androidx.core.view.*

class Code : AppCompatActivity(){
    
    private lateinit var pusat:DrawerLayout 
    private lateinit var editor:CodeEditor

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        editor = findViewById(R.id.editor)
    }
    
    private fun Awal() {
        
    }
    
    private fun Tombol(){
        Click(findViewById<ImageView>(R.id.nav)).one{
            pusat.openDrawer(GravityCompat.START)
        }
    }
}
