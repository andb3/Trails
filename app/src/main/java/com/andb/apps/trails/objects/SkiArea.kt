package com.andb.apps.trails.objects

import java.net.URL

class SkiArea(val id: Int,
              val name: String,
              val liftCount: Int,
              val runCount: Int,
              val openingYear: Int,
              val website: URL,
              val regions: ArrayList<BaseSkiRegion>,
              val maps: ArrayList<Map>
              ) {
}