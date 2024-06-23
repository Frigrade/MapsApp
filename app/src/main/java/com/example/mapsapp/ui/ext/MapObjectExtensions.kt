package com.example.mapsapp.ui.ext

import com.example.mapsapp.domain.entity.LatLonLocation
import com.yandex.mapkit.map.PlacemarkMapObject

fun PlacemarkMapObject.compare(latLonLocation: LatLonLocation?): Boolean =
	latLonLocation?.latitude == geometry.latitude
		&& latLonLocation.longitude == geometry.longitude