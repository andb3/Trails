package com.andb.apps.trails.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.trails.R
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.openMapView
import com.andb.apps.trails.repository.MapsRepo
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
                val mapUrl = MapsRepo.getMapByID(mapPreviewID)
                mainThread {
                    GlideApp.with(this@AreaItem).load(mapUrl)
                        .transforms(CenterCrop(), RoundedCorners(dpToPx(8))).into(areaMapPreview)
                }
            }
            areaMapPreview.setOnClickListener {
                openMapView(mapPreviewID, name, context)
            }
        }
        setOnClickListener { openAreaView(id, context) }

    }

}

