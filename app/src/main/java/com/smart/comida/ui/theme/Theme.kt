package com.smart.comida.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = PrimaryGreen,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = LightPurple,
    onSecondaryContainer = PurpleAccent,
    tertiary = OrangeExpiring,
    onTertiary = Color.White,
    tertiaryContainer = LightOrange,
    onTertiaryContainer = OrangeExpiring,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = CardWhite,
    onSurface = TextDark,
    surfaceVariant = CardWhite,
    onSurfaceVariant = TextGray,
    outline = GrayBorder,
    error = RedExpiring,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenDark,
    onPrimary = Color.Black,
    primaryContainer = LightGreenDark,
    onPrimaryContainer = GreenDark,
    secondary = PurpleAccentDark,
    onSecondary = Color.Black,
    secondaryContainer = LightPurpleDark,
    onSecondaryContainer = PurpleAccentDark,
    tertiary = OrangeExpiringDark,
    onTertiary = Color.Black,
    tertiaryContainer = LightOrangeDark,
    onTertiaryContainer = OrangeExpiringDark,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextGrayDark,
    outline = GrayBorderDark,
    error = RedExpiringDark,
    onError = Color.Black
)

@Composable
fun ComidaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = Color.Transparent.toArgb()
            val navBarColor = Color.Transparent.toArgb()
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navBarColor

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
