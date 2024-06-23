package com.example.mapsapp.domain.usecase

import com.example.mapsapp.domain.entity.PinInfo
import com.example.mapsapp.domain.repository.PinRepository

class InsertPinUseCase(private val repository: PinRepository) {

	suspend operator fun invoke(pinInfo: PinInfo) {
		repository.insert(pinInfo)
	}
}