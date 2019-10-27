package com.andb.apps.trails.repository

import android.content.Context
import android.util.Log
import com.andb.apps.trails.BuildConfig
import okhttp3.*



class MockInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val uri = chain.request().url().uri().toString()
        Log.d("mockInterceptor", "uri: $uri")

        if (BuildConfig.DEBUG) {
            val responseString = when {
                uri.endsWith("/regions") ->{
                    val ins = appContext.resources.openRawResource(appContext.resources.getIdentifier("regions", "raw", appContext.packageName))
                    ins.bufferedReader().use { it.readText() }
                }
                uri.endsWith("/areas")->{
                    val ins = appContext.resources.openRawResource(appContext.resources.getIdentifier("areas", "raw", appContext.packageName))
                    ins.bufferedReader().use { it.readText() }
                }
                uri.endsWith("/maps")->{
                    val ins = appContext.resources.openRawResource(appContext.resources.getIdentifier("maps", "raw", appContext.packageName))
                    ins.bufferedReader().use { it.readText() }
                }
                uri.endsWith("/regions/count") ->{ "338" }
                uri.endsWith("/areas/count")->{ "4420" }
                uri.endsWith("/maps/count")->{ "79" }
                else -> ""
            }
            //Log.d("mockInterceptor", "response: $responseString")
            return chain.proceed(chain.request())
                .newBuilder()
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message(responseString)
                .body(
                    ResponseBody.create(
                        MediaType.parse("application/json"),
                    responseString.toByteArray()))
                .addHeader("content-type", "application/json")
                .build()
        } else {
            //just to be on safe side.
            throw IllegalAccessError("MockInterceptor is only meant for Testing Purposes and " +
                    "bound to be used only with DEBUG mode")
        }
    }
}

