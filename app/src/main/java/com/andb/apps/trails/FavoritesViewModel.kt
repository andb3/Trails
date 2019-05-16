package com.andb.apps.trails

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea

class FavoritesViewModel : ViewModel() {
    fun getFavoriteMaps() = mapsDao().getFavorites()
    fun getFavoriteAreas() = areasDao().getFavorites()
}