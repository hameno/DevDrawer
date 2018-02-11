package de.psdev.devdrawer.appwidget

import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.database.DevDrawerDatabase
import javax.inject.Inject

@AndroidEntryPoint
class WidgetService : RemoteViewsService() {

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onGetViewFactory(
        intent: Intent
    ): RemoteViewsFactory = WidgetAppsListViewFactory(
        context = applicationContext,
        devDrawerDatabase = devDrawerDatabase,
        sharedPreferences = sharedPreferences,
        intent = intent
    )
}