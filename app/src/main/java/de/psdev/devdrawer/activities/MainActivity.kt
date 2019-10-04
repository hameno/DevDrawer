package de.psdev.devdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import de.psdev.devdrawer.BaseActivity
import de.psdev.devdrawer.DevDrawerApplication
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.FilterListAdapter
import de.psdev.devdrawer.adapters.PartialMatchAdapter
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.PackageFilterDao
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.settings.SettingsActivity
import de.psdev.devdrawer.utils.Constants
import de.psdev.devdrawer.utils.consume
import de.psdev.devdrawer.utils.getExistingPackages
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main.*
import me.jfenn.attribouter.Attribouter
import mu.KLogging

class MainActivity: BaseActivity(), TextWatcher {

    companion object: KLogging() {
        @JvmStatic
        fun createStartIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    private val devDrawerDatabase by lazy { (application as DevDrawerApplication).devDrawerDatabase }
    private val appPackages: List<String> by lazy { packageManager.getExistingPackages() }
    private val filterListAdapter: FilterListAdapter by lazy { FilterListAdapter(this, devDrawerDatabase) }
    private val packageNameCompletionAdapter: PartialMatchAdapter by lazy { PartialMatchAdapter(this, appPackages, devDrawerDatabase) }
    private val packageFilterDao: PackageFilterDao by lazy { devDrawerDatabase.packageFilterDao() }
    private val subscriptions = CompositeDisposable()

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        setContentView(R.layout.main)

        actionBar?.apply {
            setDisplayShowTitleEnabled(true)
            title = "DevDrawer"
        }

        subscriptions += packageFilterDao.filters()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                filterListAdapter.data = it
            }
    }

    override fun onContentChanged() {
        super.onContentChanged()
        packagesFilterListView.adapter = filterListAdapter
        addPackageEditText.setAdapter(packageNameCompletionAdapter)
        addPackageEditText.addTextChangedListener(this)
        addButton.setOnClickListener { _ ->
            val filter = addPackageEditText.text.toString()
            if (filter.isNotEmpty()) {
                if (!filterListAdapter.data.map { it.filter }.contains(filter)) {
                    subscriptions += packageFilterDao.addFilterAsync(PackageFilter(filter = filter))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(onComplete = {
                            addPackageEditText.setText("")
                            UpdateReceiver.send(this)
                        })
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Filter already exists", Snackbar.LENGTH_INDEFINITE).show()
                }
            }

        }
    }

    override fun onBackPressed() {
        val extras = intent.extras
        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widget = DDWidgetProvider.createRemoteViews(this, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, widget)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Constants.EDIT_DIALOG_CHANGE -> {
                data?.let {
                    val id = it.getIntExtra("id", -1)
                    val newFilter = it.getStringExtra("newText")
                    packageFilterDao.updateFilter(id, newFilter)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> consume { startActivity(Intent(this, SettingsActivity::class.java)) }
        R.id.action_info -> consume { Attribouter.from(this).show() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

    // ==========================================================================================================================
    // TextWatcher
    // ==========================================================================================================================

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun afterTextChanged(editable: Editable) {
        packageNameCompletionAdapter.filter.filter(editable.toString())
    }

}

