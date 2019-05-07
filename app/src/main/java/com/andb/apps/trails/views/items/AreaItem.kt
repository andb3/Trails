package com.andb.apps.trails.views.items

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.trails.R
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.openAreaView
import com.andb.apps.trails.openMapView
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.views.GlideApp
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

    fun setup(area: SkiArea) {
        areaName.text = area.name
        areaMaps.text = String.format(context.getString(R.string.map_count), area.maps.size)
        areaLikeButton.apply {
            isLiked = AreasRepo.getFavoriteAreas().contains(area)
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    area.toggleFavorite()
                    areasDao().updateArea(area)
                }

                override fun unLiked(p0: LikeButton?) {
                    area.toggleFavorite()
                    areasDao().updateArea(area)
                }
            })
        }
        newIoThread {
            val previewUrl = area.mapPreviewUrl()
            mainThread {
                GlideApp.with(this@AreaItem).load(previewUrl)
                    .transforms(CenterCrop(), RoundedCorners(dpToPx(8))).into(areaMapPreview)
                areaMapPreview.setOnClickListener {
                    val previewId = area.mapPreviewId()
                    if(previewId!=null){
                        openMapView(previewId, area.name, context)
                    }
                }
            }
        }
        setOnClickListener { openAreaView(area.id, context) }

    }

}

