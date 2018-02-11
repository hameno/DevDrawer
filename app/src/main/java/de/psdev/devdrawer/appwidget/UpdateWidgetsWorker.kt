package de.psdev.devdrawer.appwidget

import android.app.Application
import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import de.psdev.devdrawer.receivers.UpdateReceiver
import mu.KLogging
import java.util.concurrent.TimeUnit

class UpdateWidgetsWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object : KLogging() {
        private val TAG: String = UpdateWidgetsWorker::class.java.simpleName

        fun enableWorker(application: Application) {
            val workManager = WorkManager.getInstance(application)
            val request = PeriodicWorkRequestBuilder<UpdateWidgetsWorker>(30, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }

    override suspend fun doWork(): Result {
        logger.debug { "Run update job" }
        UpdateReceiver.send(applicationContext)
        return Result.success()
    }
}