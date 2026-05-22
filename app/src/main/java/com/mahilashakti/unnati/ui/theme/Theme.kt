package com.mahilashakti.unnati.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary — Warm Saffron / Gold (Women's Empowerment)
val Primary = Color(0xFFE8A020)
val PrimaryDark = Color(0xFFBF7B00)
val OnPrimary = Color(0xFF1A1200)
val PrimaryContainer = Color(0xFFFFDF99)
val OnPrimaryContainer = Color(0xFF281900)

// Secondary — Deep Magenta / Rose
val Secondary = Color(0xFF9C3587)
val SecondaryDark = Color(0xFF7A1F6A)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFFD6F3)
val OnSecondaryContainer = Color(0xFF38003C)

// Tertiary — Teal (Trust / Finance)
val Tertiary = Color(0xFF006B5D)
val TertiaryContainer = Color(0xFF87F8DF)
val OnTertiaryContainer = Color(0xFF00201A)

val Error = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)

val Background = Color(0xFFFFF8F0)
val Surface = Color(0xFFFFF8F0)
val SurfaceVariant = Color(0xFFF0E0C8)

// Dark theme
val BackgroundDark = Color(0xFF1A1200)
val SurfaceDark = Color(0xFF231B00)

// Semantic
val PaidGreen = Color(0xFF2E7D32)
val PaidGreenContainer = Color(0xFFE8F5E9)
val PendingAmber = Color(0xFFF57F17)
val PendingAmberContainer = Color(0xFFFFF8E1)
val StarGold = Color(0xFFFFB300)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    errorContainer = ErrorContainer,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = Color(0xFF1A1200),
    onSurface = Color(0xFF1A1200),
    onSurfaceVariant = Color(0xFF4D3D00),
    outline = Color(0xFFB8974A)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFBD20),
    onPrimary = Color(0xFF432C00),
    primaryContainer = Color(0xFF604200),
    onPrimaryContainer = Color(0xFFFFDF99),
    secondary = Color(0xFFFFABE9),
    onSecondary = Color(0xFF5D1158),
    secondaryContainer = Color(0xFF79196E),
    onSecondaryContainer = Color(0xFFFFD6F3),
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = Color(0xFFFFF0CE),
    onSurface = Color(0xFFFFF0CE),
)

@Composable
fun MahilaShaktiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
