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

    private val gaya: Paint
    private val linePaint: Paint
    private val cursorPaint: Paint
    private var textLayout: StaticLayout? = null
    private var isiKode = SpannableStringBuilder()
    private var bg: Int
    private var normalColorCode: Int
    private var sizeText: Float
    private var scrollX = 0f
    private var scrollY = 0f
    private var posisiCursor = 0
    private var lineNumberWidth = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var keyboardHeight = 0

    private var isCursorVisible = true
    private val cursorBlinkRunnable = object : Runnable {
        override fun run() {
            isCursorVisible = !isCursorVisible
            invalidate()
            postDelayed(this, 500)
        }
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val layout = textLayout ?: return false
            val maxScrollY = (layout.height - height + paddingBottom).coerceAtLeast(0).toFloat()
            val maxScrollX = (layout.width - (width - lineNumberWidth)).coerceAtLeast(0f)

            scrollX = (scrollX + distanceX).coerceIn(0f, maxScrollX)
            scrollY = (scrollY + distanceY).coerceIn(0f, maxScrollY)
            
            invalidate()
            return true
        }
        
        override fun onLongPress(e: MotionEvent) {
            TekanLama()
        }
    })


    init {
        bg = context.getColor(R.color.bg_log)
        normalColorCode = context.getColor(R.color.text)
        sizeText = 30f

        gaya = Paint().apply { isAntiAlias = true }
        
        linePaint = Paint().apply {
            color = Color.GRAY
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        cursorPaint = Paint().apply {
            color = normalColorCode
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        isFocusableInTouchMode = true
        isFocusable = true
        isLongClickable = true

        post(cursorBlinkRunnable)
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val ime = insets.getInsets( WindowInsetsCompat.Type.ime() )
    
            keyboardHeight = ime.bottom
            android.util.Log.d( "CodeEditor", "height=${height}, ime=${ime.bottom}" )
            UiKeyboardShow()
            invalidate()
            insets
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(cursorBlinkRunnable)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        BgNomor(c)
        Nomor(c)
        BgEditText(c)
        EditText(c)
    }

    fun backgroundCode(terima: Int?) {
        bg = terima ?: context.getColor(R.color.bg_log)
        invalidate()
    }

    fun normalCodeColor(terima: Int?) {
        normalColorCode = terima ?: context.getColor(R.color.text)
        cursorPaint.color = normalColorCode
        updateLayout()
        invalidate()
    }

    fun textSize(terima: Float?) {
        sizeText = terima ?: 30f
        updateLayout()
        invalidate()
    }

    fun setText(terima: String?) {
        isiKode.clear()
        if (terima != null) isiKode.append(terima)
        posisiCursor = isiKode.length
        updateLayout()
        invalidate()
    }

    fun append(terima: String) {
        isiKode.append(terima)
        posisiCursor = isiKode.length
        updateLayout()
        invalidate()
    }
    
    // TODO: Pewarnaan (Belakangan)
    fun codeTypeComment(color: Int) { }
    fun codeTypeKeyword(color: Int) { }
    fun codeTypeString(color: Int) { }
    fun codeTypeInt(color: Int) { }
    fun codeTypeOprator(color: Int) { }
    fun codeTypeIdentifier(color: Int) { }
    fun codeTypeTag(color: Int) { }
    fun codeTypeFunction(color: Int) { }

    private fun BgNomor(c: Canvas) {
        gaya.color = bg 
        c.drawRect(0f, 0f, lineNumberWidth, height.toFloat(), gaya)
    }

    private fun Nomor(c: Canvas) {
        val layout = textLayout ?: return
        c.save()
        c.translate(0f, -scrollY) 
        
        linePaint.textSize = sizeText * 0.8f
        val rightPadding = dpToPx(3f)

        for (i in 0 until layout.lineCount) {
            val baseline = layout.getLineBaseline(i).toFloat()
            if (baseline > scrollY - sizeText && baseline - sizeText < scrollY + height) {
                c.drawText("${i + 1}", lineNumberWidth - rightPadding, baseline, linePaint)
            }
        }
        c.restore()
    }

    private fun BgEditText(c: Canvas) {
        gaya.color = bg
        c.drawRect(lineNumberWidth, 0f, width.toFloat(), height.toFloat(), gaya)
    }

    private fun EditText(c: Canvas) {
        if (textLayout == null) updateLayout()
        
        c.save()
        c.translate(lineNumberWidth - scrollX, -scrollY)
        textLayout?.draw(c)
        drawCursor(c)
        c.restore()
    }

    private fun drawCursor(c: Canvas) {
        if (!isCursorVisible) return
        
        val layout = textLayout ?: return
        val safeCursor = posisiCursor.coerceIn(0, isiKode.length)
        
        val line = layout.getLineForOffset(safeCursor)
        val x = layout.getPrimaryHorizontal(safeCursor)
        val y1 = layout.getLineTop(line).toFloat()
        val y2 = layout.getLineBottom(line).toFloat()
        
        c.drawLine(x, y1, x, y2, cursorPaint)
    }

    private fun insertText(text: CharSequence) {
        val safeCursor = posisiCursor.coerceIn(0, isiKode.length)
        isiKode.insert(safeCursor, text)
        posisiCursor += text.length
        
        isCursorVisible = true
        removeCallbacks(cursorBlinkRunnable)
        postDelayed(cursorBlinkRunnable, 500)
        
        updateLayout()
        invalidate()
    }

    private fun deleteText(before: Int, after: Int) {
        val safeCursor = posisiCursor.coerceIn(0, isiKode.length)
        val start = (safeCursor - before).coerceAtLeast(0)
        val end = (safeCursor + after).coerceAtMost(isiKode.length)
        
        if (start < end) {
            isiKode.delete(start, end)
            posisiCursor = start
        }
        
        isCursorVisible = true
        removeCallbacks(cursorBlinkRunnable)
        postDelayed(cursorBlinkRunnable, 500)
        
        updateLayout()
        UiKeyboardShow()
    }


    private fun updateLayout() {
        if (width == 0) return 
        
        val textPaint = TextPaint(gaya).apply {
            color = normalColorCode
            textSize = sizeText
        }
        
        val lineCount = isiKode.toString().count { it == '\n' } + 1
        val maxNumberString = lineCount.toString() 
        
        linePaint.textSize = sizeText * 0.8f 
        val textWidth = linePaint.measureText(maxNumberString)
        val totalPadding = dpToPx(5f)
        lineNumberWidth = textWidth + totalPadding

        val lines = isiKode.toString().split("\n")
        var maxWidth = 0f
        for (line in lines) {
            val lineWidth = textPaint.measureText(line)
            if (lineWidth > maxWidth) maxWidth = lineWidth
        }
        
        val availableWidth = (width - lineNumberWidth).toFloat()
        val layoutWidth = (maxWidth + 50f).coerceAtLeast(availableWidth).toInt()

        textLayout = StaticLayout.Builder.obtain(isiKode, 0, isiKode.length, textPaint, layoutWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.1f) 
            .setIncludePad(false)
            .build()
    }

    private fun BukaKeyboard() {
        requestFocus()
        val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
    
    override fun onCheckIsTextEditor(): Boolean = true
    
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        
        return object : BaseInputConnection(this, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                insertText(text ?: "")
                return true
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                deleteText(beforeLength, afterLength)
                return true
            }
            
            override fun sendKeyEvent(event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                        deleteText(1, 0)
                        return true
                    } else if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        val textString = isiKode.toString()
                        val safeCursor = posisiCursor.coerceIn(0, textString.length)
                        val lastNewLine = textString.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0))
                        
                        val currentLine = if (lastNewLine == -1) {
                            textString.substring(0, safeCursor)
                        } else {
                            textString.substring(lastNewLine + 1, safeCursor)
                        }
                        
                        val indent = currentLine.takeWhile { it == ' ' || it == '\t' }
                        insertText("\n$indent")
                        return true
                    }
                }
                return super.sendKeyEvent(event)
            }
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.x
            lastTouchY = event.y
        }

        val handled = gestureDetector.onTouchEvent(event)
        
        if (event.action == MotionEvent.ACTION_UP && !handled) {
            val layout = textLayout
            if (layout != null) {
                val touchX = (event.x - lineNumberWidth) + scrollX
                val touchY = event.y + scrollY
                
                if (touchX >= 0) {
                    val lineIndex = layout.getLineForVertical(touchY.toInt())
                    if (lineIndex >= 0 && lineIndex < layout.lineCount) {
                        posisiCursor = layout.getOffsetForHorizontal(lineIndex, touchX)
                        
                        isCursorVisible = true
                        removeCallbacks(cursorBlinkRunnable)
                        postDelayed(cursorBlinkRunnable, 500)
                        invalidate()
                    }
                }
            }
            BukaKeyboard()
        }
        return true
    }
    
    private fun TekanLama() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val item = LayoutInflater.from(context).inflate(R.layout.item_pilih_text, null)
        
        val popupWindow = PopupWindow(item, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        
        val salin = item.findViewById<ImageView>(R.id.salin)
        val potong = item.findViewById<ImageView>(R.id.potong)
        val tempel = item.findViewById<ImageView>(R.id.tempel)
        val pilihSemua = item.findViewById<ImageView>(R.id.pilih_semua)
        
        tempel.visibility = if (clipboard.hasPrimaryClip()) View.VISIBLE else View.GONE
        
        tempel.setOnClickListener {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val textToPaste = clip.getItemAt(0).text.toString()
                insertText(textToPaste)
            }
            popupWindow.dismiss()
        }

        pilihSemua.setOnClickListener {
            posisiCursor = isiKode.length
            invalidate()
            popupWindow.dismiss()
        }
        
        salin.setOnClickListener {
            val clip = ClipData.newPlainText("CodeEditor", isiKode.toString())
            clipboard.setPrimaryClip(clip)
            popupWindow.dismiss()
        }

        potong.setOnClickListener {
            val clip = ClipData.newPlainText("CodeEditor", isiKode.toString())
            clipboard.setPrimaryClip(clip)
            isiKode.clear()
            posisiCursor = 0
            updateLayout()
            invalidate()
            popupWindow.dismiss()
        }
        
        val location = IntArray(2)
        getLocationInWindow(location) 
        
        val popupX = location[0] + lastTouchX.toInt()
        val popupY = (location[1] + lastTouchY.toInt()) - 150 
        
        popupWindow.showAtLocation( this, Gravity.NO_GRAVITY,  popupX,  popupY.coerceAtLeast(0) )
    }
    
    private fun UiKeyboardShow() {
        val layout = textLayout ?: return
        val safeCursor = posisiCursor.coerceIn( 0, isiKode.length )
        val line = layout.getLineForOffset(safeCursor)
        val yTop = layout.getLineTop(line).toFloat()
        val yBottom = layout.getLineBottom(line).toFloat()
        val areaBawah = ( height - keyboardHeight ).coerceAtLeast(0)
        val paddingKeyboard = dpToPx(40f)
        val batasBawah = ( areaBawah - paddingKeyboard ).coerceAtLeast(0f)

        if (yBottom > scrollY + batasBawah) {
            scrollY = yBottom - batasBawah
        } else if (yTop < scrollY) { scrollY = yTop }

        val maxScrollY = ( layout.height - areaBawah ).coerceAtLeast(0).toFloat()
        scrollY = scrollY.coerceIn( 0f, maxScrollY )

        val x = layout.getPrimaryHorizontal(safeCursor)
        val availableWidth = ( width - lineNumberWidth ).coerceAtLeast(0f)
        val paddingHorizontal = dpToPx(40f)

        if (x > scrollX + availableWidth - paddingHorizontal) {
            scrollX = ( x - availableWidth + paddingHorizontal ).coerceAtLeast(0f)
        } else if (x < scrollX) {
            scrollX = x.coerceAtLeast(0f)
        }

        val maxScrollX = ( layout.width - availableWidth ).coerceAtLeast(0f)

        scrollX = scrollX.coerceIn( 0f, maxScrollX )
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateLayout()
        ViewCompat.requestApplyInsets(this)
        UiKeyboardShow()
    }

}
