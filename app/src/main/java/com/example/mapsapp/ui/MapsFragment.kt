package com.example.mapsapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mapsapp.R
import com.example.mapsapp.databinding.MapsFragmentBinding
import com.example.mapsapp.domain.entity.LatLonLocation
import com.example.mapsapp.presentation.MapScreenState
import com.example.mapsapp.presentation.MapViewModel
import com.example.mapsapp.presentation.toPoint
import com.example.mapsapp.ui.ext.isLocationPermissionGranted
import com.example.mapsapp.ui.ext.setupPinActionDialog
import com.example.mapsapp.ui.ext.showExplainDialog
import com.example.mapsapp.ui.ext.showNoGeolocationDialog
import com.example.mapsapp.ui.ext.showSafe
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.VehicleType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.runtime.image.ImageProvider

class MapsFragment : Fragment() {

	private companion object {

		val PIN_TEXT_STYLE = TextStyle().apply { placement = TextStyle.Placement.TOP }
	}

	private lateinit var binding: MapsFragmentBinding
	private lateinit var inputListener: InputListener
	private lateinit var drivingRouteListener: DrivingRouteListener
	private lateinit var drivingSession: DrivingSession
	private var userLocationPin: PlacemarkMapObject? = null
	private val tapListeners = mutableMapOf<PlacemarkMapObject, MapObjectTapListener>()

	private val viewModel: MapViewModel by viewModel()

	@SuppressLint("MissingPermission")
	private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
		if (permissionGranted) {
			viewModel.handleLocationPermission(true)
		} else {
			showNoGeolocationDialog {
				viewModel.handleLocationPermission(false)
			}
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = MapsFragmentBinding.inflate(layoutInflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		observeViewModel()
		MapKitFactory.initialize(requireContext())
		binding.mapView.map.setupListeners()

		requestLocationPermission()
	}

	private fun observeViewModel() {
		viewModel.state.onEach {
			when (it) {
				MapScreenState.Initial,
				MapScreenState.Loading    -> renderLoading()

				is MapScreenState.Content -> renderContent(it.location)
			}
		}.launchIn(viewLifecycleOwner.lifecycleScope)

		viewModel.requestPermissionEventFlow.onEach {
			requestLocationPermission()
		}.launchIn(viewLifecycleOwner.lifecycleScope)

		viewModel.initialUserPositionEventFlow.onEach {
			setUserLocation(it)
		}.launchIn(viewLifecycleOwner.lifecycleScope)

		viewModel.updatePinsEventFlow.onEach { pinList ->
			pinList.forEach {
				createPin(it.location.toPoint(), R.drawable.pin, it.name)
			}
		}.launchIn(viewLifecycleOwner.lifecycleScope)
	}

	private fun renderContent(location: LatLonLocation?) {
		if (location != null) {
			if (userLocationPin == null) {
				userLocationPin = createPin(location.toPoint(), R.drawable.user_location)
			}

			userLocationPin?.geometry = location.toPoint()
		}

		binding.mapView.isVisible = true
		binding.progressBar.isVisible = false
	}

	private fun Map.setupListeners() {
		inputListener = object : InputListener {
			override fun onMapTap(map: Map, point: Point) {}

			override fun onMapLongTap(map: Map, point: Point) {
				val pin = createPin(point, R.drawable.pin)
				showNewPinDialog(pin)
			}
		}

		addInputListener(inputListener)
	}

	private fun createPin(point: Point, drawableRes: Int, text: String? = null): PlacemarkMapObject {
		val map = binding.mapView.map
		val imageProvider = ImageProvider.fromResource(requireContext(), drawableRes)

		return map.mapObjects.addPlacemark().apply {
			geometry = point
			setIcon(imageProvider, IconStyle().setScale(0.1f))
			text?.let { setText(it, PIN_TEXT_STYLE) }
			addTapListener(getPinTapListener(this))
		}
	}

	private fun getPinTapListener(pin: PlacemarkMapObject): MapObjectTapListener =
		MapObjectTapListener { _, point ->
			drivingRouteListener = setupPinActionDialog(viewModel, binding.mapView.map, pin, tapListeners.getValue(pin)) {
				val userLocation = (viewModel.state.value as? MapScreenState.Content)?.location ?: run {
					Toast.makeText(requireContext(), R.string.no_geo_location_cant_build_route, Toast.LENGTH_SHORT).show()
					return@setupPinActionDialog
				}

				val userPoint = userLocation.toPoint()

				val points = mutableListOf(
					RequestPoint(userPoint, RequestPointType.WAYPOINT, null, null),
					RequestPoint(point, RequestPointType.WAYPOINT, null, null)
				)

				val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
				val drivingOptions = DrivingOptions().apply { routesCount = 2 }
				val vehicleOptions = VehicleOptions().apply { vehicleType = VehicleType.DEFAULT }
				drivingSession = drivingRouter.requestRoutes(points, drivingOptions, vehicleOptions, drivingRouteListener)
			}

			true
		}.also { tapListeners[pin] = it }

	private fun showNewPinDialog(pin: PlacemarkMapObject) {
		val listener = object : PinNameDialogFragment.NoticeDialogListener {
			override fun onPositiveClick(pinName: String) {
				pin.setText(pinName, PIN_TEXT_STYLE)
				viewModel.handlePin(pin, pinName)
			}

			override fun onNegativeClick() {
				viewModel.handlePin(pin, null)
			}
		}

		showSafe(PinNameDialogFragment(listener))
	}

	private fun renderLoading() {
		binding.mapView.isVisible = false
		binding.progressBar.isVisible = true
	}

	private fun requestLocationPermission() {
		when {
			isLocationPermissionGranted()                                                          -> {
				viewModel.handleLocationPermission(true)
			}

			shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
				showExplainDialog {
					viewModel.handleLocationPermission(false)
				}
			}

			else                                                                                   -> {
				locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
			}
		}
	}

	private fun setUserLocation(location: LatLonLocation?) {
		if (location == null) {
			Toast.makeText(requireContext(), R.string.geo_location_no_permission, Toast.LENGTH_SHORT).show()
			return
		}

		binding.mapView.map.move(
			CameraPosition(location.toPoint(), 17.0f, 0f, 0f)
		)
	}

	override fun onResume() {
		super.onResume()

		if (viewModel.state.value is MapScreenState.Loading) {
			viewModel.handleLocationPermission(isLocationPermissionGranted())
		}
		viewModel.start()
	}

	override fun onStart() {
		super.onStart()
		MapKitFactory.getInstance().onStart()
		binding.mapView.onStart()
		viewModel.startPolling()
	}

	override fun onStop() {
		binding.mapView.onStop()
		MapKitFactory.getInstance().onStop()
		viewModel.stopPolling()
		super.onStop()
	}
}