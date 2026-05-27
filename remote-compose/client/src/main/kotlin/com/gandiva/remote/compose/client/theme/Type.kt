package com.gandiva.remote.compose.client.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight


class AppTypography(dimen: AppDimen) {
    private val defaultTypography = Typography()

    private val displayLarge = dimen.displayLarge
    private val headlineLarge = dimen.headlineLarge
    private val titleLarge = dimen.titleLarge
    private val bodyLarge = dimen.bodyLarge
    private val labelLarge = dimen.labelLarge

    private val mediumFontSizeScaleFactor = 1.25
    private val smallFontSizeScaleFactor = mediumFontSizeScaleFactor * 2

    val typography = Typography(
        displayLarge = defaultTypography.displayLarge.copy(
            fontSize = displayLarge
        ),
        displayMedium = defaultTypography.displayMedium.copy(
            fontSize = displayLarge / mediumFontSizeScaleFactor
        ),
        displaySmall = defaultTypography.displaySmall.copy(
            fontSize = displayLarge / smallFontSizeScaleFactor
        ),
        headlineLarge = defaultTypography.headlineLarge.copy(
            fontSize = headlineLarge,
            fontWeight = FontWeight.Bold,
        ),
        headlineMedium = defaultTypography.headlineMedium.copy(
            fontSize = headlineLarge / mediumFontSizeScaleFactor,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmall = defaultTypography.headlineSmall.copy(
            fontSize = headlineLarge / smallFontSizeScaleFactor
        ),
        titleLarge = defaultTypography.titleLarge.copy(
            fontSize = titleLarge,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            fontSize = titleLarge / mediumFontSizeScaleFactor,
        ),
        titleSmall = defaultTypography.titleSmall.copy(
            fontSize = titleLarge / smallFontSizeScaleFactor
        ),
        bodyLarge = defaultTypography.bodyLarge.copy(fontSize = bodyLarge),
        bodyMedium = defaultTypography.bodyMedium.copy(
            fontSize = bodyLarge / mediumFontSizeScaleFactor, fontWeight = FontWeight.Normal
        ),
        bodySmall = defaultTypography.bodySmall.copy(
            fontSize = bodyLarge / smallFontSizeScaleFactor,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = defaultTypography.labelLarge.copy(
            fontSize = labelLarge,
            fontWeight = FontWeight.Normal,
        ),
        labelMedium = defaultTypography.labelMedium.copy(
            fontSize = labelLarge / mediumFontSizeScaleFactor,
        ),
        labelSmall = defaultTypography.labelSmall.copy(
            fontSize = labelLarge / smallFontSizeScaleFactor
        ),
    )
}