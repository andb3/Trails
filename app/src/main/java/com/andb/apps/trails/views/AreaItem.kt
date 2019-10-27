package com.andb.apps.trails.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.openMapView
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.utils.GlideApp
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.area_item.view.*

class AreaItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        inflate(context, R.layout.area_item, this)
    }

    fun setup(area: SkiArea, onFavorite: (favorite: Boolean) -> Unit){
        setup(area.id, area.name, area.maps.size, area.mapPreviewID(), area.favorite, onFavorite)
    }

    fun setup(id: Int, name: String, mapCount: Int, mapPreviewID: Int?, favorite: Boolean, onFavorite: (favorite: Boolean) -> Unit) {
        areaName.text = name
        areaMaps.text = String.format(context.getString(R.string.map_count), mapCount)
        areaLikeButton.apply {
            isLiked = favorite
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    onFavorite.invoke(true)
                }

                override fun unLiked(p0: LikeButton?) {
                    onFavorite.invoke(false)
                }
            })
        }
        if (mapPreviewID != null) {
            newIoThread {
                val mapUrl = MapsRepo.getMapByID(mapPreviewID)?.url
                mainThread {
                    GlideApp.with(this@AreaItem)
                        .load(mapUrl)
                        .placeholder(GradientDrawable().also { it.setColor(ContextCompat.getColor(context, R.color.placeholderColor)); it.cornerRadius = dpToPx(8).toFloat() })
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
        setOnClickListener { openAreaView(id, context) }

    }

}

