package de.psdev.devdrawer.appwidget

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import mu.KotlinLogging
import okio.HashingSink
import okio.blackholeSink
import okio.buffer

val logger = KotlinLogging.logger("AppInfo")

data class AppInfo(
    val name: String,
    val packageName: String,
    val appIcon: Drawable,
    val firstInstalledTime: Long,
    val lastUpdateTime: Long,
    val signatureSha256: String
)

fun PackageHashInfo.toAppInfo(context: Context): AppInfo? = try {
    val packageManager = context.packageManager
    val applicationInfo: ApplicationInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).applicationInfo
    val appName = applicationInfo.loadLabel(packageManager).toString()
    val appIcon = applicationInfo.loadIcon(packageManager)
    AppInfo(appName, packageName, appIcon, firstInstallTime, lastUpdateTime, signatureHashSha256)
} catch (e: Exception) {
    logger.warn(e) { "Error: ${e.message}" }
    null
}

@Suppress("DEPRECATION")
val PackageInfo.signatureHashSha256: String
    get() {
        val hashingSink = HashingSink.sha256(blackholeSink()).use {
            it.buffer().use { bufferedSink ->
                bufferedSink.write(signatures.first().toByteArray())
            }
            it
        }
        return hashingSink.hash.hex()
    }