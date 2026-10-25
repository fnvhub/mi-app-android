package com.example.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.R
import com.example.data.ActionType
import com.example.data.PreferencesManager
import com.example.util.PowerActionManager

@RequiresApi(Build.VERSION_CODES.N)
class PowerTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        val prefs = PreferencesManager(this)

        tile.state = if (PowerActionManager.canPerformLock(this)) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }

        tile.label = if (prefs.actionType == ActionType.POWER_DIALOG) {
            "Menú Apagado"
        } else {
            "Apagar Pantalla"
        }

        tile.icon = Icon.createWithResource(this, R.drawable.ic_power_symbol)
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        PowerActionManager.executeAction(this)
    }
}
