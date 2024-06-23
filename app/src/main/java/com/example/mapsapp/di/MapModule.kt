package com.example.mapsapp.di

import androidx.room.Room
import com.example.mapsapp.data.repository.LocationRepositoryImpl
import com.example.mapsapp.data.database.PinDao
import com.example.mapsapp.data.database.PinDatabase
import com.example.mapsapp.data.repository.PinRepositoryImpl
import com.example.mapsapp.domain.repository.LocationRepository
import com.example.mapsapp.domain.repository.PinRepository
import com.example.mapsapp.domain.usecase.DeletePinUseCase
import com.example.mapsapp.domain.usecase.GetLocationFlowUseCase
import com.example.mapsapp.domain.usecase.GetPinsUseCase
import com.example.mapsapp.domain.usecase.InsertPinUseCase
import com.example.mapsapp.domain.usecase.StartLocationPollingUseCase
import com.example.mapsapp.domain.usecase.StopLocationPollingUseCase
import com.example.mapsapp.presentation.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
	single<LocationRepository> { LocationRepositoryImpl(get()) }
	factory<GetLocationFlowUseCase> { GetLocationFlowUseCase(get()) }
	factory<StopLocationPollingUseCase> { StopLocationPollingUseCase(get()) }
	factory<StartLocationPollingUseCase> { StartLocationPollingUseCase(get()) }

	single<PinDatabase> { Room.databaseBuilder(get(), PinDatabase::class.java, "PinDatabase").build() }
	factory<PinDao> { get<PinDatabase>().pinDao() }

	factory<PinRepository> { PinRepositoryImpl(get()) }
	factory<DeletePinUseCase> { DeletePinUseCase(get()) }
	factory<InsertPinUseCase> { InsertPinUseCase(get()) }
	factory<GetPinsUseCase> { GetPinsUseCase(get()) }

	viewModel { MapViewModel(get(), get(), get(), get(), get(), get()) }
}

