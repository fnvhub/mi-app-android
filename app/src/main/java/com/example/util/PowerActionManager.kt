package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.widget.Toast
import com.example.MainActivity
import com.example.data.ActionType
import com.example.data.PreferencesManager
import com.example.receiver.PowerDeviceAdminReceiver
import com.example.service.PowerAccessibilityService

object PowerActionManager {

    fun executeAction(context: Context, forceActionType: ActionType? = null) {
        val prefs = PreferencesManager(context)
        val action = forceActionType ?: prefs.actionType

        if (prefs.vibrateOnPress) {
            triggerHapticFeedback(context)
        }

        val delay = prefs.delaySeconds
        if (delay > 0) {
            Toast.makeText(context, "Apagando pantalla en $delay segundos...", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                performExecution(context, action)
            }, (delay * 1000).toLong())
        } else {
            performExecution(context, action)
        }
    }

    private fun performExecution(context: Context, action: ActionType) {
        when (action) {
            ActionType.POWER_DIALOG -> {
                val success = PowerAccessibilityService.openPowerDialog()
                if (!success) {
                    showPermissionNeededToast(context, isForPowerMenu = true)
                }
            }
            ActionType.LOCK_SCREEN -> {
                // Primary: Accessibility Service (Preserves biometrics/fingerprint unlock)
                var success = PowerAccessibilityService.lockScreen()

                // Secondary: Device Administrator
                if (!success) {
                    success = PowerDeviceAdminReceiver.lockNow(context)
                }

                if (!success) {
                    showPermissionNeededToast(context, isForPowerMenu = false)
                }
            }
        }
    }

    fun canPerformLock(context: Context): Boolean {
        return PowerAccessibilityService.isConnected() ||
                PowerAccessibilityService.isServiceEnabledInSettings(context) ||
                PowerDeviceAdminReceiver.isDeviceAdminActive(context)
    }

    fun canPerformPowerDialog(context: Context): Boolean {
        return PowerAccessibilityService.isConnected() ||
                PowerAccessibilityService.isServiceEnabledInSettings(context)
    }

    private fun triggerHapticFeedback(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun showPermissionNeededToast(context: Context, isForPowerMenu: Boolean) {
        val message = if (isForPowerMenu) {
            "Activa el Servicio de Accesibilidad para abrir el menú de apagado"
        } else {
            "Activa el Servicio de Accesibilidad o el Administrador para apagar la pantalla"
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        // Open Main Screen or Accessibility Settings so the user can easily enable it
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_PERMISSIONS_PROMPT", true)
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir los ajustes de accesibilidad", Toast.LENGTH_SHORT).show()
        }
    }
}
