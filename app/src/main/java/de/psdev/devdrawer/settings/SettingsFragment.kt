package de.psdev.devdrawer.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.config.RemoteConfigService
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: PreferenceFragmentCompat() {

    @Inject
    lateinit var remoteConfigService: RemoteConfigService

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        findPreference<ListPreference>(R.string.pref_sort_order).apply {
            summary = sortOrderLabelFromValue(
                sharedPreferences.getString(
                    getString(R.string.pref_sort_order),
                    getString(R.string.pref_sort_order_default)
                ).orEmpty()
            )
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = sortOrderLabelFromValue(newValue.toString())

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds =
                    appWidgetManager.getAppWidgetIds(ComponentName(context, DDWidgetProvider::class.java))
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView)

                return@setOnPreferenceChangeListener true
            }
        }
        val analyticsCategory = requireNotNull(findPreference<PreferenceCategory>("feature_analytics"))
        val analyticsPreference = findPreference<SwitchPreferenceCompat>(R.string.pref_feature_analytics_opted_in)
        lifecycleScope.launchWhenResumed {
            val analyticsEnabled = remoteConfigService.getBoolean(TrackingService.CONFIG_KEY_ENABLED)
            analyticsCategory.isVisible = analyticsEnabled
            analyticsPreference.isVisible = analyticsEnabled
        }
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private inline fun <reified T : Preference> findPreference(@StringRes keyRes: Int): T =
        requireNotNull(findPreference(getString(keyRes)))

    private fun sortOrderLabelFromValue(value: String): String {
        val resources = resources
        val values = resources.getStringArray(R.array.sort_order_values)
        val names = resources.getStringArray(R.array.sort_order_labels)
        return names[values.indexOfFirst { it == value }]
    }

}