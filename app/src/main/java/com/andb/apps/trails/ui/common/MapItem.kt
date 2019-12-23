package com.andb.apps.trails.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.ui.map.openMapView
import com.andb.apps.trails.util.dpToPx
import com.andb.apps.trails.util.glide.GlideApp
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.map_item.view.*

class MapItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private var onFavoriteListener: ((map: SkiMap, favorite: Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.map_item, this)
    }

    fun setup(map: SkiMap, areaName: String, favorite: Boolean = false) {
        GlideApp.with(this)
            .load(map.thumbnails.last().url)
            .fitCenter()
            .into(mapListItemImage)
        mapFavoritesAreaName.apply {
            if (favorite) {
                text = areaName
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
                (this@MapItem.mapListItemYear.layoutParams as LayoutParams).topMargin = dpToPx(8)
            }
        }
        mapListItemYear.text = map.year.toString()
        mapListItemImage.transitionName = "mapTransitionMapItem${map.id}"
        setOnClickListener {
            openMapView(map.id, context, mapListItemImage)
        }

        mapListFavoriteButton.apply {
            isLiked = map.favorite
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    onFavoriteListener?.invoke(map, true)
                }

                override fun unLiked(p0: LikeButton?) {
                    onFavoriteListener?.invoke(map, false)
                }
            })
        }
    }

    fun setOnFavoriteListener(onFavorite: (map: SkiMap, favorite: Boolean) -> Unit) {
        onFavoriteListener = onFavorite
    }
}