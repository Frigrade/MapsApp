package com.example.mapsapp.domain.usecase

import com.example.mapsapp.domain.entity.LatLonLocation
import com.example.mapsapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.StateFlow

class GetLocationFlowUseCase(private val repository: LocationRepository) {

	operator fun invoke(): StateFlow<LatLonLocation?> {
		return repository.getLocationFlow()
	}
}