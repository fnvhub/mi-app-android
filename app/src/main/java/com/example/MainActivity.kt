package com.example

import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ActionType
import com.example.data.ColorTheme
import com.example.data.IconStyle
import com.example.data.PreferencesManager
import com.example.receiver.PowerDeviceAdminReceiver
import com.example.service.FloatingLockService
import com.example.service.PowerAccessibilityService
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PowerCyanAccent
import com.example.ui.theme.PowerDarkBackground
import com.example.ui.theme.PowerDarkSurface
import com.example.ui.theme.PowerGreenSuccess
import com.example.ui.theme.PowerRedPrimary
import com.example.util.PowerActionManager
import com.example.widget.PowerWidgetProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember { PreferencesManager(context) }

    var isAccessibilityActive by remember {
        mutableStateOf(
            PowerAccessibilityService.isConnected() ||
                    PowerAccessibilityService.isServiceEnabledInSettings(context)
        )
    }
    var isDeviceAdminActive by remember {
        mutableStateOf(PowerDeviceAdminReceiver.isDeviceAdminActive(context))
    }

    var iconStyle by remember { mutableStateOf(prefs.iconStyle) }
    var colorTheme by remember { mutableStateOf(prefs.colorTheme) }
    var actionType by remember { mutableStateOf(prefs.actionType) }
    var vibrateOnPress by remember { mutableStateOf(prefs.vibrateOnPress) }
    var floatingButtonEnabled by remember { mutableStateOf(prefs.floatingButtonEnabled) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Recheck permissions whenever the user returns from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityActive = PowerAccessibilityService.isConnected() ||
                        PowerAccessibilityService.isServiceEnabledInSettings(context)
                isDeviceAdminActive = PowerDeviceAdminReceiver.isDeviceAdminActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val hasAnyPermission = isAccessibilityActive || isDeviceAdminActive

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PowerRedPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_power_symbol),
                                contentDescription = null,
                                tint = PowerRedPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Apagar Pantalla",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Ayuda y consejos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("main_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            item {
                StatusCard(
                    isAccessibilityActive = isAccessibilityActive,
                    isDeviceAdminActive = isDeviceAdminActive,
                    onEnableAccessibility = {
                        PowerActionManager.openAccessibilitySettings(context)
                    },
                    onEnableDeviceAdmin = {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(
                                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                PowerDeviceAdminReceiver.getComponentName(context)
                            )
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Permite apagar la pantalla rápidamente sin desgastar o pulsar el botón físico."
                            )
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // Hero Interactive Test Button
            item {
                HeroPowerButtonCard(
                    iconStyle = iconStyle,
                    colorTheme = colorTheme,
                    actionType = actionType,
                    hasPermission = hasAnyPermission,
                    onTestClick = {
                        PowerActionManager.executeAction(context)
                    }
                )
            }

            // Pin Widget to Home Screen Card
            item {
                PinWidgetCard(
                    onPinWidget = {
                        requestPinWidget(context)
                    }
                )
            }

            // Action Selection Card (Screen Off vs Power Dialog)
            item {
                ActionTypeSelectorCard(
                    selectedAction = actionType,
                    onActionSelected = { newAction ->
                        actionType = newAction
                        prefs.actionType = newAction
                    }
                )
            }

            // Widget Customization Card (Icon & Color Selection)
            item {
                WidgetCustomizationCard(
                    selectedIcon = iconStyle,
                    selectedColor = colorTheme,
                    onIconSelected = { newIcon ->
                        iconStyle = newIcon
                        prefs.iconStyle = newIcon
                        PowerWidgetProvider.updateAllWidgets(context)
                    },
                    onColorSelected = { newColor ->
                        colorTheme = newColor
                        prefs.colorTheme = newColor
                        PowerWidgetProvider.updateAllWidgets(context)
                    }
                )
            }

            // Extra Settings (Vibration, Floating Button, Delay)
            item {
                SettingsCard(
                    vibrateOnPress = vibrateOnPress,
                    onVibrateChange = {
                        vibrateOnPress = it
                        prefs.vibrateOnPress = it
                    },
                    floatingButtonEnabled = floatingButtonEnabled,
                    onFloatingButtonChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                Toast.makeText(
                                    context,
                                    "Concede el permiso para mostrar sobre otras aplicaciones",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                floatingButtonEnabled = true
                                prefs.floatingButtonEnabled = true
                                context.startService(Intent(context, FloatingLockService::class.java))
                            }
                        } else {
                            floatingButtonEnabled = false
                            prefs.floatingButtonEnabled = false
                            context.stopService(Intent(context, FloatingLockService::class.java))
                        }
                    }
                )
            }

            // Quick Info & Double Tap Advice
            item {
                InfoBannerCard()
            }

            // Safe bottom padding for navigation bars
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun StatusCard(
    isAccessibilityActive: Boolean,
    isDeviceAdminActive: Boolean,
    onEnableAccessibility: () -> Unit,
    onEnableDeviceAdmin: () -> Unit
) {
    val isFullyReady = isAccessibilityActive || isDeviceAdminActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyReady) {
                PowerGreenSuccess.copy(alpha = 0.12f)
            } else {
                PowerRedPrimary.copy(alpha = 0.12f)
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isFullyReady) {
                    listOf(PowerGreenSuccess.copy(alpha = 0.6f), PowerGreenSuccess.copy(alpha = 0.2f))
                } else {
                    listOf(PowerRedPrimary.copy(alpha = 0.7f), PowerRedPrimary.copy(alpha = 0.3f))
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isFullyReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isFullyReady) PowerGreenSuccess else PowerRedPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isFullyReady) "Servicio Activo • Listo" else "Requiere Permiso para Apagar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isAccessibilityActive) {
                            "Accesibilidad activa (Apagado sin perder huella dactilar)"
                        } else if (isDeviceAdminActive) {
                            "Administrador de dispositivo activo"
                        } else {
                            "Android requiere permiso para apagar la pantalla sin botón físico"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isFullyReady) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEnableAccessibility,
                    colors = ButtonDefaults.buttonColors(containerColor = PowerRedPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activate_accessibility_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_power_symbol),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activar Servicio de Apagado (1 toque)",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onEnableDeviceAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activate_admin_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "O activar como Administrador")
                }
            }
        }
    }
}

