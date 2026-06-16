package com.example.model

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class AlertParameter(val displayName: String, val iconName: String, val defaultThreshold: Double, val unit: String) {
    SNOWFALL("Snowfall Alert", "snowing", 1.0, ""),
    HIGH_WINDS("High Winds", "air", 50.0, "km/h"),
    STORMS("Storm Danger", "thunderstorm", 1.0, ""),
    EXTREME_COLD("Extreme Cold", "ac_unit", -20.0, "°C")
}

data class WeatherAlert(
    val id: String,
    val mountainName: String,
    val parameter: AlertParameter,
    val thresholdValue: Double,
    val isEnabled: Boolean = true,
    val isTriggered: Boolean = false,
    val lastTriggerDetail: String? = null,
    val lastTriggeredTime: Long = 0
)

class WeatherAlertRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("weather_alerts_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, WeatherAlert::class.java)
    private val adapter = moshi.adapter<List<WeatherAlert>>(listType)

    fun getAlerts(): List<WeatherAlert> {
        val json = prefs.getString("alerts_list", null) ?: return getDefaultAlerts()
        return try {
            adapter.fromJson(json) ?: getDefaultAlerts()
        } catch (e: Exception) {
            getDefaultAlerts()
        }
    }

    fun saveAlerts(alerts: List<WeatherAlert>) {
        val json = adapter.toJson(alerts)
        prefs.edit().putString("alerts_list", json).apply()
    }

    fun addAlert(alert: WeatherAlert) {
        val current = getAlerts().toMutableList()
        current.add(alert)
        saveAlerts(current)
    }

    fun removeAlert(id: String) {
        val current = getAlerts().filter { it.id != id }
        saveAlerts(current)
    }

    fun updateAlert(updated: WeatherAlert) {
        val current = getAlerts().map { if (it.id == updated.id) updated else it }
        saveAlerts(current)
    }

    private fun getDefaultAlerts(): List<WeatherAlert> {
        return listOf(
            WeatherAlert(
                id = "default_everest_wind",
                mountainName = "Mount Everest",
                parameter = AlertParameter.HIGH_WINDS,
                thresholdValue = 55.0,
                isEnabled = true,
                isTriggered = false
            ),
            WeatherAlert(
                id = "default_k2_storm",
                mountainName = "K2",
                parameter = AlertParameter.STORMS,
                thresholdValue = 1.0,
                isEnabled = true,
                isTriggered = false
            ),
            WeatherAlert(
                id = "default_denali_cold",
                mountainName = "Denali",
                parameter = AlertParameter.EXTREME_COLD,
                thresholdValue = -25.0,
                isEnabled = true,
                isTriggered = false
            )
        )
    }
}
