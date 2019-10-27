package com.andb.apps.trails.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo

class FavoritesViewModel : ViewModel() {
    fun getFavoriteMaps() = MapsRepo.getFavoriteMaps()
    fun getFavoriteAreas() = AreasRepo.getFavoriteAreas()
}