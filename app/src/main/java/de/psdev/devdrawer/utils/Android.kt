package de.psdev.devdrawer.utils

import android.os.Build

inline fun supportsLollipop(code: () -> Unit) = supportsVersion(Build.VERSION_CODES.LOLLIPOP, code)
inline fun supportsOreo(code: () -> Unit) = supportsVersion(Build.VERSION_CODES.O, code)

inline fun supportsVersion(sdk: Int, code: () -> Unit) {
    if (Build.VERSION.SDK_INT >= sdk) {
        code()
    }
}
