package cz.cvut.fel.android_app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import cz.cvut.fel.android_app.domain.model.Location
import cz.cvut.fel.android_app.domain.repository.LocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [LocationRepository] backed by [FusedLocationProviderClient].
 * [observeLocation] uses high-accuracy updates.
 * [getCurrentLocation] reads [FusedLocationProviderClient.lastLocation].
 */
class AndroidLocationRepository(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<Location?> = callbackFlow {
        if (!hasLocationEnabled()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(Location(it.latitude, it.longitude, it.accuracy))
                }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationEnabled()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        client.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location?.let { Location(it.latitude, it.longitude, it.accuracy) })
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    private fun hasLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
