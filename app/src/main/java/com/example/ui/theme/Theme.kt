package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElectricPurple,
    secondary = ElectricBlue,
    tertiary = CyanBlue,
    background = CosmicDark,
    surface = CosmicSlate,
    onPrimary = GlowSilver,
    onSecondary = GlowSilver,
    onTertiary = CosmicDark,
    onBackground = GlowSilver,
    onSurface = GlowSilver
  )

private val LightColorScheme = DarkColorScheme // Default both to DarkColorScheme as requested by user's premium dark design directive

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force cinematic dark theme as requested
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve customized brand visual design
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
