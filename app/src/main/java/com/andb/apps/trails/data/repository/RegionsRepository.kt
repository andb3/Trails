package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.model.SkiRegion

interface RegionsRepository {
    fun getAll(): LiveData<List<SkiRegion>>
    suspend fun getRegionByID(id: Int): SkiRegion?

}
