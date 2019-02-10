package com.andb.apps.trails.utils

import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface SkiMapOrgService {
    @GET("/SkiMaps/view/{mapId}.xml")
    fun getMap(@Path("mapId") mapId: Int): Deferred<Response<List<SkiMap>>>
}

const val BASE_URL = "https://skimap.org"
fun makeService(): SkiMapOrgService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(SkiMapOrgService::class.java)