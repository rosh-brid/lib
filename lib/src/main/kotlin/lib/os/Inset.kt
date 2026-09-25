package lib.os

import android.app.Activity

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Inset(private val kelas:Activity) {

    fun setInset(view: View) {
        val kiri = view.paddingLeft
        val atas = view.paddingTop
        val kanan = view.paddingRight
        val bawah = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                kiri + bars.left,
                atas + bars.top,
                kanan + bars.right,
                bawah + bars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(view)
    }
}