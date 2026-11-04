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

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberBlue,
    onPrimary = Color.Black,
    secondary = SurfaceSlate,
    onSecondary = TextSlate,
    tertiary = GlowCyan,
    background = DarkBackground,
    onBackground = TextSlate,
    surface = DeepSlate,
    onSurface = TextSlate,
    surfaceVariant = SurfaceSlate,
    onSurfaceVariant = MutedSlate,
    outline = BorderSlate
  )

private val LightColorScheme = DarkColorScheme // Always maintain premium dark theme for high-tech OSINT feeling

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for professional OSINT atmosphere
  dynamicColor: Boolean = false, // Disable dynamic colors to keep signature styling
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
