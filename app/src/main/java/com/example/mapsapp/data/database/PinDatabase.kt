package com.example.mapsapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mapsapp.domain.entity.PinInfo

@Database(entities = [PinInfo::class], version = 1)
@TypeConverters(Converters::class)
abstract class PinDatabase : RoomDatabase() {

	abstract fun pinDao(): PinDao
}
