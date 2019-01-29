package com.andb.apps.trails

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager




class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    var swipeLocked: Boolean = true

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NonSwipeViewPager, 0, 0)
        try {
            swipeLocked = !a.getBoolean(R.styleable.NonSwipeViewPager_swipeable, false)
        } finally {
            a.recycle()
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (swipeLocked) {
            false
        } else {
            super.canScrollHorizontally(direction)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return if (swipeLocked) {
            false
        } else {
            super.onInterceptTouchEvent(event)
        }    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return if (swipeLocked) {
            false
        } else {
            super.onTouchEvent(event)
        }
    }
}