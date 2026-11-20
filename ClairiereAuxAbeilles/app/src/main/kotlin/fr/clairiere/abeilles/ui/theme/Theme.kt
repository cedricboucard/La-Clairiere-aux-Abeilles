package fr.clairiere.abeilles.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = AmberPrimary,
  onPrimary = Color.White,
  secondary = ForestGreen,
  onSecondary = Color.White,
  tertiary = WaterBlue,
  background = CreamBg,
  onBackground = TextEspresso,
  surface = CreamSurface,
  onSurface = TextEspresso,
  surfaceVariant = CreamWarm,
  onSurfaceVariant = TextMuted,
  outline = BorderGold
)

@Composable
fun LaClairiReAuxAbeillesTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
