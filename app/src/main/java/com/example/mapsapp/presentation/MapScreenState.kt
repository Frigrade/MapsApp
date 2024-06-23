package com.example.mapsapp.presentation

import com.example.mapsapp.domain.entity.LatLonLocation
import com.yandex.mapkit.map.PolylineMapObject

sealed class MapScreenState {

	data object Initial: MapScreenState()

	data object Loading: MapScreenState()

	data class Content(
		val location: LatLonLocation?,
		val routeInfo: RouteInfo?
	): MapScreenState()
}

data class RouteInfo(
	val route: List<PolylineMapObject>,
	val destination: LatLonLocation,
)