package de.psdev.devdrawer.utils

import android.view.LayoutInflater
import android.view.View

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)