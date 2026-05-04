package com.autografr.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioPassColors = lightColorScheme(
    primary           = Ink,
    onPrimary         = Paper,
    primaryContainer  = Ink2,
    onPrimaryContainer = Paper,
    secondary         = Foil,
    onSecondary       = Ink,
    secondaryContainer = Paper2,
    onSecondaryContainer = Muted,
    tertiary          = SignatureRed,
    onTertiary        = Paper,
    tertiaryContainer = Paper3,
    onTertiaryContainer = Ink,
    background        = Paper,
    onBackground      = Ink,
    surface           = Paper,
    onSurface         = Ink,
    surfaceVariant    = Paper2,
    onSurfaceVariant  = Muted,
    outline           = LineStrong,
    outlineVariant    = Line,
    error             = SignatureRed,
    onError           = Paper,
    scrim             = Color(0x8C0E0D0B),
)

@Composable
fun AutografrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StudioPassColors,
        typography  = Typography,
        content     = content
    )
}
