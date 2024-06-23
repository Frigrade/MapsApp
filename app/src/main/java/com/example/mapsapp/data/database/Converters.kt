package com.example.mapsapp.data.database

import androidx.room.TypeConverter
import com.example.mapsapp.domain.entity.LatLonLocation

class Converters {
	@TypeConverter

	fun latLonLocationToString(value: LatLonLocation): String  {
		return "${value.latitude} ${value.longitude}"
	}

	@TypeConverter
	fun stringToLatLonLocation(latLonString: String): LatLonLocation {
		val splittedValues = latLonString.split(" ")
		val latitude = splittedValues[0].toDouble()
		val longitude = splittedValues[1].toDouble()

		return LatLonLocation(latitude, longitude)
	}
}