package com.example.busnew

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.busnew.ui.theme.BUSNEWTheme

class GoogleMapActivity : ComponentActivity() {
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize permission request launcher
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    getCurrentLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    getCurrentLocation()
                }
                else -> {
                    // No location access granted.
                    showPermissionDeniedDialog()
                }
            }
        }

        setContent {
            BUSNEWTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationPermissionComponent()
                }
            }
        }
    }

    @Composable
    fun LocationPermissionComponent() {
        val permissionStatus = remember { mutableStateOf("Request Location Permission") }

        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = {
                    if (isLocationPermissionGranted()) {
                        getCurrentLocation()
                    } else {
                        requestLocationPermission()
                    }
                }
            ) {
                Text(text = permissionStatus.value)
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Location access is required for this feature.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun getCurrentLocation() {
        // Handle location retrieval here.
        // This is where you'd integrate with the location APIs to fetch the user's location.
    }
}
