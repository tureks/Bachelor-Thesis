package cz.cvut.fel.android_app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// Raw palette — swap these to retheme the entire app
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Semantic surface tokens — adjust alpha values here for sunlight visibility
val ColorScheme.cardSurface: Color get() = surfaceVariant.copy(alpha = 0.4f)
val ColorScheme.cardSurfaceLight: Color get() = surfaceVariant.copy(alpha = 0.3f)
val ColorScheme.subtleContent: Color get() = onSurfaceVariant.copy(alpha = 0.7f)
val ColorScheme.disabledContent: Color get() = onSurfaceVariant.copy(alpha = 0.38f)
val ColorScheme.locationCardContainer: Color get() = secondaryContainer.copy(alpha = 0.6f)
val ColorScheme.dividerSubtle: Color get() = outlineVariant.copy(alpha = 0.5f)
val ColorScheme.primarySubtle: Color get() = primary.copy(alpha = 0.7f)