package com.toutakun04.dayline.weather

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

enum class WeatherIconType {
    Sun,
    Moon,
    Sunrise,
    Sunset,
    Cloud,
    Rain,
    Storm,
    Snow,
    LocationOff
}

data class WeatherDisplay(
    val location: String,
    val temperature: String,
    val icon: WeatherIconType
)

object WeatherRepository {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun loadWeatherDisplay(context: Context): WeatherDisplay {
        if (!hasLocationPermission(context)) {
            return WeatherDisplay("Location off", "--", WeatherIconType.LocationOff)
        }

        val location = getCurrentLocation(context) ?: return WeatherDisplay("Location unavailable", "--", WeatherIconType.LocationOff)
        val current = fetchCurrentWeather(location) ?: return WeatherDisplay("Weather unavailable", "--", WeatherIconType.Cloud)
        val locationName = reverseGeocode(context, location)
        val icon = mapIcon(current.weatherCode, current.isDay == 1, LocalTime.now())
        val temperature = "${current.temperature.roundToInt()}\u00B0C"

        return WeatherDisplay(locationName, temperature, icon)
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        val lastKnown = providers
            .filter { manager.isProviderEnabled(it) }
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

        if (lastKnown != null) return lastKnown

        val provider = providers.firstOrNull { manager.isProviderEnabled(it) } ?: return null
        return suspendCancellableCoroutine { cont ->
            val cancellationSignal = CancellationSignal()
            cont.invokeOnCancellation { cancellationSignal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    manager,
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    cont.resume(location)
                }
            } catch (_: SecurityException) {
                cont.resume(null)
            }
        }
    }

    private suspend fun reverseGeocode(context: Context, location: Location): String {
        return withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val first = results?.firstOrNull()
                val city = first?.locality ?: first?.subAdminArea ?: first?.adminArea
                val admin = first?.adminArea
                when {
                    city != null && admin != null && city != admin -> "$city, $admin"
                    city != null -> city
                    admin != null -> admin
                    else -> "Your area"
                }
            }.getOrElse { "Your area" }
        }
    }

    private suspend fun fetchCurrentWeather(location: Location): CurrentWeather? {
        return runCatching {
            val url = buildString {
                append("https://api.open-meteo.com/v1/forecast")
                append("?latitude=${location.latitude}")
                append("&longitude=${location.longitude}")
                append("&current=temperature_2m,weather_code,is_day")
            append("&temperature_unit=celsius")
                append("&timezone=auto")
            }
            val body = fetchText(url)
            json.decodeFromString(CurrentWeatherResponse.serializer(), body).current
        }.getOrNull()
    }

    private suspend fun fetchText(url: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            try {
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun mapIcon(code: Int, isDay: Boolean, time: LocalTime): WeatherIconType {
        val isMorning = time.hour in 5..8
        val isEvening = time.hour in 17..19
        return when (code) {
            0 -> {
                if (isDay) {
                    if (isMorning) WeatherIconType.Sunrise else if (isEvening) WeatherIconType.Sunset else WeatherIconType.Sun
                } else {
                    WeatherIconType.Moon
                }
            }
            1, 2, 3, 45, 48 -> WeatherIconType.Cloud
            51, 53, 55, 56, 57,
            61, 63, 65, 66, 67,
            80, 81, 82 -> WeatherIconType.Rain
            71, 73, 75, 77, 85, 86 -> WeatherIconType.Snow
            95, 96, 99 -> WeatherIconType.Storm
            else -> WeatherIconType.Cloud
        }
    }
}

@Serializable
data class CurrentWeatherResponse(
    val current: CurrentWeather? = null
)

@Serializable
data class CurrentWeather(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int
)
