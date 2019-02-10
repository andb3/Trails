package com.andb.apps.trails.views.items

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.andb.apps.trails.MapViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.views.GlideApp
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.area_item.*
import kotlinx.android.synthetic.main.area_item.view.*

class AreaItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        inflate(context, R.layout.area_item, this)
    }

    fun setup(area: BaseSkiArea, onClick: (BaseSkiArea) -> Unit) {
        areaName.text = area.name
        areaMaps.text = String.format(context.getString(R.string.map_count), area.mapCount)
        areaLikeButton.apply {
            isLiked = FavoritesList.contains(area)
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) { FavoritesList.add(area) }
                override fun unLiked(p0: LikeButton?) { FavoritesList.remove(area) }
            })
        }
        GlideApp.with(this).load(area.mapPreviewUrl).transforms(CenterCrop(), RoundedCorners(dpToPx(8))).into(areaMapPreview)
        areaMapPreview.setOnClickListener {
            val activity = context as FragmentActivity
            val ft = activity.supportFragmentManager.beginTransaction()
            ft.addToBackStack("mapView")

            val fragment = MapViewFragment()
            val bundle = Bundle()
            bundle.putInt("mapKey", area.mapPreviewId)
            fragment.arguments = bundle

            ft.add(R.id.mapViewHolder, fragment)
            ft.commit()
        }
        setOnClickListener { onClick(area) }
    }
}