package com.andb.apps.trails.objects

import androidx.room.*

@Entity
class SkiMap(
    @PrimaryKey
    @ColumnInfo(name = "map_id")
    val id: Int,
    @Embedded
    val skiArea: BaseSkiArea,
    val imageUrl: String,
    val year: Int
    //val artist: MapArtist
) {

    constructor(baseMap: BaseSkiMap, imageUrl: String):this(baseMap.id, baseMap.skiArea, imageUrl, baseMap.year)

    fun isPdf(): Boolean{
        return imageUrl.takeLast(4)==".pdf"
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("ID: $id \n")
        stringBuilder.append("Area: ${skiArea.name}, Area ID: ${skiArea.id} \n")
        stringBuilder.append("Year: $year \n")
        //stringBuilder.append("Artist: ${artist.name}, Artist ID: ${artist.id} \n")

        return stringBuilder.toString()
    }
}