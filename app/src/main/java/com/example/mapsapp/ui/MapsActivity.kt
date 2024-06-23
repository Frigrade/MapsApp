package com.example.mapsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mapsapp.databinding.MapsActivityBinding


class MapsActivity : AppCompatActivity() {

    private lateinit var binding: MapsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MapsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}