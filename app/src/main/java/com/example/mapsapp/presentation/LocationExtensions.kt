package com.example.mapsapp.presentation

import com.example.mapsapp.domain.entity.LatLonLocation
import com.yandex.mapkit.geometry.Point

fun LatLonLocation.toPoint(): Point =
	Point(latitude, longitude)

fun Point.toLatLonLocation(): LatLonLocation =
	LatLonLocation(latitude, longitude)