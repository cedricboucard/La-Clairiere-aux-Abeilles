package fr.clairiere.abeilles.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DisplaySerif = FontFamily.Serif
val BodySans = FontFamily.SansSerif

val Typography = Typography(
  displaySmall = TextStyle(
    fontFamily = DisplaySerif,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    color = TextEspresso
  ),
  headlineMedium = TextStyle(
    fontFamily = DisplaySerif,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    color = TextEspresso
  ),
  titleLarge = TextStyle(
    fontFamily = DisplaySerif,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    color = TextEspresso
  ),
  titleMedium = TextStyle(
    fontFamily = BodySans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    color = TextEspresso
  ),
  bodyLarge = TextStyle(
    fontFamily = BodySans,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    color = TextEspresso
  ),
  bodyMedium = TextStyle(
    fontFamily = BodySans,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    color = TextMuted
  ),
  labelLarge = TextStyle(
    fontFamily = BodySans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    color = TextEspresso
  ),
  labelSmall = TextStyle(
    fontFamily = BodySans,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    color = TextMuted
  )
)
