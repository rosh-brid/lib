package lib.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import lib.R
import kotlin.math.atan2

class SnackProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isIndeterminate: Boolean = true
        set(value) {
            if (field == value) {
                invalidate()
                return
            }
            field = value
            if (value) startLoopAnimation() else stopLoopAnimation()
            invalidate()
        }

    private var _progress: Float = 0f
    var progress: Float
        get() = _progress
        set(value) {
            _progress = value.coerceIn(0f, 100f)
            if (isIndeterminate) {
                isIndeterminate = false // setter akan invalidate
            } else {
                invalidate()
            }
        }

    private var loopAnimator: ValueAnimator? = null
    private var animatedFraction: Float = 0f

    private val basePath = Path()
    private val scaledPath = Path()
    private val snakePath = Path()
    private val tonguePath = Path()
    private val matrix = Matrix()
    private val posTan = FloatArray(4) // [x, y, tanX, tanY]

    private var pathMeasure: PathMeasure? = null
    private var pathLength: Float = 0f

    private val colorMain: Int = runCatching {
        ContextCompat.getColor(context, R.color.main)
    }.getOrDefault(Color.parseColor("#1e293b"))

    private val colorParent: Int = runCatching {
        ContextCompat.getColor(context, R.color.parent)
    }.getOrDefault(Color.parseColor("#22c55e"))

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 14f
        color = colorMain
    }

    private val snakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
        color = colorParent
        setShadowLayer(15f, 0f, 0f, colorParent)
    }

    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorParent
    }

    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#022c22")
    }

    private val tonguePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2f
        color = Color.RED
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorParent
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        alpha = 100
    }

    init {
        basePath.moveTo(40f, 60f)
        basePath.cubicTo(40f, 20f, 80f, 20f, 100f, 60f)
        basePath.cubicTo(120f, 100f, 160f, 100f, 160f, 60f)
        basePath.cubicTo(160f, 20f, 120f, 20f, 100f, 60f)
        basePath.cubicTo(80f, 100f, 40f, 100f, 40f, 60f)
        basePath.close()

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isIndeterminate && loopAnimator?.isRunning != true) {
            startLoopAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoopAnimation()
    }

    private fun startLoopAnimation() {
    if (loopAnimator?.isRunning == true) return
    loopAnimator?.cancel()
    loopAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            this@SnackProgress.animatedFraction = it.animatedValue as Float
            invalidate()
        }
        start()
    }
}

    private fun stopLoopAnimation() {
        loopAnimator?.cancel()
        loopAnimator = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val desiredWidth =
            (200f * density + paddingLeft + paddingRight).toInt().coerceAtLeast(suggestedMinimumWidth)
        val desiredHeight =
            (120f * density + paddingTop + paddingBottom).toInt().coerceAtLeast(suggestedMinimumHeight)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        val padH = 20f
        val left = paddingLeft + padH
        val top = paddingTop + padH
        val right = w - paddingRight - padH
        val bottom = h - paddingBottom - padH

        val availableWidth = (right - left).coerceAtLeast(1f)
        val availableHeight = (bottom - top).coerceAtLeast(1f)

        val scale = (availableWidth / 200f).coerceAtMost(availableHeight / 120f)

        val scaledWidth = 200f * scale
        val scaledHeight = 120f * scale
        val dx = left + (availableWidth - scaledWidth) / 2f
        val dy = top + (availableHeight - scaledHeight) / 2f

        matrix.reset()
        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)

        scaledPath.reset()
        basePath.transform(matrix, scaledPath)
        pathMeasure = PathMeasure(scaledPath, false)
        pathLength = pathMeasure?.length ?: 0f

        textPaint.textSize = h * 0.25f
        trackPaint.strokeWidth = scale * 8f
        snakePaint.strokeWidth = scale * 6f
        tonguePaint.strokeWidth = (scale * 2f).coerceAtLeast(1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pm = pathMeasure ?: return
        if (pathLength <= 0f) return

        canvas.drawPath(scaledPath, trackPaint)

        // Label tengah
        val centerX = width / 2f
        val textY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(
            if (isIndeterminate) "∞" else "${progress.toInt()}%",
            centerX, textY, textPaint
        )

        val snakeLength = pathLength * 0.25f
        val headDistance: Float
        val tailDistance: Float

        if (isIndeterminate) {
            headDistance = animatedFraction * pathLength
            tailDistance = (headDistance - snakeLength + pathLength) % pathLength
        } else {
            headDistance = (_progress / 100f) * pathLength
            tailDistance = 0f
        }

        snakePath.reset()
        if (headDistance < tailDistance) {
            // Snake melingkar melewati titik 0
            pm.getSegment(tailDistance, pathLength, snakePath, true)
            pm.getSegment(0f, headDistance, snakePath, false)
        } else {
            pm.getSegment(tailDistance, headDistance, snakePath, true)
        }
        canvas.drawPath(snakePath, snakePaint)

        if (isIndeterminate || headDistance > 0f) {
            drawSnakeHead(canvas, pm, headDistance)
        }
    }

    private fun drawSnakeHead(canvas: Canvas, pm: PathMeasure, headDistance: Float) {
        pm.getPosTan(headDistance, posTan, null)
        val tanX = posTan[2]
        val tanY = posTan[3]

        val degrees = Math.toDegrees(atan2(tanY.toDouble(), tanX.toDouble())).toFloat()
        val headScale = (snakePaint.strokeWidth / 6f).coerceAtLeast(0.5f)

        canvas.save()
        canvas.translate(posTan[0], posTan[1])
        canvas.rotate(degrees)

        // Kepala
        canvas.drawCircle(0f, 0f, 4f * headScale, headPaint)

        // Mata
        val eyeSpread = 2f * headScale
        val eyeOffset = 1.5f * headScale
        val eyeRadius = 1f * headScale
        canvas.drawCircle(eyeOffset, -eyeSpread, eyeRadius, eyePaint)
        canvas.drawCircle(eyeOffset, eyeSpread, eyeRadius, eyePaint)

        // Lidah — animasi hanya saat indeterminate
        val tongueStretch = if (isIndeterminate) {
            val phase = (animatedFraction * 4f) % 1f
            if (phase > 0.5f) 1.2f else 1.0f
        } else 1.0f

        canvas.save()
        canvas.scale(tongueStretch, 1f, 4f * headScale, 0f)

        tonguePath.reset()
        tonguePath.moveTo(4f * headScale, 0f)
        tonguePath.lineTo(8f * headScale, 0f)
        tonguePath.moveTo(8f * headScale, 0f)
        tonguePath.lineTo(10f * headScale, -1.5f * headScale)
        tonguePath.moveTo(8f * headScale, 0f)
        tonguePath.lineTo(10f * headScale, 1.5f * headScale)
        canvas.drawPath(tonguePath, tonguePaint)

        canvas.restore()
        canvas.restore()
    }
}