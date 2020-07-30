package com.andb.apps.trails.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.model.SkiRegion

@Database(
    entities = [SkiMap::class, SkiArea::class, SkiRegion::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(value = [Converters::class])
abstract class Database : RoomDatabase() {
    abstract fun mapsDao(): MapsDao
    abstract fun areasDao(): AreasDao
    abstract fun regionsDao(): RegionsDao
}