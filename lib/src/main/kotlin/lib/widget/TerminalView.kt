package lib.widget

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.text.InputType
import android.util.AttributeSet
import android.view.*
import android.widget.*
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import lib.R
import kotlin.math.abs

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TerminalView @JvmOverloads constructor(
    c: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(c, attrs, defStyleAttr) {

    private val bg = Paint().apply {
        color = Color.BLACK
    }

    private val teks = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val teksDir = Paint().apply {
        color = Color.BLUE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
        textSize = 30f
        isAntiAlias = true
    }

    private val kursor = Paint().apply {
        color = Color.parseColor("#FFAAFF")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val maxLine = 5000
    private var isiTerminal: String = "TerminalView ready...\n"
    private var direktoriSaatIni: String = "~"
    private var inputUser: String = ""
    private var statusKey: Boolean = false
    private var isTextSelectable: Boolean = false
    private var onCommandListener: ((String) -> Unit)? = null
    private var posisiScroll = 0f
    private var sentuhY = 0f
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var isScrolling = false
    private var forceScrollToBottom = true
    private var sedangMengetik = false
    private var kursorNyala = true
    private var tinggiIme = 0

    private val kedipKursor = object : Runnable {
        override fun run() {
            if (!sedangMengetik) {
                kursorNyala = !kursorNyala
                invalidate()
            }
            postDelayed(this, 500)
        }
    }

    private val resetMengetik = object : Runnable {
        override fun run() {
            sedangMengetik = false
            kursorNyala = true
            invalidate()
        }
    }

    fun setOnCommandListener(listener: (String) -> Unit) {
        onCommandListener = listener
    }

    init {
        if (attrs != null) {
            val typedArray = c.obtainStyledAttributes(attrs, R.styleable.TerminalView)
            isTextSelectable = typedArray.getBoolean(R.styleable.TerminalView_textSelectable, false)
            typedArray.recycle()
        }

        isFocusable = true
        isFocusableInTouchMode = true
    }

    // ── SATU onAttachedToWindow saja ────────────────
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(kedipKursor)

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val baru = (ime.bottom - bars.bottom).coerceAtLeast(0)
            if (baru != tinggiIme) {
                tinggiIme = baru
                forceScrollToBottom = true   
                invalidate()
            }
            insets  
        }
        ViewCompat.requestApplyInsets(this)
    }

    // ── ini yang tadi hilang, dikembalikan ──────────
    override fun onDetachedFromWindow() {
        removeCallbacks(kedipKursor)
        removeCallbacks(resetMengetik)
        super.onDetachedFromWindow()
    }

    private fun tandaiMengetik() {
        sedangMengetik = true
        kursorNyala = true
        removeCallbacks(resetMengetik)
        postDelayed(resetMengetik, 700)
    }

    private fun scrollToBottom() {
        forceScrollToBottom = true
        invalidate()
    }

    override fun onDraw(kertas: Canvas) {
        super.onDraw(kertas)
        kertas.drawColor(Color.BLACK)

        if (width == 0 || height == 0) return

        val marginKiri = 20f
        val tinggiBaris = teks.descent() - teks.ascent()

        val barisList = isiTerminal.split("\n").toMutableList()
        barisList.add("")

        var simulatedY = 50f
        for (i in barisList.indices) {
            val baris = barisList[i]
            simulatedY = if (i == barisList.lastIndex) {
                MeasureInput(marginKiri, simulatedY)
            } else {
                MeasureTeks(baris, marginKiri, simulatedY, teks)
            }
            simulatedY += tinggiBaris
        }

        val totalHeight = simulatedY - 50f
        val tinggiTerlihat = height - tinggiIme

        var maxScroll = totalHeight - tinggiTerlihat + 150f
        if (maxScroll < 0f) maxScroll = 0f

        if (forceScrollToBottom) {
            posisiScroll = maxScroll
            forceScrollToBottom = false
        }

        if (posisiScroll < 0f) posisiScroll = 0f
        if (posisiScroll > maxScroll) posisiScroll = maxScroll

        kertas.save()
        kertas.translate(0f, -posisiScroll)

        var posisiY = 50f
        for (i in barisList.indices) {
            val baris = barisList[i]
            posisiY = if (i == barisList.lastIndex) {
                TulisInput(kertas, marginKiri, posisiY)
            } else {
                TulisTeks(kertas, baris, marginKiri, posisiY, teks)
            }
            posisiY += tinggiBaris
        }

        kertas.restore()
    }

    private fun MeasureTeks(teksTulis: String, x: Float, y: Float, paint: Paint): Float {
        val lebarMaks = width - x - 20f
        val tinggiBaris = paint.descent() - paint.ascent()

        if (paint.measureText(teksTulis) <= lebarMaks) return y

        var bagian = ""
        var posisiY = y

        for (karakter in teksTulis) {
            val calon = bagian + karakter
            if (paint.measureText(calon) > lebarMaks) {
                if (bagian.isNotEmpty()) posisiY += tinggiBaris
                bagian = karakter.toString()
            } else {
                bagian = calon
            }
        }
        return posisiY
    }

    private fun MeasureInput(x: Float, y: Float): Float {
        val bagianDirDanSimbol = "$direktoriSaatIni $ "
        val lebarDir = teksDir.measureText(bagianDirDanSimbol)
        return MeasureTeks(inputUser, x + lebarDir, y, teks)
    }

    private fun ukurPosisiAkhir(teksTulis: String, x0: Float, y0: Float, paint: Paint): Pair<Float, Float> {
        val lebarMaks = width - x0 - 20f
        val tinggiBaris = teks.descent() - teks.ascent()

        if (paint.measureText(teksTulis) <= lebarMaks) {
            return (x0 + paint.measureText(teksTulis)) to y0
        }

        var bagian = ""
        var y = y0

        for (karakter in teksTulis) {
            val calon = bagian + karakter
            if (paint.measureText(calon) > lebarMaks && bagian.isNotEmpty()) {
                y += tinggiBaris
                bagian = karakter.toString()
            } else {
                bagian = calon
            }
        }
        return (x0 + paint.measureText(bagian)) to y
    }

    fun append(terima: String?) {
        if (terima != null) {
            isiTerminal += terima
            val barisList = isiTerminal.split("\n")
            if (barisList.size > maxLine) {
                isiTerminal = barisList.takeLast(maxLine).joinToString("\n")
            }
            scrollToBottom()
        }
    }

    fun textClear() {
        isiTerminal = ""
        inputUser = ""
        scrollToBottom()
    }

    fun setText(terima: String?) {
        isiTerminal = terima ?: ""
        scrollToBottom()
    }

    fun setDir(terima: String?, maxFolder: Int = 2) {
        val dir = terima ?: "~"
        val bagian = dir.trim('/').split('/').filter { it.isNotEmpty() }
        direktoriSaatIni = if (bagian.size > maxFolder) {
            ".../" + bagian.takeLast(maxFolder).joinToString("/")
        } else {
            dir
        }
        scrollToBottom()
    }

    fun runCmd(): String {
        return inputUser
    }

    private fun OpenKey() {
        requestFocus()
        val key = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        key.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        statusKey = true
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or
                             InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                             InputType.TYPE_TEXT_FLAG_MULTI_LINE
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE

        return object : BaseInputConnection(this, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text != null) {
                    inputUser += text.toString()
                    tandaiMengetik()
                    scrollToBottom()
                }
                return true
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                if (beforeLength > 0 && inputUser.isNotEmpty()) {
                    inputUser = inputUser.dropLast(beforeLength.coerceAtMost(inputUser.length))
                    tandaiMengetik()
                    scrollToBottom()
                }
                return true
            }

            override fun sendKeyEvent(event: KeyEvent): Boolean {
                return this@TerminalView.dispatchKeyEvent(event)
            }

            override fun performEditorAction(actionCode: Int): Boolean {
                if (actionCode == EditorInfo.IME_ACTION_DONE ||
                    actionCode == EditorInfo.IME_ACTION_GO ||
                    actionCode == EditorInfo.IME_ACTION_NEXT
                ) {
                    executeCommand()
                    return true
                }
                return false
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            executeCommand()
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (inputUser.isNotEmpty()) {
                inputUser = inputUser.dropLast(1)
                tandaiMengetik()
                scrollToBottom()
            }
            return true
        }

        val unicode = event?.unicodeChar ?: 0
        if (unicode != 0) {
            inputUser += unicode.toChar()
            tandaiMengetik()
            scrollToBottom()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun executeCommand() {
        val perintahFinal = inputUser
        isiTerminal += "$direktoriSaatIni $ $perintahFinal\n"
        inputUser = ""
        scrollToBottom()
        onCommandListener?.invoke(perintahFinal)
    }

    private fun TulisTeks(kertas: Canvas, teksTulis: String, x: Float, y: Float, paint: Paint): Float {
        val lebarMaks = width - x - 20f
        val tinggiBaris = teks.descent() - teks.ascent()

        if (paint.measureText(teksTulis) <= lebarMaks) {
            kertas.drawText(teksTulis, x, y, paint)
            return y
        }

        var bagian = ""
        var posisiY = y

        for (karakter in teksTulis) {
            val calon = bagian + karakter
            if (paint.measureText(calon) > lebarMaks) {
                if (bagian.isNotEmpty()) {
                    kertas.drawText(bagian, x, posisiY, paint)
                    posisiY += tinggiBaris
                }
                bagian = karakter.toString()
            } else {
                bagian = calon
            }
        }

        if (bagian.isNotEmpty()) {
            kertas.drawText(bagian, x, posisiY, paint)
        }

        return posisiY
    }

    private fun TulisInput(kertas: Canvas, x: Float, y: Float): Float {
        val bagianDirDanSimbol = "$direktoriSaatIni $ "
        val lebarDir = teksDir.measureText(bagianDirDanSimbol)
        val xInput = x + lebarDir

        kertas.drawText(bagianDirDanSimbol, x, y, teksDir)

        val yAkhir = TulisTeks(kertas, inputUser, xInput, y, teks)

        if (sedangMengetik || kursorNyala) {
            val (xKursor, yKursor) = ukurPosisiAkhir(inputUser, xInput, y, teks)
            val tebalKursor = 16f
            kertas.drawRect(
                xKursor + 2f, yKursor + teks.ascent(),
                xKursor + 2f + tebalKursor, yKursor + teks.descent(),
                kursor
            )
        }

        return yAkhir
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                sentuhY = event.y
                isScrolling = false
                downTime = System.currentTimeMillis()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val selisih = sentuhY - event.y
                if (abs(event.y - downY) > 15f) {
                    isScrolling = true
                }

                posisiScroll += selisih
                sentuhY = event.y
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val upTime = System.currentTimeMillis()

                if (!isScrolling) {
                    if (upTime - downTime > 500) {
                        tampilkanMenuCopyPaste()
                    } else {
                        OpenKey()
                        performClick()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun tampilkanMenuCopyPaste() {
        val opsi = arrayOf("Copy Semua Output", "Paste", "Select Terpilih")
        val ad = AlertDialog.Builder(context)
        ad.setTitle("Terminal Menu")
        ad.setItems(opsi) { dialog, which ->
            when (which) {
                0 -> copyKeClipboard()
                1 -> pasteDariClipboard()
                2 -> {
                    dialog.dismiss()
                    popTerpilh()
                }
            }
        }
        ad.show()
    }

    private fun copyKeClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val teksCopy = isiTerminal + "$direktoriSaatIni $ $inputUser"
        val clip = ClipData.newPlainText("Terminal Output", teksCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Teks disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun pasteDariClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val pasteData = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            if (pasteData.isNotEmpty()) {
                val amanPaste = pasteData.replace("\n", " ").replace("\r", "")
                inputUser += amanPaste
                tandaiMengetik()
                scrollToBottom()
            }
        }
    }

    private fun popTerpilh() {
        val t = TextView(context).apply {
            text = isiTerminal
            setTextIsSelectable(true)
            textSize = 16f
            setPadding(30, 30, 30, 30)
        }

        val sv = ScrollView(context).apply {
            addView(t)
        }

        AlertDialog.Builder(context)
            .setTitle("Select Text")
            .setView(sv)
            .setPositiveButton("Close") { log, _ ->
                log.dismiss()
            }
            .show()
    }
}
