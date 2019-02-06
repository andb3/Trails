package com.andb.apps.trails.utils

import android.content.Context
import android.view.View
import android.widget.TextView

object Utils {
    fun showIfAvailible(value: Int, textView: TextView, stringId: Int) {
        textView.apply {
            if (value != -1) {
                text = String.format(context.getString(stringId), value)
                visibility = View.VISIBLE

            } else {
                visibility = View.GONE
            }
        }
    }

    fun showIfAvailible(value: String, textView: TextView, stringId: Int) {
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