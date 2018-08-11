package de.psdev.devdrawer

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.fragment.app.FragmentManager
import de.psdev.devdrawer.utils.consume

abstract class BaseActivity: AppCompatActivity() {

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> consume {
                // From: https://developer.android.com/training/implementing-navigation/ancestral.html
                val upIntent = NavUtils.getParentActivityIntent(this)
                if (upIntent != null) {
                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        // This activity is NOT part of this app's task, so create a new task
                        // when navigating up, with a synthesized back stack.
                        TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities()
                    } else {
                        // This activity is part of this app's task, so simply
                        // navigate up to the logical parent activity.
                        NavUtils.navigateUpTo(this, upIntent)
                    }
                } else {
                    finish()
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (onBackPressed(fragmentManager)) {
            return
        }
        super.onBackPressed()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    // see http://stackoverflow.com/a/24176614/381899
    private fun onBackPressed(fm: FragmentManager?): Boolean {
        if (fm != null) {
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                invalidateOptionsMenu()
                return true
            }

            val fragList = fm.fragments
            if (!fragList.isEmpty()) {
                for (frag in fragList) {
                    if (frag == null) {
                        continue
                    }
                    if (frag.isVisible && onBackPressed(frag.childFragmentManager)) {
                        invalidateOptionsMenu()
                        return true
                    }
                }
            }
        }
        invalidateOptionsMenu()
        return false
    }

}