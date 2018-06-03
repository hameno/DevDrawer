package de.psdev.devdrawer.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.widget.Toast
import de.psdev.devdrawer.R
import de.psdev.devdrawer.settings.SettingsActivity
import de.psdev.devdrawer.utils.Constants
import mu.KLogging

class ClickHandlingActivity: Activity() {
    companion object: KLogging() {
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_LAUNCH_TYPE = "launchType"
    }

    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val launchType = intent.getIntExtra(EXTRA_LAUNCH_TYPE, 0)

        if (packageName != null && isAppInstalled(packageName)) {
            when (launchType) {
                Constants.LAUNCH_APP -> startApp(this, packageName)
                Constants.LAUNCH_APP_DETAILS -> startAppDetails(this, packageName)
                Constants.LAUNCH_UNINSTALL -> startUninstall(this, packageName)
                // We need to always call finish
                else -> finish()
            }
        } else {
            // We need to always call finish
            finish()
        }
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun isAppInstalled(uri: String): Boolean = try {
        packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun startApp(activity: Activity, packageName: String) {
        if (sharedPreferences.getBoolean(getString(R.string.pref_show_activity_choice), resources.getBoolean(R.bool.pref_show_activity_choice_default))) {
            // Show the activity choice dialog
            val intent = ChooseActivityDialog.createStartIntent(activity, packageName).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
            activity.finish()
        } else {
            // Launch the app
            try {
                val intent = activity.packageManager.getLaunchIntentForPackage(packageName).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    flags = if (sharedPreferences.getString(getString(R.string.pref_launch_intents), getString(R.string.pref_launch_intents_default)) ==
                        getString(R.string.launch_intent_aosp)) {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    } else {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
                    }
                }
                activity.startActivity(intent)
            } catch (e: NullPointerException) {
                Toast.makeText(activity, activity.getString(R.string.no_main_activity_could_be_found), Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                activity.startActivity(intent)
            }

            activity.finish()
        }
    }

    private fun startAppDetails(activity: Activity, packageName: String) {
        // Launch the app details settings screen for the app
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        activity.startActivity(intent)
        activity.finish()
    }

    private fun startUninstall(activity: Activity, packageName: String) {
        try {
            val packageUri = Uri.parse("package:" + packageName)
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity.startActivity(uninstallIntent)
            activity.finish()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Application cannot be uninstalled / possibly system app", Toast.LENGTH_SHORT).show()
        }

    }

}
