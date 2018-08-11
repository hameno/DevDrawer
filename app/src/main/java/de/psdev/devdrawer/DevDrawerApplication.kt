package de.psdev.devdrawer

import android.content.Intent
import android.content.IntentFilter
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.evernote.android.job.JobManager
import com.squareup.leakcanary.LeakCanary
import de.psdev.devdrawer.appwidget.UpdateJob
import de.psdev.devdrawer.appwidget.UpdateJobCreator
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.receivers.AppInstallationReceiver
import mu.KLogging
import kotlin.system.measureTimeMillis

class DevDrawerApplication: MultiDexApplication() {

    companion object: KLogging()

    val devDrawerDatabase: DevDrawerDatabase by lazy { Room.databaseBuilder(this, DevDrawerDatabase::class.java, DevDrawerDatabase.NAME).build() }

    private val appInstallationReceiver: AppInstallationReceiver = AppInstallationReceiver()

    override fun onCreate() {
        super.onCreate()
        measureTimeMillis {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return
            }
            LeakCanary.install(this)
            registerAppInstallationReceiver()
            setupJobScheduler()
        }.let {
            logger.warn("{} version {} ({}) took {}ms to init", this::class.java.simpleName, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, it)
        }
    }

    private fun registerAppInstallationReceiver() {
        registerReceiver(appInstallationReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        })
    }

    private fun setupJobScheduler() {
        JobManager.create(this).addJobCreator(UpdateJobCreator(this))
        UpdateJob.enableJob()
        logger.debug { "Job requests: ${JobManager.instance().allJobRequests}" }
        logger.debug { "Jobs: ${JobManager.instance().allJobs}" }
        logger.debug { "Job results: ${JobManager.instance().allJobResults}" }
    }

}