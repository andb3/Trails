package com.andb.apps.trails.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andb.apps.trails.converters.IDListConverter
import com.andb.apps.trails.converters.SkiAreaDetailsConverter
import com.andb.apps.trails.converters.ThumbnailListConverter
import com.andb.apps.trails.objects.*
import dev.matrix.roomigrant.GenerateRoomMigrations

@Database(entities = [SkiMap::class, SkiArea::class, SkiRegion::class], version = 1, exportSchema = true)
@TypeConverters(value = [ThumbnailListConverter::class, IDListConverter::class, SkiAreaDetailsConverter::class])
@GenerateRoomMigrations
abstract class Database : RoomDatabase() {
    abstract fun mapsDao(): MapsDao
    abstract fun areasDao(): AreasDao
    abstract fun regionsDao(): RegionsDao

    companion object {
        lateinit var db: com.andb.apps.trails.database.Database

        fun setDB(ctxt: Context) {
            db = Room.databaseBuilder(ctxt, com.andb.apps.trails.database.Database::class.java, "TrailsDatabase")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

}

fun db() = com.andb.apps.trails.database.Database.db
fun mapsDao() = db().mapsDao()
fun areasDao() = db().areasDao()
fun regionsDao() = db().regionsDao()