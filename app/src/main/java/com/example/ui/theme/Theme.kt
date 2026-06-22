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

private val GamingColorScheme = darkColorScheme(
  primary = NeonBlue,
  secondary = PurpleAccent,
  tertiary = ActivePink,
  background = BgBlack,
  surface = SurfaceDark,
  onPrimary = BgBlack,
  onSecondary = TextWhite,
  onTertiary = TextWhite,
  onBackground = TextWhite,
  onSurface = TextWhite,
  surfaceVariant = SurfaceDarkCard,
  onSurfaceVariant = TextWhite,
  outline = BorderDark
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme always for immersive gaming experience
  dynamicColor: Boolean = false, // Disable to preserve neon brand identity
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = GamingColorScheme,
    typography = Typography,
    content = content
  )
}
