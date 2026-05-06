package cz.cvut.fel.android_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark scheme — midnight sky palette.
// Primary = sky blue (bright, high-contrast, sunlight-readable).
// Secondary = neutral slate grey (doesn't compete with primary).
// Tertiary = warm gold accent.
// Surfaces = neutral charcoal — no strong hue cast.
// Dynamic colour disabled — wallpaper palettes can't guarantee sunlight contrast.
private val OutdoorDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),           // sky-400 — clear, bright, sunlight-readable
    onPrimary = Color(0xFF001829),
    primaryContainer = Color(0xFF01579B),  // deep sky container (FAB, badges)
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF94A3B8),         // slate-400 — neutral, clean
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF334155), // slate-700
    onSecondaryContainer = Color(0xFFCBD5E1),
    tertiary = Color(0xFFFBBF24),          // gold-400 — warm accent, not orange-dominant
    onTertiary = Color(0xFF1A0F00),
    tertiaryContainer = Color(0xFF3D2800),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF09090E),        // near-black, neutral
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0F1117),           // dark charcoal surface
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1C2333),    // dark blue-charcoal cards
    onSurfaceVariant = Color(0xFFCCD6E8), // cool white — high contrast on dark cards
    outline = Color(0xFF4A6480),           // muted steel blue — clear borders
    outlineVariant = Color(0xFF1A2640),    // dark navy dividers
    error = OutdoorError,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF7A0000),
    onErrorContainer = Color(0xFFFF8A80),
    scrim = Color(0xFF000000),
)

@Composable
fun Android_appTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OutdoorDarkColorScheme,
        typography = Typography,
        content = content
    )
}