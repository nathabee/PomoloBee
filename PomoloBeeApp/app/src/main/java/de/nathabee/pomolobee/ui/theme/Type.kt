package de.nathabee.pomolobee.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.nathabee.pomolobee.R // ✅ Fix Unresolved R


// ✅ Register Gentium Font
val GentiumFontFamily = FontFamily(
    Font(R.font.gentiumplus_regular, FontWeight.Normal),
    Font(R.font.gentiumplus_italic, FontWeight.Light),
    Font(R.font.gentiumplus_bold, FontWeight.Bold),
    Font(R.font.gentiumplus_bolditalic, FontWeight.Black)
)
// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = GentiumFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = GentiumFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GentiumFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)