package lib.action

import android.view.MotionEvent
import android.view.View

class Click(private val view: View) {

    private var sedangLempar = false

    fun one(action: () -> Unit) {
        view.setOnClickListener {
            view.animate()
                .scaleX(0.55f).scaleY(0.55f).setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f).scaleY(1f).setDuration(100)
                        .withEndAction { action() }
                        .start()
                }
                .start()
        }
    }

    fun long(action: () -> Unit) {
        view.setOnLongClickListener {
            view.animate()
                .scaleX(0.55f).scaleY(0.55f).setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f).scaleY(1f).setDuration(100)
                        .withEndAction { action() }
                        .start()
                }
                .start()
            true
        }
    }

    private var aksiKanan: (() -> Unit)? = null
    private var aksiKiri: (() -> Unit)? = null
    private var geserTerpasang = false
    private var startX = 0f

    fun goRight(action: () -> Unit = {}) {
        pasangGeser()
        aksiKanan = action
    }

    fun goLeft(action: () -> Unit = {}) {
        pasangGeser()
        aksiKiri = action
    }

    private fun pasangGeser() {
        if (geserTerpasang) return
        geserTerpasang = true

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val delta  = event.x - startX
                    val ambang = view.resources.displayMetrics.widthPixels * 0.2f

                    when {
                        sedangLempar     -> Unit  // abaikan saat animasi berjalan
                        delta > ambang   -> lempar(keKanan = true)  { aksiKanan?.invoke() }
                        delta < -ambang  -> lempar(keKanan = false) { aksiKiri?.invoke() }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun lempar(keKanan: Boolean, action: () -> Unit) {
        if (sedangLempar) return
        sedangLempar = true

        val lebar  = view.resources.displayMetrics.widthPixels.toFloat()
        val keluar = if (keKanan) lebar else -lebar
        val masuk  = -keluar
        val miring = if (keKanan) 20f else -20f

        view.animate()
            .translationX(keluar)
            .rotation(miring)
            .alpha(0f)
            .setDuration(220)
            .withEndAction {
                action()  

                view.translationX = masuk
                view.rotation = -miring
                view.alpha = 0f

                view.animate()
                    .translationX(0f)
                    .rotation(0f)
                    .alpha(1f)
                    .setDuration(220)
                    .withEndAction { sedangLempar = false }
                    .start()
            }
            .start()
    }
}
