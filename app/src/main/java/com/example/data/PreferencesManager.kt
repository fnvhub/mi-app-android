package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class IconStyle {
    POWER,
    LOCK
}

enum class ColorTheme {
    RED,
    GREEN,
    CYAN,
    DARK
}

enum class ActionType {
    LOCK_SCREEN,
    POWER_DIALOG
}

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var iconStyle: IconStyle
        get() = try {
            IconStyle.valueOf(prefs.getString(KEY_ICON_STYLE, IconStyle.POWER.name) ?: IconStyle.POWER.name)
        } catch (e: Exception) {
            IconStyle.POWER
        }
        set(value) = prefs.edit().putString(KEY_ICON_STYLE, value.name).apply()

    var colorTheme: ColorTheme
        get() = try {
            ColorTheme.valueOf(prefs.getString(KEY_COLOR_THEME, ColorTheme.RED.name) ?: ColorTheme.RED.name)
        } catch (e: Exception) {
            ColorTheme.RED
        }
        set(value) = prefs.edit().putString(KEY_COLOR_THEME, value.name).apply()

    var actionType: ActionType
        get() = try {
            ActionType.valueOf(prefs.getString(KEY_ACTION_TYPE, ActionType.LOCK_SCREEN.name) ?: ActionType.LOCK_SCREEN.name)
        } catch (e: Exception) {
            ActionType.LOCK_SCREEN
        }
        set(value) = prefs.edit().putString(KEY_ACTION_TYPE, value.name).apply()

    var vibrateOnPress: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var floatingButtonEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_BUTTON, false)
        set(value) = prefs.edit().putBoolean(KEY_FLOATING_BUTTON, value).apply()

    var delaySeconds: Int
        get() = prefs.getInt(KEY_DELAY_SECONDS, 0)
        set(value) = prefs.edit().putInt(KEY_DELAY_SECONDS, value).apply()

    companion object {
        private const val PREFS_NAME = "power_off_app_preferences"
        private const val KEY_ICON_STYLE = "key_icon_style"
        private const val KEY_COLOR_THEME = "key_color_theme"
        private const val KEY_ACTION_TYPE = "key_action_type"
        private const val KEY_VIBRATE = "key_vibrate"
        private const val KEY_FLOATING_BUTTON = "key_floating_button"
        private const val KEY_DELAY_SECONDS = "key_delay_seconds"
    }
}
