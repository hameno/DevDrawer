package de.psdev.devdrawer.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.activities.ClickHandlingActivity
import de.psdev.devdrawer.utils.Constants
import mu.KLogging

class DDWidgetViewsFactory(private val context: Context, intent: Intent): RemoteViewsService.RemoteViewsFactory {

    companion object: KLogging()

    private val appWidgetId: Int by lazy { intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) }
    private val viewId: Int by lazy { intent.getIntExtra("viewId", -1) }

    private val appWidgetManager: AppWidgetManager by lazy { AppWidgetManager.getInstance(context) }
    private val packageManager: PackageManager by lazy { context.packageManager }
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    private val apps: MutableList<AppInfo> = mutableListOf()

    // ==========================================================================================================================
    // RemoteViewsService.RemoteViewsFactory
    // ==========================================================================================================================

    override fun onCreate() {
        logger.warn { "onCreate" }
    }

    override fun onDataSetChanged() {
        logger.warn { "onDataSetChanged" }
        loadApps()
    }

    override fun onDestroy() {
        logger.warn { "onDestroy" }
    }

    override fun getCount(): Int = apps.size

    override fun getViewAt(position: Int): RemoteViews? {
        logger.debug { "getViewAt[position=$position]" }
        val (appName, packageName, appIcon) = apps[position]

        // Setup the list item and intents for on click
        val row = RemoteViews(context.packageName, R.layout.list_item)

        try {
            row.setTextViewText(R.id.packageNameTextView, packageName)
            row.setTextViewText(R.id.appNameTextView, appName)
            row.setImageViewBitmap(R.id.imageView, convertFromDrawable(appIcon))

            if (sharedPreferences.getString("theme", "Light") == "Light") {
                row.setTextColor(R.id.appNameTextView, ContextCompat.getColor(context, R.color.app_name_light))
                row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview)
                row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview)
            } else {
                row.setTextColor(R.id.appNameTextView, ContextCompat.getColor(context, R.color.app_name_dark))
                row.setImageViewResource(R.id.appDetailsImageButton, R.drawable.settings_imageview_dark)
                row.setImageViewResource(R.id.uninstallImageButton, R.drawable.delete_imageview_dark)
            }

            val appDetailsClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_APP_DETAILS)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.appDetailsImageButton, appDetailsClickIntent)

            val uninstallClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_UNINSTALL)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.uninstallImageButton, uninstallClickIntent)

            val rowClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_APP)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.touchArea, rowClickIntent)

            return row
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = apps[position].packageName.hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    // ==========================================================================================================================
    // Private APi
    // ==========================================================================================================================

    /**
     * Method to get all apps from the app database and add to the dataset
     */
    private fun loadApps() {
        val application = context.applicationContext as DevDrawerApplication

        val devDrawerDatabase = application.devDrawerDatabase

        val packageFilters = devDrawerDatabase.packageFilterDao()
            .filters()
            .blockingFirst(emptyList())
            .map { it.filter.replace("*", ".*").toRegex() }

        val appList = packageManager.getInstalledPackages(0).asSequence()
            .filter {
                return@filter packageFilters.any { filter ->
                    return@any filter.matches(it.packageName)
                }
            }
            .mapNotNull {
                try {
                    val applicationInfo: ApplicationInfo = packageManager.getPackageInfo(it.packageName, PackageManager.GET_ACTIVITIES).applicationInfo
                    val appName = applicationInfo.loadLabel(packageManager).toString()
                    val appIcon = applicationInfo.loadIcon(packageManager)
                    return@mapNotNull AppInfo(appName, it.packageName, appIcon, it.firstInstallTime, it.lastUpdateTime)
                } catch (e: Exception) {
                    logger.warn("Error: {}", e.message, e)
                }
                return@mapNotNull null
            }
            .sortedWith(appComparator)
            .distinct()
            .toList()

        apps.clear()
        apps.addAll(appList)
    }

    private val appComparator: Comparator<AppInfo>
        get() {
            val defaultSortOrder = context.getString(R.string.pref_sort_order_default)
            return when (SortOrder.valueOf(sharedPreferences.getNonNullString(R.string.pref_sort_order, defaultSortOrder))) {
                SortOrder.FIRST_INSTALLED -> compareByDescending { it.firstInstalledTime }
                SortOrder.LAST_UPDATED -> compareByDescending { it.lastUpdateTime }
                SortOrder.NAME -> compareBy { it.name }
                SortOrder.PACKAGE_NAME -> compareBy { it.packageName }
            }
        }

    /**
     * Method to return a bitmap from drawable
     */
    private fun convertFromDrawable(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            getBitmapFromDrawable(drawable)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private fun SharedPreferences.getNonNullString(
        @StringRes stringRes: Int,
        defaultValue: String
    ): String = getString(context.getString(stringRes), defaultValue) ?: defaultValue

    // ==========================================================================================================================
    // Inner classes
    // ==========================================================================================================================

    data class AppInfo(
        val name: String,
        val packageName: String,
        val appIcon: Drawable,
        val firstInstalledTime: Long,
        val lastUpdateTime: Long
    )

}
