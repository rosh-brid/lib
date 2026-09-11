package lib.aksi

import lib.R
import lib.aksi.Klik

import android.widget.*
import android.view.*
import android.app.Activity

import java.io.*

class Download(private val kelas:Activity){

    private lateinit var item : View
    private lateinit var pop : PopupWindow
    private lateinit var namaLink : TextView
    private lateinit var prosesBar : ProgressBar
    private lateinit var persen : TextView
    private lateinit var rootProses : LinearLayout
    private lateinit var anakProses : LinearLayout
    private lateinit var ukuran : TextView
    
    private var lokasiFile : File?
    private var namaFile : String?
    private var link : String = ""
    
    init{
        item = = LayoutInflater.from(kelas).inflate(R.layout.pop_download, null)
        pop = PopupWindow(item, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
        namaLink = item.findViewById(R.id.nama)
        prosesBar = item.findViewById(R.id.proses_bar)
        persen = item.findViewById(R.id.persen)
        rootProses = item.findViewById(R.id.root_proses)
        anakProses = item.findViewById(R.id.anak_persen)
        ukuran = item.findViewById(R.id.ukuran)
        
        Klik(item.findViewById<TextView>(R.id.n)).sekali{
            pop.dismiss()
        }
    }

    fun setLink(terima:String?){
        if(terima != null){
            link = terima
        }
    }
    
    fun setDir(terima:String?){
        if(terima != null){
            lokasiFile = terima
        }
    }
    
    fun setName(terima:String?){
        if(terima != null){
            namaFile = terima
        }
    }
    
    fun start(){
        val root = kelas.window.decorView.rootView
        pop.showAtLocation(root, Gravity.CENTER, 0, 0)
        mulaiUnduh()
    }
    
    private fun mulaiUnduh(){
        
    }
}
