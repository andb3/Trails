package com.andb.apps.trails

import com.chibatching.kotpref.KotprefModel

object Prefs : KotprefModel() {
    var lastRegionsUpdate by longPref()
    var lastAreasUpdate by longPref()
    var lastMapsUpdate by longPref()

    var exploreSortRegions by booleanPref(key = "exploreSortRegions")
    var exploreSortMaps by booleanPref(key = "exploreSortMaps")
    var startingRegion by intPref(default = 1, key = "startingRegion")
}