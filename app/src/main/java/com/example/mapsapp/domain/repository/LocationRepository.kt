package com.example.mapsapp.domain.repository

import com.example.mapsapp.domain.entity.LatLonLocation
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {

	fun getLocationFlow(): StateFlow<LatLonLocation?>

	fun startLocationPolling()

	fun stopLocationPolling()
}