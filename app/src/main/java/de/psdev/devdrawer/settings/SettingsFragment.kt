package de.psdev.devdrawer.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.DDWidgetProvider

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        findPreference<ListPreference>(R.string.pref_launch_intents).apply {
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = launchIntentLabelFromValue(newValue.toString())

                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>(R.string.pref_sort_order).apply {
            summary = sortOrderLabelFromValue(sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.pref_sort_order_default)).orEmpty())
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = sortOrderLabelFromValue(newValue.toString())

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DDWidgetProvider::class.java))
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView)

                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>(R.string.pref_theme).apply {
            summary = sharedPreferences.getString("theme", "Light")
            setOnPreferenceChangeListener { preference, newValue ->
                sharedPreferences.edit {
                    putString(preference.key, newValue.toString())
                }

                preference.summary = newValue.toString()

                Toast.makeText(activity, "You may need to re-add the widget for this change to take effect", Toast.LENGTH_SHORT).show()

                return@setOnPreferenceChangeListener true
            }
        }
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private inline fun <reified T: Preference> findPreference(@StringRes keyRes: Int): T = requireNotNull(findPreference(getString(keyRes)))

    private fun sortOrderLabelFromValue(value: String): String {
        val resources = resources
        val values = resources.getStringArray(R.array.sort_order_values)
        val names = resources.getStringArray(R.array.sort_order_labels)
        return names[values.indexOfFirst { it == value }]
    }

    private fun launchIntentLabelFromValue(value: String): String {
        val resources = resources
        val values = resources.getStringArray(R.array.launch_intent_values)
        val names = resources.getStringArray(R.array.launch_intent_labels)
        return names[values.indexOfFirst { it == value }]
    }
}