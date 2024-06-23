package com.example.mapsapp.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin")
data class PinInfo(
	val name: String?,
	@PrimaryKey val location: LatLonLocation,
)