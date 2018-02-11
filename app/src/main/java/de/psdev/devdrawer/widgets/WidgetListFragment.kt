package de.psdev.devdrawer.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.databinding.FragmentWidgetListBinding
import de.psdev.devdrawer.utils.Constants
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.supportsVersion
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mu.KLogging
import javax.inject.Inject


@AndroidEntryPoint
class WidgetListFragment : BaseFragment<FragmentWidgetListBinding>() {

    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentWidgetListBinding = FragmentWidgetListBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val clickListener: (Widget) -> Unit = { widget ->
            findNavController().navigate(WidgetListFragmentDirections.editWidget(widget.id))
        }
        val listAdapter = WidgetsListAdapter(clickListener)
        with(binding) {
            recyclerWidgets.adapter = listAdapter
        }
        devDrawerDatabase.widgetDao().findAllFlow().onEach {
            listAdapter.awaitSubmit(it)
            binding.containerNoWidgets.isVisible = it.isEmpty()
            supportsVersion(Build.VERSION_CODES.O) {
                with(binding.btnAddWidget) {
                    isVisible = true
                    setOnClickListener {
                        requestAppWidgetPinning()
                    }
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.widgets)
    }

    override fun onDestroyView() {
        binding.recyclerWidgets.adapter = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAppWidgetPinning() {
        val activity = requireActivity()
        val appWidgetManager: AppWidgetManager = activity.getSystemService() ?: return
        val widgetProvider = ComponentName(activity, DDWidgetProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val pinnedWidgetCallbackIntent = Intent(activity, DDWidgetProvider::class.java).apply {
                action = Constants.ACTION_WIDGET_PINNED
            }
            val successCallback = PendingIntent.getBroadcast(
                activity,
                1,
                pinnedWidgetCallbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val bundle = bundleOf()
            appWidgetManager.requestPinAppWidget(widgetProvider, bundle, successCallback)
        }
    }
}