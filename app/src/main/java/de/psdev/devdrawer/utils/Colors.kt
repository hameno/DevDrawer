package de.psdev.devdrawer.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

fun Int.rgbToYiq(): Int = ((red * 299) + (green * 587) + (blue * 114)) / 1000

fun getContrastColor(@ColorInt color: Int): Int {
    val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
    val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)
    return if (whiteContrast > blackContrast) Color.WHITE else Color.BLACK
}

fun textColorForBackground(backgroundColor: Int): Int {
    val r = backgroundColor.red * 255
    val g = backgroundColor.green * 255
    val b = backgroundColor.blue * 255
    val yiq = (r * 299 + g * 587 + b * 114) / 1000
    return if (yiq >= 128) Color.BLACK else Color.WHITE
}

@ColorInt
fun getDesaturatedColor(@ColorInt color: Int): Int {
    val result = FloatArray(size = 3)
    Color.colorToHSV(color, result)
    result[1] *= 0.6f
    return Color.HSVToColor(result)
}