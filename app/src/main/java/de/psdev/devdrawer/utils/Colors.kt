package de.psdev.devdrawer.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

@ColorInt
fun @receiver:ColorInt Int.textColorForBackground(): Int {
    val r = red
    val g = green
    val b = blue
    val yiq = (r * 299 + g * 587 + b * 114) / 1000
    return if (yiq >= 160) Color.BLACK else Color.WHITE
}

@ColorInt
fun @receiver:ColorInt Int.getDesaturatedColor(): Int {
    val result = FloatArray(size = 3)
    Color.colorToHSV(this, result)
    result[1] *= 0.6f
    return Color.HSVToColor(result)
}

/**
 * from https://medium.com/@anthony.st91/sort-things-by-colors-in-android-f34dc2c9f4b7
 */
fun @receiver:ColorInt Int.getHSL(): FloatArray {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this, hsl)
    for (i in hsl.indices) {
        hsl[i] = hsl[i] * 100
    }
    return hsl
}

/**
 * from https://medium.com/@anthony.st91/sort-things-by-colors-in-android-f34dc2c9f4b7
 */
fun @receiver:ColorInt IntArray.sortColorList(): List<Int> = sortedWith { o1, o2 ->
    val hsl1 = o1.getHSL()
    val hsl2 = o2.getHSL()

    // Colors have the same Hue
    if (hsl1[0] == hsl2[0]) {

        // Colors have the same saturation
        if (hsl1[1] == hsl2[1]) {
            // Compare lightness
            (hsl1[2] - hsl2[2]).toInt()

        } else {
            (hsl1[1] - hsl2[1]).toInt()
        }

    } else {
        (hsl1[0] - hsl2[0]).toInt()
    }
}