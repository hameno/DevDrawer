package de.psdev.devdrawer

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.databinding.ActivityMainBinding
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object : KLogging()

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.widget_list_fragment,
            R.id.profiles_list_fragment,
            R.id.settings_fragment,
            R.id.about_fragment
        ).build()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navbar.setupWithNavController(navController)

        lifecycleScope.launchWhenResumed {
            trackingService.checkOptIn(this@MainActivity)
        }
    }

}

