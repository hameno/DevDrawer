package de.psdev.devdrawer.profiles

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.databinding.FragmentWidgetProfileListBinding
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.consume
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProfileListFragment: BaseFragment<FragmentWidgetProfileListBinding>() {

    companion object: KLogging()

    // Dependencies
    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    val listAdapter: WidgetProfilesListAdapter = WidgetProfilesListAdapter()
    var _selectionTracker: SelectionTracker<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): FragmentWidgetProfileListBinding =
        FragmentWidgetProfileListBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerProfiles.adapter = listAdapter
        val selectionTracker = SelectionTracker.Builder(
            "widgetProfile",
            binding.recyclerProfiles,
            WidgetProfilesItemKeyProvider(listAdapter),
            WidgetProfilesDetailsLookup(binding.recyclerProfiles),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything()).build().also { tracker ->
            tracker.onRestoreInstanceState(savedInstanceState)
            tracker.addObserver(object: SelectionTracker.SelectionObserver<String?>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    activity?.invalidateOptionsMenu()
                }
            })
            _selectionTracker = tracker
        }
        listAdapter.selectionTracker = selectionTracker
        viewLifecycleOwner.lifecycleScope.launch {
            val widgetProfileDao = devDrawerDatabase.widgetProfileDao()
            widgetProfileDao.findAllFlow().collect {
                logger.warn { "$it" }
                listAdapter.awaitSubmit(it)
                binding.recyclerProfiles.scrollToPosition(it.indexOfFirst { selectionTracker.isSelected(it.id) })
            }
        }
        childFragmentManager.setFragmentResultListener("createProfile", viewLifecycleOwner) { _, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("profileId") ?: selectionTracker.selection.firstOrNull() ?: ""
            selectionTracker.select(result)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profiles_list, menu)
        val hasSelection = _selectionTracker?.hasSelection() ?: false
        menu.findItem(R.id.action_create).isVisible = !hasSelection
        menu.findItem(R.id.action_edit).isVisible = hasSelection
        menu.findItem(R.id.action_delete).isVisible = hasSelection
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.profiles)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> consume {
            lifecycleScope.launchWhenResumed {
                val widgetProfileDao = devDrawerDatabase.widgetProfileDao()
                val size = widgetProfileDao.findAll().size
                val widgetProfile = WidgetProfile(name = "Profile ${size + 1}")
                widgetProfileDao.insert(widgetProfile)
                findNavController().navigate(WidgetProfileListFragmentDirections.editWidgetProfile(widgetProfile.id))
            }
        }
        R.id.action_edit -> consume {
            val selectedId = _selectionTracker?.selection?.firstOrNull()
            if (selectedId != null) {
                findNavController().navigate(WidgetProfileListFragmentDirections.editWidgetProfile(selectedId))
            }
        }
        R.id.action_delete -> consume {
            lifecycleScope.launchWhenStarted {
                _selectionTracker?.let { tracker ->
                    val selectedProfile = tracker.selection.firstOrNull()
                    if (selectedProfile != null) {
                        val widgetProfile = devDrawerDatabase.widgetProfileDao().findById(selectedProfile)
                        if (widgetProfile != null) {
                            try {
                                devDrawerDatabase.widgetProfileDao().delete(widgetProfile)
                            } catch (e: SQLiteConstraintException) {
                                Snackbar.make(binding.root, R.string.error_profile_in_use, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        tracker.deselect(selectedProfile)
                    }
                }
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _selectionTracker?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _selectionTracker = null
        listAdapter.selectionTracker = null
        binding.recyclerProfiles.adapter = null
        super.onDestroyView()
    }
}
