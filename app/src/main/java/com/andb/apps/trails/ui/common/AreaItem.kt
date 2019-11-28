package com.andb.apps.trails.ui.common

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.andb.apps.trails.R
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.ui.area.openAreaView
import com.andb.apps.trails.ui.map.openMapView
import com.andb.apps.trails.util.GlideApp
import com.andb.apps.trails.util.dpToPx
import com.andb.apps.trails.util.mainThread
import com.andb.apps.trails.util.newIoThread
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.area_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class AreaItem : ConstraintLayout, KoinComponent {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    val mapsRepo: MapsRepository by inject()
    private var onFavoriteListener: ((map: SkiArea, favorite: Boolean) -> Unit)? = null


    init {
        inflate(context, R.layout.area_item, this)
    }


    fun setup(area: SkiArea) {
        areaName.text = area.name
        areaMaps.text = String.format(context.getString(R.string.map_count), area.maps.size)
        areaLikeButton.apply {
            isLiked = area.isFavorite()
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    onFavoriteListener?.invoke(area, true)
                }

                override fun unLiked(p0: LikeButton?) {
                    onFavoriteListener?.invoke(area, false)
                }
            })
        }
        val mapPreviewID = area.mapPreviewID()
        if (mapPreviewID != null) {
            newIoThread {
                val mapUrl = mapsRepo.getMapByID(mapPreviewID)?.thumbnails?.last()?.url
                mainThread {
                    GlideApp.with(this@AreaItem)
                        .load(mapUrl)
                        .placeholder(GradientDrawable().also {
                            it.setColor(ContextCompat.getColor(context, R.color.placeholderColor)); it.cornerRadius = dpToPx(8)
                            .toFloat()
                        })
                        .transforms(CenterCrop(), RoundedCorners(dpToPx(8)))
                        .into(areaMapPreview)
                }
            }
            areaMapPreview.transitionName = "mapTransitionAreaItem$mapPreviewID"
            areaMapPreview.setOnClickListener {
                openMapView(mapPreviewID, context, areaMapPreview)
            }
        }else{
            //no placeholder if no maps
            areaMapPreview.setImageDrawable(null)
            areaMapPreview.setOnClickListener {  }
        }
        setOnClickListener { openAreaView(area.id, context) }

    }

    fun setOnFavoriteListener(onFavorite: (area: SkiArea, favorite: Boolean) -> Unit) {
        onFavoriteListener = onFavorite
    }
}

