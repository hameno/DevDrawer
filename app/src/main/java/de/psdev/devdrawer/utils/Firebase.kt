package de.psdev.devdrawer.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.trace
import com.google.firebase.perf.metrics.Trace

inline fun <T> FirebasePerformance.trace(traceName: String, block: Trace.() -> T): T {
    return newTrace(traceName).trace(block)
}