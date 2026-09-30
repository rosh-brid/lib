package lib.widget.roshcode

import lib.R

import android.content.*
import android.graphics.*
import android.text.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.text.InputType
import android.view.inputmethod.*
import android.widget.*

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CodeEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    
    private var target:String
    private val lebar:Float
    private val panjang:Float
    
    private val gaya : Paint
    private var kursor : Float
    private var bgGlobal: Int
    private var textGlobal : Int
    
    init{
        target = "ini kode"
        lebar = width.toFloat()
        panjang = height.toFloat()
        gaya = Paint()
        kursor = 0f
        bgGlobal = context.getColor(R.color.ui_bg)
        textGlobal = context.getColor(R.color.text)
        
        isFocusable = true
        isFocusableInTouchMode = true
    }
    
    override fun onDraw(c:Canvas){
        super.onDraw(c)
        
        Root(c)
    }
    
    fun setText(terima:String?){
        if(terima != null){target = terima}
    }
    
    private fun Root(c:Canvas){
        val warna = gaya.apply{ color = bgGlobal }
        val tinggi = BacaTinggi()
        
        c.drawRect(0f, 0f, lebar, tinggi, warna)
        Number(c)
        EditText(c)
    }
    
    private fun EditText(c: Canvas) {
    val warna = gaya.apply {
        color = textGlobal
        textSize = 30f
    }

   // if (gaya.measureText(target) <= lebar) {
            c.drawText( target, 40f, 40f, warna )
       // }
    }
    
    private fun Number(c:Canvas){
        val warna = gaya.apply {
            color = textGlobal
            textSize = 30f
        }
        
        c.drawText("1", 0f, 0f, warna)
    }
    
    private fun BacaTinggi():Float{
        return height.toFloat()
    }
    
    private fun StatusKeyboard(): Boolean {
        val root = rootView
        val tinggiRoot = root.height
        val tinggiVisible = root.rootWindowInsets?.getInsets(
            WindowInsetsCompat.Type.ime()
        )?.bottom ?: 0

        return tinggiVisible > 0
    }

    private fun BukaKeyboard() {
        requestFocus()

        val input = context.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        input.showSoftInput( this, InputMethodManager.SHOW_IMPLICIT )
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                return true
            }

            MotionEvent.ACTION_UP -> {
                return true
            }
        }
        return true
    }
    
     
}