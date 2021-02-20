package de.psdev.devdrawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.psdev.devdrawer.analytics.TrackingService
import de.psdev.devdrawer.review.ReviewManager
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var trackingService: TrackingService

    @Inject
    lateinit var reviewManager: ReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            reviewManager.triggerReview(this@BaseActivity)
        }
    }
}