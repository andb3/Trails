package com.andb.apps.trails.objects

import android.graphics.drawable.Drawable
import java.lang.StringBuilder

class Map(
        val id: Int,
        val skiArea: BaseSkiArea,
        val image: Drawable,
        val year: Int,
        val artist: MapArtist
){
    override fun toString(): String {
        val stringBuilder: StringBuilder = StringBuilder()

        stringBuilder.append("ID: $id \n")
        stringBuilder.append("Area: ${skiArea.name}, Area ID: ${skiArea.id} \n")
        stringBuilder.append("Year: $year \n")
        stringBuilder.append("Artist: ${artist.name}, Artist ID: ${artist.id} \n")

        return stringBuilder.toString()
    }
}