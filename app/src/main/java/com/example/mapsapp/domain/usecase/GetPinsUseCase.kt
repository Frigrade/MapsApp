package com.example.mapsapp.domain.usecase

import com.example.mapsapp.domain.entity.PinInfo
import com.example.mapsapp.domain.repository.PinRepository

class GetPinsUseCase(private val repository: PinRepository) {

	suspend operator fun invoke(): List<PinInfo> {
		return repository.getAll()
	}
}