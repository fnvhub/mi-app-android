package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.QuickLockActivity
import com.example.R
import com.example.data.ColorTheme
import com.example.data.IconStyle
import com.example.data.PreferencesManager

class PowerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = PreferencesManager(context)
            val views = RemoteViews(context.packageName, R.layout.widget_power_button)

            // Select icon
            val iconRes = when (prefs.iconStyle) {
                IconStyle.POWER -> R.drawable.ic_power_symbol
                IconStyle.LOCK -> R.drawable.ic_lock_symbol
            }
            views.setImageViewResource(R.id.widget_button_icon, iconRes)

            // Select background color drawable
            val bgRes = when (prefs.colorTheme) {
                ColorTheme.RED -> R.drawable.widget_bg_red
                ColorTheme.GREEN -> R.drawable.widget_bg_green
                ColorTheme.CYAN -> R.drawable.widget_bg_cyan
                ColorTheme.DARK -> R.drawable.widget_bg_dark
            }
            views.setInt(R.id.widget_button_container, "setBackgroundResource", bgRes)

            // Click action: Launches QuickLockActivity
            val intent = Intent(context, QuickLockActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_button_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, PowerWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                for (id in ids) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
