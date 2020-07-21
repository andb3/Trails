package com.andb.apps.trails.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RatioImageView : AppCompatImageView {

    // Inherited constructors
    var ratio: Ratio = Ratio(1, 1)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // If one of the measures is match_parent, use that one to determine the size.
        // If not, use the default implementation of onMeasure.
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, (widthSize * ratio.heightMultiplier).toInt())
        } else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
            setMeasuredDimension((heightSize * ratio.widthMultiplier).toInt(), heightSize)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}

data class Ratio(val width: Int, val height: Int) {
    val heightMultiplier get() = height.toDouble() / width.toDouble()
    val widthMultiplier get() = width.toDouble() / height.toDouble()
}