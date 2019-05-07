package com.andb.apps.trails.utils

import com.andb.apps.trails.objects.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface SkiMapOrgService {
    @GET("/SkiMaps/view/{mapId}.xml")
    fun getMap(@Path("mapId") mapId: Int): Deferred<Response<SkiMap>>

    @GET("/SkiAreas/view/{areaId}.xml")
    fun getArea(@Path("areaId") areaId: Int): Deferred<Response<SkiArea>>

    @GET("/SkiRegions/view/{regionId}.xml")
    fun getRegion(@Path("regionId") regionId: Int): Deferred<Response<SkiRegion>>


}

const val BASE_URL = "https://skimap.org"
fun makeMapService(): SkiMapOrgService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(SkiMapOrgService::class.java)

fun makeAreaService(): SkiMapOrgService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(SkiMapOrgService::class.java)

fun makeRegionService(): SkiMapOrgService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(SkiMapOrgService::class.java)