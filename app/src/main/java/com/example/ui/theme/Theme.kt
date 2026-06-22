package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    secondary = GreenBgDark,
    tertiary = OrangeBg,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color(0xFFC8E6C9),
    onBackground = DarkOnBg,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF242925),
    onSurfaceVariant = Color(0xFFA6ACA6)
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenBg,
    tertiary = OrangeBg,
    background = LightBg,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color(0xFF1B5E20),
    onBackground = LightOnBg,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFEAECE8),
    onSurfaceVariant = Color(0xFF434943)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color of Android 12+ (false by default to keep the beautiful brand color identity)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