@Composable
fun HeroPowerButtonCard(
    iconStyle: IconStyle,
    colorTheme: ColorTheme,
    actionType: ActionType,
    hasPermission: Boolean,
    onTestClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val (bgGradient, glowColor) = when (colorTheme) {
        ColorTheme.RED -> listOf(Color(0xFFFF2B4E), Color(0xFF9E0B24)) to Color(0xFFFF3B5C)
        ColorTheme.GREEN -> listOf(Color(0xFF00E676), Color(0xFF007A38)) to Color(0xFF00E676)
        ColorTheme.CYAN -> listOf(Color(0xFF00E5FF), Color(0xFF00758F)) to Color(0xFF00E5FF)
        ColorTheme.DARK -> listOf(Color(0xFF2C323D), Color(0xFF14171D)) to Color(0xFF5E677B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_power_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BOTÓN PRINCIPAL DE APAGADO",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (actionType == ActionType.POWER_DIALOG) {
                    "Toca para abrir Menú de Apagado"
                } else {
                    "Toca para Apagar Pantalla"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Outer Ring & Main Touch Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
            ) {
                // Outer subtle glow
                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = 0.18f))
                )

                // Main Circular Button
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(bgGradient))
                        .border(3.dp, glowColor.copy(alpha = 0.8f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTestClick
                        )
                        .testTag("hero_power_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (iconStyle == IconStyle.POWER) {
                                R.drawable.ic_power_symbol
                            } else {
                                R.drawable.ic_lock_symbol
                            }
                        ),
                        contentDescription = "Botón de apagado",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasPermission) {
                    "Simula exactamente el botón físico sin dañarlo"
                } else {
                    "⚠️ Activa el permiso arriba para permitir que apague la pantalla"
                },
                fontSize = 13.sp,
                color = if (hasPermission) MaterialTheme.colorScheme.onSurfaceVariant else PowerRedPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PinWidgetCard(onPinWidget: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pin_widget_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Widget en Pantalla de Inicio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Coloca el botón On/Off directo en tu pantalla de inicio",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onPinWidget,
                modifier = Modifier.testTag("pin_widget_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Añadir", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionTypeSelectorCard(
    selectedAction: ActionType,
    onActionSelected: (ActionType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("action_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ACCIÓN AL PULSAR",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1: Lock Screen
            ActionOptionRow(
                title = "Apagar y Bloquear Pantalla",
                subtitle = "Apaga la pantalla inmediatamente. Puedes reactivarla con doble toque o huella.",
                icon = Icons.Default.PowerSettingsNew,
                isSelected = selectedAction == ActionType.LOCK_SCREEN,
                onClick = { onActionSelected(ActionType.LOCK_SCREEN) },
                testTag = "action_lock_screen"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Power Menu
            ActionOptionRow(
                title = "Menú de Encendido / Apagado",
                subtitle = "Abre el menú oficial de Android (Apagar, Reiniciar, Emergencia).",
                icon = Icons.Default.Settings,
                isSelected = selectedAction == ActionType.POWER_DIALOG,
                onClick = { onActionSelected(ActionType.POWER_DIALOG) },
                testTag = "action_power_dialog"
            )
        }
    }
}

@Composable
fun ActionOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ),
                width = 2.dp
            )
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WidgetCustomizationCard(
    selectedIcon: IconStyle,
    selectedColor: ColorTheme,
    onIconSelected: (IconStyle) -> Unit,
    onColorSelected: (ColorTheme) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customization_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PERSONALIZAR WIDGET E ICONO",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "1. Estilo del icono",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Power Icon Choice
                IconChoiceChip(
                    label = "Botón On/Off",
                    iconRes = R.drawable.ic_power_symbol,
                    isSelected = selectedIcon == IconStyle.POWER,
                    onClick = { onIconSelected(IconStyle.POWER) },
                    modifier = Modifier.weight(1f),
                    testTag = "icon_choice_power"
                )

                // Lock Icon Choice
                IconChoiceChip(
                    label = "Candado Cierre",
                    iconRes = R.drawable.ic_lock_symbol,
                    isSelected = selectedIcon == IconStyle.LOCK,
                    onClick = { onIconSelected(IconStyle.LOCK) },
                    modifier = Modifier.weight(1f),
                    testTag = "icon_choice_lock"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Color del botón",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorChoiceCircle(
                    colorName = "Rojo Neón",
                    color = Color(0xFFFF2B4E),
                    isSelected = selectedColor == ColorTheme.RED,
                    onClick = { onColorSelected(ColorTheme.RED) }
                )
                ColorChoiceCircle(
                    colorName = "Verde On",
                    color = Color(0xFF00E676),
                    isSelected = selectedColor == ColorTheme.GREEN,
                    onClick = { onColorSelected(ColorTheme.GREEN) }
                )
                ColorChoiceCircle(
                    colorName = "Azul Neón",
                    color = Color(0xFF00E5FF),
                    isSelected = selectedColor == ColorTheme.CYAN,
                    onClick = { onColorSelected(ColorTheme.CYAN) }
                )
                ColorChoiceCircle(
                    colorName = "Oscuro",
                    color = Color(0xFF2C323D),
                    isSelected = selectedColor == ColorTheme.DARK,
                    onClick = { onColorSelected(ColorTheme.DARK) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mini Widget Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0C0F))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VISTA PREVIA DEL WIDGET (1x1)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C95A6),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    when (selectedColor) {
                                        ColorTheme.RED -> listOf(Color(0xFFFF2B4E), Color(0xFF9E0B24))
                                        ColorTheme.GREEN -> listOf(Color(0xFF00E676), Color(0xFF007A38))
                                        ColorTheme.CYAN -> listOf(Color(0xFF00E5FF), Color(0xFF00758F))
                                        ColorTheme.DARK -> listOf(Color(0xFF2C323D), Color(0xFF14171D))
                                    }
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (selectedIcon == IconStyle.POWER) {
                                    R.drawable.ic_power_symbol
                                } else {
                                    R.drawable.ic_lock_symbol
                                }
                            ),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconChoiceChip(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)))
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ColorChoiceCircle(
    colorName: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = colorName,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingsCard(
    vibrateOnPress: Boolean,
    onVibrateChange: (Boolean) -> Unit,
    floatingButtonEnabled: Boolean,
    onFloatingButtonChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AJUSTES ADICIONALES",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Vibration Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vibración al pulsar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Confirma con una ligera vibración táctil",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = vibrateOnPress,
                    onCheckedChange = onVibrateChange,
                    modifier = Modifier.testTag("vibrate_switch")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Floating Bubble Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Botón flotante en pantalla",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Burbuja flotante movible visible en cualquier app",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = floatingButtonEnabled,
                    onCheckedChange = onFloatingButtonChange,
                    modifier = Modifier.testTag("floating_switch")
                )
            }
        }
    }
}

