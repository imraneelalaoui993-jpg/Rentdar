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
  primary = EmeraldGreenLight,
  onPrimary = EmeraldGreenDark,
  primaryContainer = EmeraldGreenDark,
  onPrimaryContainer = EmeraldGreenLight,
  secondary = AmberGold,
  tertiary = StatusBlue,
  background = Color(0xFF0F172A),
  surface = Color(0xFF1E293B),
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldGreen,
  onPrimary = Color.White,
  primaryContainer = EmeraldGreenLight,
  onPrimaryContainer = EmeraldGreenDark,
  secondary = AmberGold,
  onSecondary = Color.White,
  secondaryContainer = AmberGoldLight,
  tertiary = StatusBlue,
  background = Color(0xFFF8FAFC),
  surface = Color.White,
  surfaceVariant = Color(0xFFF1F5F9),
  onBackground = NeutralDark,
  onSurface = NeutralDark,
  onSurfaceVariant = NeutralMedium
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep branded emerald palette for RentDar identity
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
