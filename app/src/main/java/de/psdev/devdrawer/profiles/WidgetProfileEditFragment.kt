package de.psdev.devdrawer.profiles

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseFragment
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.PartialMatchAdapter
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.appwidget.toPackageHashInfo
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.databinding.FragmentWidgetProfileEditBinding
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.consume
import de.psdev.devdrawer.utils.getExistingPackages
import de.psdev.devdrawer.utils.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProfileEditFragment : BaseFragment<FragmentWidgetProfileEditBinding>(), TextWatcher {

    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    val args by navArgs<WidgetProfileEditFragmentArgs>()

    private val appPackages: List<String> by lazy { requireActivity().packageManager.getExistingPackages() }
    private val packageNameCompletionAdapter: PartialMatchAdapter by lazy {
        PartialMatchAdapter(
            requireActivity(),
            args.profileId,
            appPackages,
            devDrawerDatabase
        )
    }
    private val onDeleteClickListener: (String) -> Unit = { id ->
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete?")
            .setNegativeButton("No") { _, _ -> }
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launchWhenResumed {
                    devDrawerDatabase.packageFilterDao().deleteById(id)
                    UpdateReceiver.send(requireContext())
                }
            }
            .show()
    }
    private val listAdapter: PackageFilterListAdapter = PackageFilterListAdapter(onDeleteClickListener)
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

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val context = requireContext()
            radioGroupFilterType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_package_name -> {
                        groupSignature.isVisible = false
                        groupPackageName.isVisible = true
                    }
                    R.id.radio_signature -> {
                        groupPackageName.isVisible = false
                        groupSignature.isVisible = true
                    }
                }
            }

            btnAdd.setOnClickListener { _ ->
                val filter = editPackageName.text.toString()
                if (filter.isNotEmpty()) {
                    if (listAdapter.currentList.none { it.filter == filter }) {
                        widgetProfile?.let {
                            lifecycleScope.launchWhenResumed {
                                val packageFilter = PackageFilter(
                                    filter = filter,
                                    profileId = it.id
                                )
                                devDrawerDatabase.packageFilterDao().insert(packageFilter)
                                editPackageName.text.clear()
                                UpdateReceiver.send(context)
                            }
                        }
                    } else {
                        inputLayoutPackage.error = "Filter already exists"
                    }
                }

            }

            btnSelectApp.setOnClickListener {
                btnSelectApp.isEnabled = false

                val appAdapter = AppAdapter()
                MaterialAlertDialogBuilder(context)
                    .setTitle("Select signature from app")
                    .setAdapter(appAdapter) { _, position ->
                        widgetProfile?.let {
                            lifecycleScope.launch {
                                val appInfo = appAdapter.getItem(position)
                                val packageFilter = PackageFilter(
                                    filter = appInfo.signatureSha256,
                                    type = FilterType.SIGNATURE,
                                    description = appInfo.name,
                                    profileId = it.id
                                )
                                devDrawerDatabase.packageFilterDao().insert(packageFilter)
                                UpdateReceiver.send(context)
                            }
                        }
                    }
                    .show()
                viewLifecycleScope.launch(Dispatchers.IO) {
                    val packageManager = context.packageManager
                    Firebase.performance.trace("widget_profile_packages") {
                        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)
                            .asSequence()
                            .map { it.toPackageHashInfo() }
                            .distinctBy { it.signatureHashSha256 }
                            .filter { hashInfo -> listAdapter.currentList.none { it.type == FilterType.SIGNATURE && it.filter == hashInfo.signatureHashSha256 } }
                            .mapNotNull { it.toAppInfo(context) }
                            .sortedBy { it.name }
                            .toList()
                        withContext(Dispatchers.Main) {
                            appAdapter.update(installedPackages)
                            btnSelectApp.isEnabled = true
                        }
                    }
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

            // Auto completion edit text
            editPackageName.setAdapter(packageNameCompletionAdapter)
            editPackageName.addTextChangedListener(this@WidgetProfileEditFragment)

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

    // ==========================================================================================================================
    // TextWatcher
    // ==========================================================================================================================

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) = Unit

    override fun afterTextChanged(editable: Editable) {
        packageNameCompletionAdapter.filter.filter(editable.toString())
        binding.inputLayoutPackage.error = null
    }

}
