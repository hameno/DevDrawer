package de.psdev.devdrawer.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import mu.KLogging

class UpdateReceiver: BroadcastReceiver() {
    companion object: KLogging() {

        @JvmStatic
        fun intent(context: Context): Intent = Intent(context, UpdateReceiver::class.java)

        @JvmStatic
        fun send(context: Context) = context.sendBroadcast(intent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        logger.warn { "onReceive[context=$context, intent=$intent]" }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        context.sendBroadcast(Intent(context, DDWidgetProvider::class.java).apply {
            `package` = context.packageName
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                appWidgetManager.getAppWidgetIds(ComponentName(context, DDWidgetProvider::class.java))
            )
        })
    }
}