package com.andb.apps.trails.utils

import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MapService {
    @GET("SkiMaps/view/{mapId}.xml")
    fun getMap(@Path("mapId") mapId: Int): Deferred<Response<SkiMap?>>
}

interface AreaService {
    @GET("SkiAreas/view/{areaId}.xml")
    fun getArea(@Path("areaId") areaId: Int): Deferred<Response<SkiArea?>>
}

interface RegionService {
    @GET("Regions/view/{regionId}.xml")
    fun getRegion(@Path("regionId") regionId: Int): Deferred<Response<SkiRegion?>>
}