package com.example.mapsapp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class MyDialogFragment(private val alertDialogCreator: () -> AlertDialog) : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return alertDialogCreator()
	}
}
