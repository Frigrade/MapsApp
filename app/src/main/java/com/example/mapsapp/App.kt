package com.example.mapsapp

import android.app.Application
import com.example.mapsapp.di.mapModule
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App: Application() {

	override fun onCreate() {
		super.onCreate()
		MapKitFactory.setApiKey("7189c83a-bfd1-491b-a2c3-7a425d347f29")

		startKoin {
			androidContext(this@App)
			modules(mapModule)
		}
	}
}