package lib.os

import android.app.Activity

class Display(private val kelas: Activity) {

    fun height(): Int {
        return kelas.resources.displayMetrics.heightPixels
    }

    fun width(): Int {
        return kelas.resources.displayMetrics.widthPixels
    }
}