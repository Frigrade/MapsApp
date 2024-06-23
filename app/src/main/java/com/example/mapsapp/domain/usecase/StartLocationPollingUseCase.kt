package com.example.mapsapp.domain.usecase

import com.example.mapsapp.domain.repository.LocationRepository

class StartLocationPollingUseCase(private val repository: LocationRepository) {

	operator fun invoke() {
		repository.startLocationPolling()
	}
}