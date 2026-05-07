package cz.cvut.fel.android_app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Logo palette — tree silhouettes + water surface
val SlateDeep = Color(0xFF111827)     // logo: deep tree. M
val SlateDark = Color(0xFF1E2D40)     // logo: tree silhouette
val SkyMid = Color(0xFF38BDF8)        // logo: water surface / primary accent
val SkyDeep = Color(0xFF0284C7)       // logo: deep water

// UI variants
val SkyBright = Color(0xFF7DD3FC)     // sky-300 — lighter highlight
val SlateGrey = Color(0xFF94A3B8)     // slate-400 — neutral secondary
val OutdoorError = Color(0xFFFF5252)

// Semantic surface tokens — high alpha values for sunlight visibility
val ColorScheme.cardSurface: Color get() = surfaceVariant.copy(alpha = 0.9f)
val ColorScheme.cardSurfaceLight: Color get() = surfaceVariant.copy(alpha = 0.7f)
val ColorScheme.subtleContent: Color get() = onSurfaceVariant.copy(alpha = 0.9f)
val ColorScheme.disabledContent: Color get() = onSurfaceVariant.copy(alpha = 0.55f)
val ColorScheme.locationCardContainer: Color get() = secondaryContainer.copy(alpha = 0.9f)
val ColorScheme.dividerSubtle: Color get() = outlineVariant.copy(alpha = 0.8f)
val ColorScheme.primarySubtle: Color get() = primary.copy(alpha = 0.9f)