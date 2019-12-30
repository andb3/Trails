package com.andb.apps.trails.ui.explore

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.trails.util.dp

class ExploreItemDecoration : RecyclerView.ItemDecoration(){
    private val margin = 16.dp
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        view.updatePadding(left = margin, right = margin)
    }
}