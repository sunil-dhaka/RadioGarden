package com.example.radiogarden.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.example.radiogarden.R.array.com_google_android_gms_fonts_certs
)

val interFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
)

val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = interFamily),
        displayMedium = displayMedium.copy(fontFamily = interFamily),
        displaySmall = displaySmall.copy(fontFamily = interFamily),
        headlineLarge = headlineLarge.copy(fontFamily = interFamily),
        headlineMedium = headlineMedium.copy(fontFamily = interFamily),
        headlineSmall = headlineSmall.copy(fontFamily = interFamily),
        titleLarge = titleLarge.copy(fontFamily = interFamily),
        titleMedium = titleMedium.copy(fontFamily = interFamily),
        titleSmall = titleSmall.copy(fontFamily = interFamily),
        bodyLarge = bodyLarge.copy(fontFamily = interFamily),
        bodyMedium = bodyMedium.copy(fontFamily = interFamily),
        bodySmall = bodySmall.copy(fontFamily = interFamily),
        labelLarge = labelLarge.copy(fontFamily = interFamily),
        labelMedium = labelMedium.copy(fontFamily = interFamily),
        labelSmall = labelSmall.copy(fontFamily = interFamily),
    )
}
