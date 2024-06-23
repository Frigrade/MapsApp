package com.example.mapsapp.domain.repository

import com.example.mapsapp.domain.entity.PinInfo

interface PinRepository {

	suspend fun getAll(): List<PinInfo>

	suspend fun insert(pinInfo: PinInfo)

	suspend fun delete(pinInfo: PinInfo)
}