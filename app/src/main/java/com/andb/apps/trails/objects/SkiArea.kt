package com.andb.apps.trails.objects

class SkiArea(id: Int,
              name: String,
              liftCount: Int,
              runCount: Int,
              openingYear: Int,
              website: String,
              val maps: ArrayList<SkiMap>
) : BaseSkiArea(id, name, liftCount, runCount, openingYear, website) {

    constructor(baseArea: BaseSkiArea,
                maps: ArrayList<SkiMap>) : this(baseArea.id, baseArea.name, baseArea.liftCount, baseArea.runCount, baseArea.openingYear, baseArea.website, maps)
}