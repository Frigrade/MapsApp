package com.example.mapsapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.mapsapp.domain.entity.PinInfo

@Dao
interface PinDao {

    @Query("SELECT * FROM pin")
    suspend fun getAll(): List<PinInfo>

    @Insert
    suspend fun insert(pinInfo: PinInfo)

    @Delete
    suspend fun delete(pinInfo: PinInfo)
}