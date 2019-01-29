package com.andb.apps.trails.objects

class SkiRegion(val id: Int,
                val name: String,
                val mapCount: Int,
                val areas: ArrayList<SkiArea>,
                val children: ArrayList<SkiRegion>) {
}