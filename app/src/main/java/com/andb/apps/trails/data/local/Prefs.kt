package com.andb.apps.trails.data.local

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.execute
import com.chibatching.kotpref.pref.AbstractPref
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

const val KEY_SORT_REGIONS = "exploreSortRegions"
const val KEY_SORT_AREAS = "exploreSortAreas"
const val KEY_STARTING_REGION = "startingRegion"
const val KEY_NIGHT_MODE = "nightMode"
const val KEY_FAVORITES_ORDER_MAP = "favoritesOrderMap"
const val KEY_FAVORITES_ORDER_AREA = "favoritesOrderArea"

object Prefs : KotprefModel() {

    var lastRegionsUpdate by longPref()
    var lastAreasUpdate by longPref()
    var lastMapsUpdate by longPref()

    var exploreSortRegions by intPref(key = KEY_SORT_REGIONS)
    var exploreSortAreas by intPref(key = KEY_SORT_AREAS)
    var startingRegion by intPref(default = 1, key = KEY_STARTING_REGION)
    var nightMode by intPref(default = AppCompatDelegate.MODE_NIGHT_NO, key = KEY_NIGHT_MODE)

    private val moshi = Moshi.Builder().build()
    private val favoritesAdapter = moshi.adapter(Int::class.java)
    var favoritesOrderMap by listPref<Int>(
        key = KEY_FAVORITES_ORDER_MAP,
        default = listOf(),
        adapter = favoritesAdapter
    )
    var favoritesOrderArea by listPref<Int>(
        key = KEY_FAVORITES_ORDER_AREA,
        default = listOf(),
        adapter = favoritesAdapter
    )
}

/**
 * Delegate list shared preferences property.
 * @param default default list value
 * @param key custom preferences key
 * @param commitByDefault commit this property instead of apply
 */
fun <T : Any> KotprefModel.listPref(
    default: List<T> = listOf(),
    key: String? = null,
    adapter: JsonAdapter<T>,
    commitByDefault: Boolean = commitAllPropertiesByDefault
): ReadWriteProperty<KotprefModel, List<T>> =
    ListPref(default, key, adapter, commitByDefault)

/**
 * Delegate list shared preferences property.
 * @param default default list value
 * @param key custom preferences key resource id
 * @param commitByDefault commit this property instead of apply
 */
fun <T : Any> KotprefModel.listPref(
    default: List<T> = listOf(),
    key: Int,
    adapter: JsonAdapter<T>,
    commitByDefault: Boolean = commitAllPropertiesByDefault
): ReadWriteProperty<KotprefModel, List<T>> =
    listPref(default, context.getString(key), adapter, commitByDefault)

internal class ListPref<T : Any>(
    val default: List<T>,
    override val key: String?,
    val adapter: JsonAdapter<T>,
    private val commitByDefault: Boolean
) : AbstractPref<List<T>>() {

    val moshi = Moshi.Builder().build()
    val converters = Converters()
    private val stringAdapter = moshi.adapter<List<String>>(List::class.java)

    override fun getFromPreference(property: KProperty<*>, preference: SharedPreferences): List<T> {
        val asString = preference.getString(key ?: property.name, null) ?: return default
        return stringToList(asString)
    }

    @SuppressLint("CommitPrefEdits")
    override fun setToPreference(
        property: KProperty<*>,
        value: List<T>,
        preference: SharedPreferences
    ) {
        val toString = listToString(value)
        preference.edit().putString(key ?: property.name, toString).execute(commitByDefault)
    }

    override fun setToEditor(
        property: KProperty<*>,
        value: List<T>,
        editor: SharedPreferences.Editor
    ) {
        val toString = listToString(value)
        editor.putString(key ?: property.name, toString)
    }

    fun listToString(list: List<T>): String {
        return stringAdapter.toJson(list.map { adapter.toJson(it) })
    }

    fun stringToList(data: String?): List<T> {
        if (data == null) {
            return emptyList()
        }
        val strings: List<String> =
            stringAdapter.fromJson(data) as List<String>? ?: return emptyList()
        return strings.mapNotNull { adapter.fromJson(it) }
    }
}
