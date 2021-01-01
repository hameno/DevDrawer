package de.psdev.devdrawer.profiles

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.databinding.FragmentWidgetProfileEditBinding
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.consume
import kotlinx.coroutines.flow.*
import mu.KLogging
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProfileEditFragment : BaseFragment<FragmentWidgetProfileEditBinding>() {

    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    private val args by navArgs<WidgetProfileEditFragmentArgs>()

    private val onDeleteClickListener: PackageFilterActionListener = { packageFilter ->
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete?")
            .setNegativeButton("No") { _, _ -> }
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launchWhenResumed {
                    devDrawerDatabase.packageFilterDao().deleteById(packageFilter.id)
                    UpdateReceiver.send(requireContext())
                }
            }
            .show()
    }
    private val onPreviewFilterClickListener: PackageFilterActionListener = { packageFilter ->
        findNavController().navigate(
            WidgetProfileEditFragmentDirections.openFilterPreviewBottomSheetDialogFragment(
                packageFilterId = packageFilter.id
            )
        )
    }
    private val listAdapter: PackageFilterListAdapter = PackageFilterListAdapter(
        onDeleteClickListener = onDeleteClickListener,
        onPreviewFilterClickListener = onPreviewFilterClickListener
    )
    private var widgetProfile: WidgetProfile? = null

    private var changedWidgetProfileProperty: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentWidgetProfileEditBinding = FragmentWidgetProfileEditBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val context = requireContext()

            btnAddFilter.setOnClickListener { _ ->
                widgetProfile?.let {
                    val directions =
                        WidgetProfileEditFragmentDirections.openAddPackageFilterBottomSheetDialogFragment(
                            widgetProfileId = it.id
                        )
                    findNavController().navigate(directions)
                }
            }

            btnAddSignature.setOnClickListener {
                widgetProfile?.let {
                    val directions =
                        WidgetProfileEditFragmentDirections.openAppSignatureChooserBottomSheetDialogFragment(
                            widgetProfileId = it.id
                        )
                    findNavController().navigate(directions)
                }
            }
            editName.textChanges().skipInitialValue().map { it.toString() }.onEach {
                widgetProfile?.let { widgetProfile ->
                    if (widgetProfile.name != it) {
                        widgetProfile.name = it
                        changedWidgetProfileProperty.value = true
                    }
                }
            }.launchIn(viewLifecycleScope)

            changedWidgetProfileProperty.onEach {
                btnApply.isVisible = it
            }.launchIn(viewLifecycleScope)

            btnApply.clicks().mapNotNull { widgetProfile }.onEach {
                devDrawerDatabase.widgetProfileDao().insertOrUpdate(it)
                editName.clearFocus()
                changedWidgetProfileProperty.value = false
            }.launchIn(viewLifecycleScope)

            recyclerPackages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerPackages.adapter = listAdapter
        }

        lifecycleScope.launchWhenResumed {
            val profile = devDrawerDatabase.widgetProfileDao().findById(args.profileId)!!
            binding.editName.setText(profile.name)
            widgetProfile = profile

        }
        devDrawerDatabase.packageFilterDao().findAllByProfileFlow(args.profileId).onEach {
            listAdapter.awaitSubmit(it)
        }.launchIn(viewLifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_widget_profile_edit, menu)
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(R.string.edit_profile)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_delete -> consume {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete profile?")
                .setNegativeButton("No") { _, _ -> }
                .setPositiveButton("Yes") { _, _ ->
                    widgetProfile?.let { widgetProfile ->
                        lifecycleScope.launchWhenResumed {
                            devDrawerDatabase.widgetProfileDao().delete(widgetProfile)
                            UpdateReceiver.send(requireContext())
                            findNavController().popBackStack()
                        }
                    }
                }
                .show()
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        binding.recyclerPackages.adapter = null
        super.onDestroyView()
    }

}
