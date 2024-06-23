package com.example.mapsapp.data.repository

import com.example.mapsapp.data.database.PinDao
import com.example.mapsapp.domain.entity.PinInfo
import com.example.mapsapp.domain.repository.PinRepository

class PinRepositoryImpl(private val dao: PinDao): PinRepository {

	override suspend fun getAll(): List<PinInfo> {
		return dao.getAll()
	}

	override suspend fun insert(pinInfo: PinInfo) {
		return dao.insert(pinInfo)
	}

	override suspend fun delete(pinInfo: PinInfo) {
		return dao.delete(pinInfo)
	}
}