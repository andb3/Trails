package com.andb.apps.trails

import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.KotprefModel

const val KEY_SORT_REGIONS = "exploreSortRegions"
const val KEY_SORT_AREAS = "exploreSortAreas"
const val KEY_STARTING_REGION = "startingRegion"
const val KEY_NIGHT_MODE = "nightMode"

object Prefs : KotprefModel() {
    var lastRegionsUpdate by longPref()
    var lastAreasUpdate by longPref()
    var lastMapsUpdate by longPref()

    var exploreSortRegions by intPref(key = KEY_SORT_REGIONS)
    var exploreSortAreas by intPref(key = KEY_SORT_AREAS)
    var startingRegion by intPref(default = 1, key = KEY_STARTING_REGION)
    var nightMode by intPref(default = AppCompatDelegate.MODE_NIGHT_NO, key = KEY_NIGHT_MODE)
}