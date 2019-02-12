package com.andb.apps.trails.views.items

import android.content.Context
import android.os.Bundle
import android.transition.Transition
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.andb.apps.trails.AreaViewFragment
import com.andb.apps.trails.MapViewFragment
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.BaseSkiArea
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

    fun setup(area: BaseSkiArea) {
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
        setOnClickListener { openAreaView(area, context, areaName) }
    }

}

fun openAreaView(area: BaseSkiArea, context: Context, text: View) {
    val fragmentActivity = context as FragmentActivity
    val ft = fragmentActivity.supportFragmentManager.beginTransaction()
    //ft.addSharedElement(context.areaItemBackground, "areaLayout")
    //ft.addSharedElement(text, "areaViewName")
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    val intent = AreaViewFragment()
    intent.arguments =
        Bundle().also { it.putInt("areaKey", area.id) }
    ft.add(R.id.exploreAreaReplacement, intent)
    ft.addToBackStack("areaView")
    ft.commit()
}