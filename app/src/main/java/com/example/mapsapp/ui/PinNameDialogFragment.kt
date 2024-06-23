package com.example.mapsapp.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mapsapp.databinding.PinNameDialogBinding

class PinNameDialogFragment(private val listener: NoticeDialogListener) : DialogFragment() {

	interface NoticeDialogListener {

		fun onPositiveClick(pinName: String)

		fun onNegativeClick()
	}

	private lateinit var binding: PinNameDialogBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = PinNameDialogBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.setCancelable(false)
		dialog?.setCanceledOnTouchOutside(false)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

		binding.positiveButton.setOnClickListener {
			listener.onPositiveClick(binding.pinNameInput.text.toString())
			dismiss()
		}

		binding.negativeButton.setOnClickListener {
			listener.onNegativeClick()
			dismiss()
		}
	}
}