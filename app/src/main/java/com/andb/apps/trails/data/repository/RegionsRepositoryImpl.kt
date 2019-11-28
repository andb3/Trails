package com.andb.apps.trails.data.repository

import com.andb.apps.trails.data.local.RegionsDao
import com.andb.apps.trails.data.model.SkiRegion

class RegionsRepositoryImpl(private val regionsDao: RegionsDao) : RegionsRepository {

    override fun getAll() = regionsDao.getAll()
    override suspend fun getRegionByID(id: Int): SkiRegion? = regionsDao.getRegionByID(id)

}