package com.andb.apps.trails.utils

import android.content.res.Resources
import android.view.View
import android.widget.TextView

object Utils {
    fun showIfAvailable(value: Int, textView: TextView, stringId: Int) {
        textView.apply {
            if (value != -1) {
                text = String.format(context.getString(stringId), value)
                visibility = View.VISIBLE

            } else {
                visibility = View.GONE
            }
        }
    }

    fun showIfAvailable(value: String, textView: TextView, stringId: Int) {
        textView.apply {
            if (value != "") {
                text = String.format(context.getString(stringId), value)
                visibility = View.VISIBLE

            } else {
                visibility = View.GONE
            }
        }
    }
}

fun dpToPx(dp: Int): Int{
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}