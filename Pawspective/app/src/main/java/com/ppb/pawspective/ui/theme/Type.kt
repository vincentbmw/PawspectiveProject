package com.ppb.pawspective.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ppb.pawspective.R

// Custom font families
val QuicksandRegular = FontFamily(Font(R.font.quicksand_regular))
val QuicksandMedium = FontFamily(Font(R.font.quicksand_medium))
val QuicksandSemiBold = FontFamily(Font(R.font.quicksand_semibold))
val QuicksandBold = FontFamily(Font(R.font.quicksand_bold))
val QuicksandLight = FontFamily(Font(R.font.quicksand_light))
val QuandoRegular = FontFamily(Font(R.font.quando_regular))

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = QuicksandRegular,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = QuicksandSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = QuandoRegular,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)