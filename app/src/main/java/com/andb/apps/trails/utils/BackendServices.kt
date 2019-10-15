package com.andb.apps.trails.utils

import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapService {
    /**Get amount of maps in backend**/
    @GET("maps/count")
    suspend fun getMapCount(): Int

    /**Get all maps from backend**/
    @GET("maps")
    suspend fun getAllMaps(): List<SkiMap>

    /**Get specific map from backend**/
    @GET("maps/{mapID}")
    suspend fun getMap(@Path("mapID") mapID: Int): SkiMap

    /**Get updated maps from backend**/
    @GET("maps/new")
    suspend fun getMapUpdates(@Query("last") lastUpdate: Long): List<SkiMap>
}

interface AreaService {
    /**Get amount of areas in backend**/
    @GET("areas/count")
    suspend fun getAreaCount(): Int

    /**Get all areas from backend**/
    @GET("areas")
    suspend fun getAllAreas(): List<SkiArea>

    /**Get specific area from backend**/
    @GET("areas/{areaID}")
    suspend fun getArea(@Path("areaID") areaID: Int): SkiArea

    /**Get updated areas from backend**/
    @GET("areas/new")
    suspend fun getAreaUpdates(@Query("last") lastUpdate: Long): List<SkiArea>
}

interface RegionService {
    /**Get amount of regions in backend**/
    @GET("regions/count")
    suspend fun getRegionCount(): Int

    /**Get all regions from backend**/
    @GET("regions")
    suspend fun getAllRegions(): List<SkiRegion>

    /**Get specific region from backend**/
    @GET("regions/{regionID}")
    suspend fun getRegion(@Path("regionID") regionID: Int): SkiRegion

    /**Get updated regions from backend**/
    @GET("regions/new")
    suspend fun getRegionUpdates(@Query("last") lastUpdate: Long): List<SkiRegion>
}