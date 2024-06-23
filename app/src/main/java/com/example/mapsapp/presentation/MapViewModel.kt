package com.example.mapsapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapsapp.domain.entity.LatLonLocation
import com.example.mapsapp.domain.entity.PinInfo
import com.example.mapsapp.domain.usecase.DeletePinUseCase
import com.example.mapsapp.domain.usecase.GetLocationFlowUseCase
import com.example.mapsapp.domain.usecase.GetPinsUseCase
import com.example.mapsapp.domain.usecase.InsertPinUseCase
import com.example.mapsapp.domain.usecase.StartLocationPollingUseCase
import com.example.mapsapp.domain.usecase.StopLocationPollingUseCase
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MapViewModel(
	getLocationFlowUseCase: GetLocationFlowUseCase,
	private val startLocationPollingUseCase: StartLocationPollingUseCase,
	private val stopLocationPollingUseCase: StopLocationPollingUseCase,
	private val deletePinUseCase: DeletePinUseCase,
	private val insertPinUseCase: InsertPinUseCase,
	private val getPinsUseCase: GetPinsUseCase,
) : ViewModel() {

	private val requestPermissionEventChannel = Channel<Unit>()
	val requestPermissionEventFlow = requestPermissionEventChannel.receiveAsFlow()

	private val updatePinsEventChannel = Channel<List<PinInfo>>()
	val updatePinsEventFlow = updatePinsEventChannel.receiveAsFlow()

	private val initialUserPositionEventChannel = Channel<LatLonLocation?>()
	val initialUserPositionEventFlow = initialUserPositionEventChannel.receiveAsFlow()

	private val _state = MutableStateFlow<MapScreenState>(MapScreenState.Initial)
	val state = _state.asStateFlow()

	private val pinInfoMap = mutableMapOf<LatLonLocation, PinInfo>()

	init {
		getLocationFlowUseCase().drop(1).onEach {
			val state = _state.value

			_state.value = if (state is MapScreenState.Content) {
				state.copy(location = it)
			} else {
				initialUserPositionEventChannel.trySend(it)
				MapScreenState.Content(it, null)
			}
		}.launchIn(viewModelScope)

		viewModelScope.launch {
			val pins = getPinsUseCase()
			pinInfoMap.putAll(pins.associateBy { it.location })
			updatePinsEventChannel.send(pins)
		}
	}

	fun start() {
		if (_state.value !is MapScreenState.Initial) {
			return
		}

		_state.value = MapScreenState.Loading
	}

	fun handleLocationPermission(locationPermissionGranted: Boolean) {
		if (locationPermissionGranted) {
			startLocationPollingUseCase()
		} else {
			if (_state.value !is MapScreenState.Content) {
				_state.value = MapScreenState.Content(null, null)
			}
		}
	}

	fun removePin(pin: PlacemarkMapObject) {
		viewModelScope.launch {
			val latLonLocation = pin.geometry.toLatLonLocation()
			deletePinUseCase(pinInfoMap.getValue(latLonLocation))
			pinInfoMap.remove(latLonLocation)
		}
	}

	fun handlePin(pin: PlacemarkMapObject, name: String?) {
		viewModelScope.launch {
			val latLonLocation = pin.geometry.toLatLonLocation()
			val pinInfo = PinInfo(name, latLonLocation)

			insertPinUseCase(pinInfo)
			pinInfoMap[latLonLocation] = pinInfo
		}
	}

	fun handleRoute(route: List<PolylineMapObject>, destinationPoint: Point) {
		val contentState = _state.value as? MapScreenState.Content ?: return

		_state.value = contentState.copy(
			routeInfo = RouteInfo(
				route = route,
				destination = destinationPoint.toLatLonLocation()
			)
		)
	}

	fun startPolling() {
		startLocationPollingUseCase()
	}

	fun stopPolling() {
		stopLocationPollingUseCase()
	}
}