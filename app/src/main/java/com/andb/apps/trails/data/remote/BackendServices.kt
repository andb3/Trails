package com.andb.apps.trails.data.remote

import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.model.SkiRegion
import retrofit2.http.GET
import retrofit2.http.Query

interface MapService {
    /**Get amount of maps in backend**/
    @GET("maps/count")
    suspend fun getMapCount(): Int

    /**Get maps from backend
     * @param lastUpdate Unix timestamp of last request so backend only delivers updates (0 to get all maps)
     **/
    @GET("maps")
    suspend fun getMaps(@Query("last") lastUpdate: Long): List<SkiMap>
}

interface AreaService {
    /**Get amount of areas in backend**/
    @GET("areas/count")
    suspend fun getAreaCount(): Int

    /**Get updated areas from backend
     * @param lastUpdate Unix timestamp of last request so backend only delivers updates (0 to get all areas)
     **/
    @GET("areas")
    suspend fun getAreas(@Query("last") lastUpdate: Long): List<SkiArea>
}

interface RegionService {
    /**Get amount of regions in backend**/
    @GET("regions/count")
    suspend fun getRegionCount(): Int

    /**Get regions from backend
     * @param lastUpdate Unix timestamp of last request so backend only delivers updates (0 to get all regions)
     **/
    @GET("regions")
    suspend fun getRegions(@Query("last") lastUpdate: Long): List<SkiRegion>
}