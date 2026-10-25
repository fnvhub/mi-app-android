package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActionType
import com.example.data.ColorTheme
import com.example.data.IconStyle
import com.example.data.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Apagar Pantalla", appName)
  }

  @Test
  fun `preferences manager default values and updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    // Test updating preferences
    prefs.iconStyle = IconStyle.LOCK
    assertEquals(IconStyle.LOCK, prefs.iconStyle)

    prefs.colorTheme = ColorTheme.CYAN
    assertEquals(ColorTheme.CYAN, prefs.colorTheme)

    prefs.actionType = ActionType.LOCK_SCREEN
    assertEquals(ActionType.LOCK_SCREEN, prefs.actionType)

    prefs.vibrateOnPress = true
    assertTrue(prefs.vibrateOnPress)
  }
}
