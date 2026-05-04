package com.autografr.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = emptyList()
)

val FrauncesFamily: FontFamily = FontFamily(
    Font(GoogleFont("Fraunces"), provider, FontWeight.Normal),
    Font(GoogleFont("Fraunces"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Fraunces"), provider, FontWeight.Bold),
    Font(GoogleFont("Fraunces"), provider, FontWeight.ExtraBold),
    Font(GoogleFont("Fraunces"), provider, FontWeight.Normal, FontStyle.Italic),
    Font(GoogleFont("Fraunces"), provider, FontWeight.SemiBold, FontStyle.Italic),
)

val InterFamily: FontFamily = FontFamily(
    Font(GoogleFont("Inter"), provider, FontWeight.Normal),
    Font(GoogleFont("Inter"), provider, FontWeight.Medium),
    Font(GoogleFont("Inter"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Inter"), provider, FontWeight.Bold),
)

val CaveatFamily: FontFamily = FontFamily(
    Font(GoogleFont("Caveat"), provider, FontWeight.Medium),
    Font(GoogleFont("Caveat"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Caveat"), provider, FontWeight.Bold),
)

val JetBrainsMonoFamily: FontFamily = FontFamily(
    Font(GoogleFont("JetBrains Mono"), provider, FontWeight.Normal),
    Font(GoogleFont("JetBrains Mono"), provider, FontWeight.Medium),
)

val Typography = Typography(
    // Display — Fraunces 700
    displayLarge  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 46.sp, letterSpacing = (-1.68).sp),
    displayMedium = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 37.8.sp, letterSpacing = (-0.9).sp),
    displaySmall  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 42.sp, lineHeight = 42.sp, letterSpacing = (-0.84).sp),

    // Heading
    headlineLarge  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 33.6.sp, letterSpacing = (-0.42).sp),
    headlineMedium = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.8.sp, letterSpacing = (-0.36).sp),
    headlineSmall  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 26.4.sp, letterSpacing = (-0.33).sp),

    // Title — Inter
    titleLarge  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 21.6.sp, letterSpacing = (-0.18).sp),
    titleMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    titleSmall  = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),

    // Body — Inter
    bodyLarge   = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.35.sp),
    bodyMedium  = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.4.sp),
    bodySmall   = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.8.sp),

    // Label / Eyebrow — Inter
    labelLarge  = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.25.sp),
    labelMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 11.sp, letterSpacing = 2.sp),
)
