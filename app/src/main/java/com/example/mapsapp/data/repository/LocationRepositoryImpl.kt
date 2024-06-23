package com.example.mapsapp.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.mapsapp.domain.entity.LatLonLocation
import com.example.mapsapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

	private companion object {

		const val ACCURACY_METERS = 30f
	}

	private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

	private val gpsLocationListener: LocationListener = LocationListener { location ->
		if (location.accuracy <= ACCURACY_METERS) {
			locationFlow.tryEmit(LatLonLocation(latitude = location.latitude, longitude = location.longitude))
		}
	}

	private val networkLocationListener = LocationListener { location ->
		if (location.accuracy <= ACCURACY_METERS) {
			locationFlow.tryEmit(LatLonLocation(latitude = location.latitude, longitude = location.longitude))
		}
	}

	private val locationFlow = MutableStateFlow<LatLonLocation?>(null)

	override fun getLocationFlow(): StateFlow<LatLonLocation?> {
		return locationFlow
	}

	@SuppressLint("MissingPermission")
	override fun startLocationPolling() {
		stopLocationPolling()

		if (isLocationPermissionGranted()) {
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				2000,
				0f,
				gpsLocationListener
			)

			locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				2000,
				0f,
				networkLocationListener
			)
		} else {
			locationFlow.tryEmit(null)
		}
	}

	override fun stopLocationPolling() {
		locationManager.removeUpdates(networkLocationListener)
		locationManager.removeUpdates(gpsLocationListener)
	}

	private fun isLocationPermissionGranted(): Boolean =
		ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}