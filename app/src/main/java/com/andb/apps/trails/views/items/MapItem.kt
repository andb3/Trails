package com.andb.apps.trails.views.items

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.trails.R
import com.andb.apps.trails.lists.FavoritesList
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.openMapView
import com.andb.apps.trails.utils.dpToPx
import com.andb.apps.trails.views.GlideApp
import com.andb.apps.trails.xml.MapXMLParser
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.map_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

class MapItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        inflate(context, R.layout.map_item, this)
    }

    fun setup(map: SkiMap, favorite: Boolean = false) {
        GlideApp.with(this)
            .load(map.imageUrl)
            .fitCenter()
            .into(mapListItemImage)
        mapFavoritesAreaName.apply {
            if (favorite) {
                text = map.skiArea.name
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
                (this@MapItem.mapListItemYear.layoutParams as ConstraintLayout.LayoutParams).topMargin = dpToPx(8)
            }
        }
        mapListItemYear.text = map.year.toString()
        setOnClickListener {
            openMapView(map.id, context)
        }

        mapListFavoriteButton.apply {
            isLiked = FavoritesList.contains(map)
            setOnLikeListener(object : OnLikeListener {
                override fun liked(p0: LikeButton?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val saveMap = MapXMLParser.parseThumbnail(map.id)
                        if (saveMap != null) {
                            withContext(Dispatchers.Main) {
                                FavoritesList.add(saveMap)
                            }
                        }
                    }
                }

                override fun unLiked(p0: LikeButton?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val savedMap = MapXMLParser.parseThumbnail(map.id)
                        if (savedMap != null) {
                            withContext(Dispatchers.Main) {
                                FavoritesList.remove(savedMap)
                            }
                        }

                    }
                }
            })
        }
    }
}