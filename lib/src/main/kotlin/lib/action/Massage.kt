package lib.action

import lib.R

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

class Massage(private val kelas: Activity) {

    private val item: View
    private val pop: PopupWindow

    private val j: TextView
    private val p: TextView
    private val n: TextView
    private val y: TextView

    private val inti: LinearLayout
    private val anak: LinearLayout

    private var aksiPositive: (() -> Unit)? = null
    private var aksiNegative: (() -> Unit)? = null

    init {

        item = LayoutInflater
            .from(kelas)
            .inflate(R.layout.pop_message, null)

        pop = PopupWindow(
            item,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        j = item.findViewById(R.id.judul)
        p = item.findViewById(R.id.pesan)
        n = item.findViewById(R.id.n)
        y = item.findViewById(R.id.y)

        inti = item.findViewById(R.id.inti)
        anak = item.findViewById(R.id.anak)

        j.text = "Warning"
        p.text = "Message"

        inti.setOnClickListener {
            pop.dismiss()
        }

        anak.setOnClickListener {
        }

        n.visibility = View.GONE
        y.visibility = View.GONE
    }

    fun setTitle(terima: String?) {
        j.text = terima ?: "Warning"
    }

    fun setMassage(terima: String?) {
        p.text = terima ?: "Message"
    }

    fun setPositiveButton(
        terima: String?,
        aksi: () -> Unit
    ) {
        if (terima == null) {
            y.visibility = View.GONE
            aksiPositive = null
            return
        }

        y.visibility = View.VISIBLE
        y.text = terima

        aksiPositive = aksi

        y.setOnClickListener {
            aksiPositive?.invoke()
            pop.dismiss()
        }
    }

    fun setNegativeButton(
        terima: String?,
        aksi: (() -> Unit)? = null
    ) {
        if (terima == null) {
            n.visibility = View.GONE
            aksiNegative = null
            return
        }

        n.visibility = View.VISIBLE
        n.text = terima

        aksiNegative = aksi

        n.setOnClickListener {
            aksiNegative?.invoke()
            pop.dismiss()
        }
    }

    fun show() {
        val root = kelas.window.decorView.rootView

        pop.showAtLocation(
            root,
            Gravity.CENTER,
            0,
            0
        )
    }

    fun dismiss() {
        pop.dismiss()
    }
}