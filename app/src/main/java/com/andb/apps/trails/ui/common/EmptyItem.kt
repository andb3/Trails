package com.andb.apps.trails.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.andb.apps.trails.R
import kotlinx.android.synthetic.main.empty_item.view.*

class EmptyItem : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EmptyItem, 0, 0)
        try {
            emptyTitle.text = a.getText(R.styleable.EmptyItem_emptyTitle)
            emptySummary.text = a.getText(R.styleable.EmptyItem_emptySummary)
            emptyIcon.setImageDrawable(a.getDrawable(R.styleable.EmptyItem_emptyIcon))
            emptyActionButton.text = a.getText(R.styleable.EmptyItem_buttonText)
            emptyActionButton.icon = a.getDrawable(R.styleable.EmptyItem_buttonIcon)
            emptyActionButton.isVisible = a.getBoolean(R.styleable.EmptyItem_showButton, true)
        } finally {
            a.recycle()
        }
    }

    var title
        get() = emptyTitle.text
        set(value) {
            emptyTitle.text = value
        }

    var summary
        get() = emptySummary.text
        set(value) {
            emptySummary.text = value
        }

    var icon: Drawable?
        get() = emptyIcon.drawable
        set(value) {
            emptyIcon.setImageDrawable(value)
        }

    var buttonText
        get() = emptyActionButton.text
        set(value) {
            emptyActionButton.text = value
        }

    var buttonIcon: Drawable?
        get() = emptyActionButton.icon
        set(value) {
            emptyActionButton.icon = value
        }

    var isButtonVisible
        get() = emptyActionButton.isVisible
        set(value) {
            emptyActionButton.isVisible = value
        }

    init {
        inflate(context, R.layout.empty_item, this)
    }

    fun setButtonAction(onClick: (View) -> Unit) {
        emptyActionButton.setOnClickListener(onClick)
    }

}