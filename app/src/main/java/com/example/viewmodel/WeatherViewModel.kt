package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.WeatherAlert
import com.example.model.WeatherAlertRepository
import com.example.model.AlertNotificationManager
import com.example.model.AlertParameter
import com.example.api.RetrofitClient
import com.example.model.Mountain
import com.example.model.MountainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow

data class ElevationWeather(
    val elevationMeters: Int,
    val name: String, // "Base Camp", "High Camp", "Summit"
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double, // Wind chill
    val windSpeedKmh: Double,
    val pressureHpa: Double,
    val oxygenPercentage: Int, // Relative density %
    val weatherCode: Int
)

data class DailyWeatherDay(
    val dateLabel: String,
    val tempMax: Double,
    val tempMin: Double,
    val weatherCode: Int,
    val windSpeedMaxKmh: Double,
    val uvIndexMax: Double
)

data class HourlyWeatherHour(
    val hourLabel: String,
    val temp: Double,
    val weatherCode: Int
)

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val elevations: List<ElevationWeather>,
        val daily: List<DailyWeatherDay>,
        val hourly: List<HourlyWeatherHour>,
        val isSimulated: Boolean
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val alertRepository = WeatherAlertRepository(application)
    
    private val _alerts = MutableStateFlow<List<WeatherAlert>>(emptyList())
    val alerts: StateFlow<List<WeatherAlert>> = _alerts.asStateFlow()

    private val _selectedMountain = MutableStateFlow<Mountain>(MountainRepository.mountains.first())
    val selectedMountain: StateFlow<Mountain> = _selectedMountain.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        _alerts.value = alertRepository.getAlerts()
        loadForecastForMountain(MountainRepository.mountains.first())
    }

    fun selectMountain(mountain: Mountain) {
        _selectedMountain.value = mountain
        loadForecastForMountain(mountain)
    }

    private fun loadForecastForMountain(mountain: Mountain) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val response = RetrofitClient.api.getForecast(
                    latitude = mountain.latitude,
                    longitude = mountain.longitude
                )

                val hourly = response.hourly
                val daily = response.daily

                if (hourly != null && daily != null) {
                    // Extract base current temperature from early index (usually index 0 or current hour)
                    val baseTemp = hourly.temperature2m.firstOrNull() ?: 5.0
                    val baseWind = hourly.windSpeed10m.firstOrNull() ?: 12.0
                    val baseCode = hourly.weatherCode.firstOrNull() ?: 0

                    val elevationsList = calculateElevations(mountain, baseTemp, baseWind, baseCode)

                    // Map Daily
                    val dailyDays = mutableListOf<DailyWeatherDay>()
                    val dailyCount = daily.time.size
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("EEE dd", Locale.getDefault())

                    for (i in 0 until dailyCount) {
                        val originalDateStr = daily.time[i]
                        val formattedDate = try {
                            val parsedDate = inputFormat.parse(originalDateStr)
                            parsedDate?.let { outputFormat.format(it) } ?: originalDateStr
                        } catch (e: Exception) {
                            originalDateStr
                        }

                        dailyDays.add(
                            DailyWeatherDay(
                                dateLabel = formattedDate,
                                tempMax = daily.temperature2mMax[i],
                                tempMin = daily.temperature2mMin[i],
                                weatherCode = daily.weatherCode[i],
                                windSpeedMaxKmh = daily.windSpeed10mMax?.getOrNull(i) ?: 15.0,
                                uvIndexMax = daily.uvIndexMax?.getOrNull(i) ?: 2.0
                            )
                        )
                    }

                    // Map Hourly (first 24h)
                    val hourlyHours = mutableListOf<HourlyWeatherHour>()
                    val hourlyCount = minOf(hourly.time.size, 24)
                    val hourInputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                    val hourOutputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    for (i in 0 until hourlyCount) {
                        val rawTimeStr = hourly.time[i]
                        val formattedHour = try {
                            val parsedDate = hourInputFormat.parse(rawTimeStr)
                            parsedDate?.let { hourOutputFormat.format(it) } ?: rawTimeStr.substringAfter("T")
                        } catch (e: Exception) {
                            rawTimeStr.substringAfter("T")
                        }

                        hourlyHours.add(
                            HourlyWeatherHour(
                                hourLabel = formattedHour,
                                temp = hourly.temperature2m[i],
                                weatherCode = hourly.weatherCode[i]
                            )
                        )
                    }

                    _uiState.value = WeatherUiState.Success(
                        elevations = elevationsList,
                        daily = dailyDays,
                        hourly = hourlyHours,
                        isSimulated = false
                    )
                    checkAndUpdateAlerts(mountain, elevationsList)
                } else {
                    generateSimulatedWeather(mountain)
                }

            } catch (e: Exception) {
                // Recover using simulated science-based meteorological model
                generateSimulatedWeather(mountain)
            }
        }
    }

    private fun generateSimulatedWeather(mountain: Mountain) {
        // Fallback generator incorporating alpine physics, latitude, and month seasons
        val month = Calendar.getInstance().get(Calendar.MONTH) // 0 - 11
        val isNorthernHemisphere = mountain.latitude >= 0
        val isSummer = if (isNorthernHemisphere) {
            month in 5..8 // Jun-Sep is standard summer
        } else {
            month in 11..2 || month == 0 // Dec-Mar is southern summer
        }

        // Base Temperature calculation at sea level standard
        val latitudeFactor = (90.0 - kotlin.math.abs(mountain.latitude)) / 90.0
        // equatorial standard = 30C, polar standard = -10C.
        val baseSeaLevelTemp = -10.0 + (latitudeFactor * 40.0) + (if (isSummer) 6.0 else -6.0)

        // Standard temperature lapse rate is 6.5C decrease per 1000m elevation.
        // Base Camp is typically situated at about 40% of the prominence or elevation of the mountain, or a standard height.
        // Let's assume standard meteorological base temperature at the lowest weather station:
        val baseStationElevation = (mountain.elevationMeters * 0.4).toInt()
        val baseTemp = baseSeaLevelTemp - 0.0065 * baseStationElevation

        // Generate base wind speeds based on latitudes / geographic ranges
        val baseWind = when (mountain.range) {
            "Himalayas", "Karakoram" -> 20.0 + (if (!isSummer) 25.0 else 0.0) // severe winter jetstream
            "Alaska Range" -> 25.0 + (if (!isSummer) 20.0 else 0.0)
            else -> 12.0 + (if (!isSummer) 10.0 else 0.0)
        }

        val baseWeatherCode = when {
            baseTemp < -5 && !isSummer -> 73 // Moderate Snow
            baseTemp < 5 && baseWind > 30 -> 85 // Snow showers
            baseWind > 40 -> 95 // Stormy
            baseSeaLevelTemp > 18 -> 1 // Sunny/mainly clear
            else -> 3 // Partly cloudy/overcast
        }

        val elevationsList = calculateElevations(mountain, baseTemp, baseWind, baseWeatherCode)

        // Generate Mock Daily Forecast (7 days)
        val dailyDays = mutableListOf<DailyWeatherDay>()
        val calendar = Calendar.getInstance()
        val dayFormatter = SimpleDateFormat("EEE dd", Locale.getDefault())

        val baseCodes = listOf(1, 1, 3, 71, 73, 3, 0)
        for (i in 0 until 7) {
            val dateLabel = dayFormatter.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val dailyOffset = if (i == 3 || i == 4) -4.0 else (i % 3) * 1.5 - 1.0
            val maxT = baseTemp + 4.0 + dailyOffset
            val minT = baseTemp - 5.0 + dailyOffset
            val code = if (maxT < 0) 73 else baseCodes[i % baseCodes.size]

            dailyDays.add(
                DailyWeatherDay(
                    dateLabel = dateLabel,
                    tempMax = maxT,
                    tempMin = minT,
                    weatherCode = code,
                    windSpeedMaxKmh = baseWind * 1.2 + (i % 2) * 5,
                    uvIndexMax = if (isSummer) 8.0 - (i % 3) else 4.0 - (i % 2)
                )
            )
        }

        // Generate Mock Hourly Forecast (24h)
        val hourlyHours = mutableListOf<HourlyWeatherHour>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        for (i in 0 until 24) {
            val h = (currentHour + i) % 24
            val hourLabel = String.format(Locale.getDefault(), "%02d:00", h)
            
            // Diurnal temperature fluctuation
            val hourRad = (h - 14) * (2 * Math.PI / 24)
            val tempVal = baseTemp + Math.cos(hourRad) * 4.0

            val code = if (tempVal < 0) 71 else baseWeatherCode

            hourlyHours.add(
                HourlyWeatherHour(
                    hourLabel = hourLabel,
                    temp = tempVal,
                    weatherCode = code
                )
            )
        }

        _uiState.value = WeatherUiState.Success(
            elevations = elevationsList,
            daily = dailyDays,
            hourly = hourlyHours,
            isSimulated = true
        )
        checkAndUpdateAlerts(mountain, elevationsList)
    }

    private fun calculateElevations(
        mountain: Mountain,
        baseTempCelsius: Double,
        baseWindKmh: Double,
        baseCode: Int
    ): List<ElevationWeather> {
        // We divide into 3 Zones:
        // 1. Valley / Base Camp (40% of Peak Elevation)
        // 2. High Camp (75% of Peak Elevation)
        // 3. Summit (100% of Peak Elevation)

        val heightSummit = mountain.elevationMeters
        val heightHighCamp = (mountain.elevationMeters * 0.75).toInt()
        val heightBase = (mountain.elevationMeters * 0.40).toInt()

        return listOf(
            createZoneWeather("Base Camp", heightBase, heightBase, baseTempCelsius, baseWindKmh, baseCode, 1.0),
            createZoneWeather("High Camp", heightHighCamp, heightBase, baseTempCelsius, baseWindKmh, baseCode, 1.6),
            createZoneWeather("Summit", heightSummit, heightBase, baseTempCelsius, baseWindKmh, baseCode, 2.3)
        )
    }

    private fun createZoneWeather(
        name: String,
        targetHeightMeters: Int,
        baseHeightMeters: Int,
        baseTempCelsius: Double,
        baseWindKmh: Double,
        baseCode: Int,
        windMultiplier: Double
    ): ElevationWeather {
        // Temperature drop via standard lapse rate: 0.0065 C per meter
        val deltaHeight = targetHeightMeters - baseHeightMeters
        val zoneTemp = baseTempCelsius - (0.0065 * deltaHeight)

        // Wind Speed multiplies with severe gusts at summits due to thinner atmospheric boundary layers
        val zoneWind = baseWindKmh * windMultiplier

        // Barometric pressure formula for elevations (Standard pressure standard is 1013.25 hPa)
        val pressure = 1013.25 * (1.0 - 2.25577e-5 * targetHeightMeters).pow(5.25588)

        // Oxygen density drops relative to sea level density (which is 100%)
        val oxygenDensityPct = ((pressure / 1013.25) * 100).toInt().coerceIn(10, 100)

        // Calculate Wind Chill (apparent temperature felt by human skin)
        // Formula valid for temperatures <= 10C and winds >= 4.8km/h
        val windChill = if (zoneTemp <= 10.0 && zoneWind >= 4.8) {
            13.12 + 0.6215 * zoneTemp - 11.37 * zoneWind.pow(0.16) + 0.3965 * zoneTemp * zoneWind.pow(0.16)
        } else {
            zoneTemp
        }

        // Adjust weather code to colder variants at summit (e.g. rain at base becomes snow / blizzard at summit)
        val adjustedCode = if (zoneTemp <= 0) {
            when (baseCode) {
                51, 53, 55, 61, 63 -> 71 // Light snow
                65, 80, 81, 82 -> 75    // Heavy snow
                95, 96, 99 -> 86        // Torrential snow blizzard with storms
                else -> baseCode
            }
        } else {
            baseCode
        }

        return ElevationWeather(
            elevationMeters = targetHeightMeters,
            name = name,
            temperatureCelsius = zoneTemp,
            apparentTemperatureCelsius = windChill,
            windSpeedKmh = zoneWind,
            pressureHpa = pressure,
            oxygenPercentage = oxygenDensityPct,
            weatherCode = adjustedCode
        )
    }

    fun getAlerts() {
        _alerts.value = alertRepository.getAlerts()
    }

    fun addAlert(mountainName: String, parameter: AlertParameter, threshold: Double) {
        val alert = WeatherAlert(
            id = java.util.UUID.randomUUID().toString(),
            mountainName = mountainName,
            parameter = parameter,
            thresholdValue = threshold,
            isEnabled = true,
            isTriggered = false
        )
        alertRepository.addAlert(alert)
        _alerts.value = alertRepository.getAlerts()
        
        // Check newly added alert criteria immediately
        if (mountainName.equals(_selectedMountain.value.name, ignoreCase = true)) {
            val currentState = _uiState.value
            if (currentState is WeatherUiState.Success) {
                checkAndUpdateAlerts(_selectedMountain.value, currentState.elevations)
            }
        }
    }

    fun toggleAlertEnabled(id: String) {
        val alertsList = alertRepository.getAlerts()
        val found = alertsList.find { it.id == id }
        if (found != null) {
            val updated = found.copy(
                isEnabled = !found.isEnabled,
                isTriggered = if (found.isEnabled) false else found.isTriggered
            )
            alertRepository.updateAlert(updated)
            _alerts.value = alertRepository.getAlerts()
            
            // Check immediately on enable
            if (updated.isEnabled && updated.mountainName.equals(_selectedMountain.value.name, ignoreCase = true)) {
                val currentState = _uiState.value
                if (currentState is WeatherUiState.Success) {
                    checkAndUpdateAlerts(_selectedMountain.value, currentState.elevations)
                }
            }
        }
    }

    fun deleteAlert(id: String) {
        alertRepository.removeAlert(id)
        _alerts.value = alertRepository.getAlerts()
    }

    fun testTriggerAlert(alert: WeatherAlert) {
        val context = getApplication<Application>().applicationContext
        val title = "🚨 WEATHER ALERT: ${alert.mountainName}"
        val content = "Demo Breach: ${alert.parameter.displayName} condition met criteria."
        AlertNotificationManager.sendNotification(context, title, content)
        
        val updated = alert.copy(
            isTriggered = true,
            lastTriggerDetail = "Triggered via manual telemetry scan on user request.",
            lastTriggeredTime = System.currentTimeMillis()
        )
        alertRepository.updateAlert(updated)
        _alerts.value = alertRepository.getAlerts()
    }

    fun checkAndUpdateAlerts(mountain: Mountain, elevations: List<ElevationWeather>) {
        val context = getApplication<Application>().applicationContext
        val currentAlerts = alertRepository.getAlerts()
        
        val updatedAlerts = currentAlerts.map { alert ->
            if (alert.isEnabled && alert.mountainName.equals(mountain.name, ignoreCase = true)) {
                var shouldTrigger = false
                var details = ""
                
                when (alert.parameter) {
                    AlertParameter.HIGH_WINDS -> {
                        val maxWindElev = elevations.maxByOrNull { it.windSpeedKmh }
                        if (maxWindElev != null && maxWindElev.windSpeedKmh >= alert.thresholdValue) {
                            shouldTrigger = true
                            details = "Gusts of ${String.format("%.1f", maxWindElev.windSpeedKmh)} km/h recorded at ${maxWindElev.name} zone."
                        }
                    }
                    AlertParameter.EXTREME_COLD -> {
                        val minTempElev = elevations.minByOrNull { it.apparentTemperatureCelsius }
                        if (minTempElev != null && minTempElev.apparentTemperatureCelsius <= alert.thresholdValue) {
                            shouldTrigger = true
                            details = "Freezing ice chill of ${String.format("%.1f", minTempElev.apparentTemperatureCelsius)}°C felt at ${minTempElev.name} zone."
                        }
                    }
                    AlertParameter.SNOWFALL -> {
                        val snowElev = elevations.find { it.weatherCode in listOf(71, 73, 75, 77, 85, 86) }
                        if (snowElev != null) {
                            shouldTrigger = true
                            details = "Active snowstorm blizzard reported at ${snowElev.name} level."
                        }
                    }
                    AlertParameter.STORMS -> {
                        val stormElev = elevations.find { it.weatherCode in listOf(95, 96, 99) }
                        if (stormElev != null) {
                            shouldTrigger = true
                            details = "High hazard lightning and storm front at ${stormElev.name} level."
                        }
                    }
                }
                
                if (shouldTrigger && !alert.isTriggered) {
                    val title = "🚨 WEATHER ALERT: ${alert.mountainName}"
                    val content = "Threshold breached: $details"
                    AlertNotificationManager.sendNotification(context, title, content)
                    
                    alert.copy(
                        isTriggered = true,
                        lastTriggerDetail = details,
                        lastTriggeredTime = System.currentTimeMillis()
                    )
                } else if (!shouldTrigger && alert.isTriggered) {
                    alert.copy(isTriggered = false)
                } else {
                    alert
                }
            } else {
                alert
            }
        }
        
        alertRepository.saveAlerts(updatedAlerts)
        _alerts.value = updatedAlerts
    }
}
