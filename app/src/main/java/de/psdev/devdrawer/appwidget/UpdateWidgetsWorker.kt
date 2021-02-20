package de.psdev.devdrawer.appwidget

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.psdev.devdrawer.receivers.UpdateReceiver
import mu.KLogging
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateWidgetsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object : KLogging() {
        private val TAG: String = UpdateWidgetsWorker::class.java.simpleName

        fun enableWorker(application: Application) {
            val workManager = WorkManager.getInstance(application)
            val request = PeriodicWorkRequestBuilder<UpdateWidgetsWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()
            workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }

    override suspend fun doWork(): Result {
        logger.debug { "Run update job" }
        UpdateReceiver.send(applicationContext)
        return Result.success()
    }
}