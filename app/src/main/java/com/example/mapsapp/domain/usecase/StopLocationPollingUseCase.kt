package com.example.mapsapp.domain.usecase

import com.example.mapsapp.domain.repository.LocationRepository

class StopLocationPollingUseCase(private val repository: LocationRepository) {

	operator fun invoke() {
		repository.stopLocationPolling()
	}
}