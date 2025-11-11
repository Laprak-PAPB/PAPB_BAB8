package com.example.papb_bab8

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import android.app.Application
import androidx.lifecycle.AndroidViewModel

// 1. Data Class untuk menyimpan hasil lokasi
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "Menunggu lokasi..."
)

// 2. Sealed Class untuk State UI
sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: LocationData) : LocationState()
    data class Error(val message: String) : LocationState()
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val _locationState = mutableStateOf<LocationState>(LocationState.Idle)
    val locationState: State<LocationState> = _locationState


    private val _addressResult = mutableStateOf("Mencari alamat...")
    val addressResult: State<String> = _addressResult

    fun updateLocationStateToError(errorMessage: String) {
        _locationState.value = LocationState.Error(errorMessage)
    }



    fun startLocationFetch(fusedLocationClient: FusedLocationProviderClient) {
        if (_locationState.value == LocationState.Loading) return

        _locationState.value = LocationState.Loading


        fetchLastLocation(fusedLocationClient)
    }


    @Suppress("MissingPermission")
    private fun fetchLastLocation(fusedLocationClient: FusedLocationProviderClient) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Lokasi berhasil didapat
                val locationData = LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                _locationState.value = LocationState.Success(locationData)


                viewModelScope.launch {
                    val address = getAddressFromCoordinates(getApplication(), location)
                    _addressResult.value = address
                }

            } else {

                _locationState.value = LocationState.Error("Lokasi tidak ditemukan. Pastikan GPS aktif.")
            }
        }.addOnFailureListener { e ->
            _locationState.value = LocationState.Error("Gagal mengambil lokasi: ${e.message}")
        }
    }

    private suspend fun getAddressFromCoordinates(context: Context, location: Location): String =
        withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            return@withContext try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var addressResult = "Mencari alamat..."
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        addressResult = if (addresses.isNotEmpty()) {
                            addresses[0].getAddressLine(0) ?: "Alamat tidak dapat diurai"
                        } else {
                            "Alamat tidak ditemukan (Internet mungkin bermasalah)"
                        }
                    }
                    addressResult
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        addresses[0].getAddressLine(0) ?: "Alamat tidak dapat diurai"
                    } else {
                        "Alamat tidak ditemukan (Internet mungkin bermasalah)"
                    }
                }
            } catch (_: IOException) {
                "Layanan Geocoder tidak tersedia/Koneksi Internet putus."
            } catch (e: Exception) {
                "Kesalahan Geocoding: ${e.message}"
            }
        }
}
