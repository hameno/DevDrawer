package de.psdev.devdrawer

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import androidx.room.Room
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.squareup.leakcanary.LeakCanary
import de.psdev.devdrawer.appwidget.UpdateJob
import de.psdev.devdrawer.appwidget.UpdateJobCreator
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.receivers.AppInstallationReceiver
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import mu.KLogging
import kotlin.system.measureTimeMillis

class DevDrawerApplication: Application() {

    companion object: KLogging() {
        init {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { AndroidSchedulers.from(Looper.getMainLooper(), true) }
            RxJavaPlugins.setErrorHandler { throwable ->
                logger.warn("Uncaught error: {}", throwable.message, throwable)
                // Send to crashlytics as non-fatal error
                Crashlytics.logException(throwable)
            }
        }
    }

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