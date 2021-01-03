package de.psdev.devdrawer.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.databinding.FragmentWidgetEditBinding
import de.psdev.devdrawer.profiles.WidgetProfilesDetailsLookup
import de.psdev.devdrawer.profiles.WidgetProfilesItemKeyProvider
import de.psdev.devdrawer.profiles.WidgetProfilesListAdapter
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.receiveClicksFrom
import de.psdev.devdrawer.utils.receiveTextChangesFrom
import de.psdev.devdrawer.utils.sortColorList
import de.psdev.devdrawer.widgets.EditWidgetFragmentViewModel.Selection
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditWidgetFragment : BaseFragment<FragmentWidgetEditBinding>() {

    // Dependencies
    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    val args by navArgs<EditWidgetFragmentArgs>()

    val viewModel: EditWidgetFragmentViewModel by viewModels()

    var _selectionTracker: SelectionTracker<String>? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentWidgetEditBinding =
        FragmentWidgetEditBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WidgetProfilesListAdapter()
        adapter.itemLongClickListener = { widgetProfile ->
            findNavController().navigate(EditWidgetFragmentDirections.createProfileAction(widgetProfile.id))
        }
        // Setup views
        with(binding) {
            with(editName) {
                setText("Widget ${args.widgetId}")
            }
            with(btnColor) {
                setOnClickListener {
                    val currentColor = viewModel.savedWidget.value?.color ?: Color.BLACK
                    MaterialColorPickerDialog
                            .Builder(requireContext())
                            .setTitle(R.string.pick_widget_color)
                            .setDefaultColor(currentColor)
                            .setColorShape(ColorShape.SQAURE)
                            .setColorRes(resources.getIntArray(R.array.widget_colors).sortColorList())
                            .setPositiveButton(R.string.ok)
                            .setNegativeButton(R.string.cancel)
                            .setColorListener { color, _ ->
                                setBackgroundColor(color)
                                viewModel.inputColor.value = color
                            }
                            .showBottomSheet(childFragmentManager)
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            with(binding) {
                val widget = checkNotNull(viewModel.savedWidget.filterNotNull().first())
                editName.setText(widget.name)
                btnColor.setBackgroundColor(widget.color)
            }
        }

        binding.btnNewProfile.setOnClickListener {
            lifecycleScope.launchWhenResumed {
                val widgetProfile = WidgetProfile(name = "Profile for ${viewModel.inputWidgetName.value}")
                devDrawerDatabase.widgetProfileDao().insert(widgetProfile)
                findNavController().navigate(EditWidgetFragmentDirections.createProfileAction(widgetProfile.id))
            }
        }

        binding.recyclerProfiles.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerProfiles.adapter = adapter
        val selectionTracker = SelectionTracker.Builder(
                "widgetProfile",
                binding.recyclerProfiles,
                WidgetProfilesItemKeyProvider(adapter),
                WidgetProfilesDetailsLookup(binding.recyclerProfiles),
                StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectSingleAnything()
        ).build().also {
            it.onRestoreInstanceState(savedInstanceState)
            if (savedInstanceState == null) {
                lifecycleScope.launchWhenResumed {
                    it.select(devDrawerDatabase.widgetDao().findById(args.widgetId)?.profileId.orEmpty())
                }
            }
            _selectionTracker = it
        }
        adapter.selectionTracker = selectionTracker

        viewLifecycleScope.launch {
            viewModel.inputWidgetName.receiveTextChangesFrom(binding.editName).launchIn(this)

            selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val widgetProfile = selectionTracker.selection.asSequence()
                        .map { selectedKey -> adapter.currentList.firstOrNull { it.id == selectedKey } }.firstOrNull()
                    if (widgetProfile != null) {
                        viewModel.inputSelectedProfile.value = Selection.Profile(widgetProfile)
                    } else {
                        viewModel.inputSelectedProfile.value = Selection.Nothing
                    }
                }
            })
            viewModel.inputSaveTrigger.receiveClicksFrom(binding.btnConfirm).launchIn(this)
            viewModel.outputWidgetProfiles.onEach {
                adapter.awaitSubmit(it)
                binding.txtNoProfiles.isVisible = it.isEmpty()
            }.launchIn(this)
            viewModel.outputFormCompleted.onEach { completed ->
                if (completed) {
                    with(binding.btnConfirm) {
                        isEnabled = true
                        setText(R.string.save)
                    }
                } else {
                    with(binding.btnConfirm) {
                        isEnabled = false
                        text = "Select profile"
                    }
                }
            }.launchIn(this)
            viewModel.outputCloseTrigger.onEach { widget ->
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.id)
                }
                requireActivity().setResult(Activity.RESULT_OK, resultValue)
                // Will either close the fragment or finish the activity when it's the last activity
                if (!findNavController().popBackStack()) {
                    requireActivity().finish()
                }
                UpdateReceiver.send(requireContext())
            }.launchIn(this)
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.edit_widget)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _selectionTracker?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.recyclerProfiles.adapter = null
        super.onDestroyView()
    }

    // TODO Default name: Widget <ID>
    // TODO After losing focus of text input update name in viewState

}
