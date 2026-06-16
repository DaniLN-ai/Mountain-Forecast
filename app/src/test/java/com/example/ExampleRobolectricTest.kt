package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mountain Weather", appName)
  }

  @Test
  fun `verify WeatherAlertRepository default and CRUD operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.model.WeatherAlertRepository(context)

    // Load defaults on first initialization
    val originalAlerts = repository.getAlerts()
    assert(originalAlerts.isNotEmpty())
    
    // Test add behavior
    val newAlert = com.example.model.WeatherAlert(
        id = "test_custom_alert",
        mountainName = "Aconcagua",
        parameter = com.example.model.AlertParameter.HIGH_WINDS,
        thresholdValue = 75.0,
        isEnabled = true,
        isTriggered = false
    )
    repository.addAlert(newAlert)
    
    val afterAdd = repository.getAlerts()
    assert(afterAdd.any { it.id == "test_custom_alert" && it.mountainName == "Aconcagua" })

    // Test update
    val updated = newAlert.copy(isEnabled = false, isTriggered = true)
    repository.updateAlert(updated)
    
    val afterUpdate = repository.getAlerts()
    val found = afterUpdate.find { it.id == "test_custom_alert" }
    assert(found != null)
    assertEquals(false, found?.isEnabled)
    assertEquals(true, found?.isTriggered)

    // Test remove behavior
    repository.removeAlert("test_custom_alert")
    val afterRemove = repository.getAlerts()
    assert(afterRemove.none { it.id == "test_custom_alert" })
  }
}
