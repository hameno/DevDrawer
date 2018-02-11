package de.psdev.devdrawer

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    fun sharedPreferences(
        application: Application
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

}