@Composable
fun InfoBannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("info_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 Ideal si se te ha roto el botón físico",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Muchos teléfonos permiten encender la pantalla con 'Doble Toque' o levantando el teléfono, pero no tienen opción para apagarla.\n• Con este Widget en tu pantalla de inicio o en la barra de notificaciones, ¡puedes apagar la pantalla con 1 toque sin volver a tocar el botón físico!",
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Cómo colocar y usar el Widget",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "1. Pulsa el botón 'Añadir' en la sección de widget de esta app.\n\n" +
                            "2. O mantén pulsada cualquier zona vacía de la pantalla de inicio de tu teléfono -> elige 'Widgets' -> busca 'Apagar Pantalla' y arrástralo donde quieras.\n\n" +
                            "3. Barra de notificaciones: Desliza hacia abajo la barra superior de Android, pulsa el lápiz de edición de Ajustes Rápidos y añade la ficha 'Apagar Pantalla'.\n\n" +
                            "4. Al usar el Servicio de Accesibilidad, tu teléfono se apaga y bloquea permitiendo desbloquearlo con huella dactilar normalmente.",
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}

private fun requestPinWidget(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, PowerWidgetProvider::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
            Toast.makeText(context, "Añadiendo widget a la pantalla de inicio...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Mantén pulsada la pantalla de inicio -> Widgets -> Apagar Pantalla",
                Toast.LENGTH_LONG
            ).show()
        }
    } else {
        Toast.makeText(
            context,
            "Mantén pulsada la pantalla de inicio -> Widgets -> Apagar Pantalla",
            Toast.LENGTH_LONG
        ).show()
    }
}
