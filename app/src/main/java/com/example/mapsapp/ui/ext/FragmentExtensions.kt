package com.example.mapsapp.ui.ext

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.mapsapp.R
import com.example.mapsapp.presentation.MapScreenState
import com.example.mapsapp.presentation.MapViewModel
import com.example.mapsapp.ui.MyDialogFragment
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.runtime.Error

private const val DIALOG_TAG = "DIALOG_TAG"

internal fun Fragment.showExplainDialog(negativeButtonListener: () -> Unit) {
	AlertDialog.Builder(context)
		.setTitle(R.string.geo_location_request)
		.setMessage(R.string.geo_location_request_message)
		.setPositiveButton(R.string.open_settings) { _, _ ->
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			val uri = Uri.fromParts("package", requireContext().packageName, null)
			intent.setData(uri)
			startActivity(intent)
		}
		.setNegativeButton(R.string.pin_negative_button) { _, _ -> negativeButtonListener() }
		.setCancelable(false)
		.show()
}

internal fun Fragment.showNoGeolocationDialog(listener: () -> Unit) {
	AlertDialog.Builder(context)
		.setTitle(R.string.geo_location_no_permission)
		.setMessage(R.string.geo_location_cant_open_map)
		.setPositiveButton(R.string.pin_positive_button) { _, _ -> listener() }
		.setNegativeButton(R.string.open_settings) { _, _ ->
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			val uri = Uri.fromParts("package", requireContext().packageName, null)
			intent.setData(uri)
			startActivity(intent)
		}
		.setCancelable(false)
		.show()
}

internal fun Fragment.setupPinActionDialog(
	viewModel: MapViewModel,
	map: Map,
	pin: PlacemarkMapObject,
	tapListener: MapObjectTapListener,
	setupDrivingSession: () -> Unit,
): DrivingRouteListener {
	val listener = object : DrivingRouteListener {
		override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
			val contentState = viewModel.state.value as? MapScreenState.Content ?: return

			contentState.routeInfo?.route?.forEach {
				map.mapObjects.remove(it)
			}

			val polylineMapObjectList = mutableListOf<PolylineMapObject>()
			routes.forEach { route ->
				map.mapObjects.addPolyline(route.geometry).also { mapObject ->
					polylineMapObjectList.add(mapObject)
				}
			}

			viewModel.handleRoute(polylineMapObjectList, pin.geometry)
		}

		override fun onDrivingRoutesError(p0: Error) {
			Toast.makeText(requireContext(), R.string.geo_location_something_went_wrong, Toast.LENGTH_SHORT).show()
		}
	}

	showPinActionDialog(viewModel, map, pin, setupDrivingSession, tapListener)

	return listener
}

private fun Fragment.showPinActionDialog(
	viewModel: MapViewModel,
	map: Map,
	pin: PlacemarkMapObject,
	setupDrivingSession: () -> Unit,
	tapListener: MapObjectTapListener,
) {
	val alertDialogCreator = {
		AlertDialog.Builder(context)
			.setMessage(R.string.choose_action)
			.setPositiveButton(R.string.build_a_route) { _, _ ->
				setupDrivingSession()
			}
			.setNegativeButton(R.string.delete) { _, _ ->
				val contentState = viewModel.state.value as? MapScreenState.Content ?: return@setNegativeButton

				if (pin.compare(contentState.routeInfo?.destination)) {
					contentState.routeInfo?.route?.forEach {
						map.mapObjects.remove(it)
					}
				}

				pin.removeTapListener(tapListener)
				map.mapObjects.remove(pin)
				viewModel.removePin(pin)
			}.create()
	}

	showSafe(MyDialogFragment(alertDialogCreator))
}

internal fun Fragment.showSafe(dialog: DialogFragment) {
	if (childFragmentManager.findFragmentByTag(DIALOG_TAG) == null) {
		dialog.show(childFragmentManager, DIALOG_TAG)
	}
}

internal fun Fragment.isLocationPermissionGranted(): Boolean =
	ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
		ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED