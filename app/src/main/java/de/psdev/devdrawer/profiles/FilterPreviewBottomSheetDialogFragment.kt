package de.psdev.devdrawer.profiles

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.appwidget.toPackageHashInfo
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.databinding.FilterPreviewBottomSheetDialogFragmentBinding
import de.psdev.devdrawer.utils.awaitSubmit
import de.psdev.devdrawer.utils.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class FilterPreviewBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    private var _binding: FilterPreviewBottomSheetDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private val navArgs: FilterPreviewBottomSheetDialogFragmentArgs by navArgs()

    private val onAppClickListener: AppInfoActionListener = { appInfo ->
        val activity = requireActivity()
        startActivity(activity.packageManager.getLaunchIntentForPackage(appInfo.packageName))
    }
    private val appAdapter = AppListAdapter(onAppClickListener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FilterPreviewBottomSheetDialogFragmentBinding.inflate(inflater).also {
        _binding = it
    }.root

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnClose.setOnClickListener { dismiss() }
            recyclerApps.adapter = appAdapter
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    val filter = devDrawerDatabase.packageFilterDao().findById(navArgs.packageFilterId)
                            ?: throw IllegalArgumentException("Unknown filter")
                    val context = requireContext()
                    val packageManager = context.packageManager
                    val affectedApps = Firebase.performance.trace("profile_filter_preview") {
                        packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)
                                .asSequence()
                                .map { it.toPackageHashInfo() }
                                .filter { filter.matches(it) }
                                .mapNotNull { it.toAppInfo(context) }
                                .sortedBy { it.name }
                                .toList()
                    }
                    logger.warn { "Affected apps: $affectedApps" }
                    withContext(Dispatchers.Main) {
                        appAdapter.awaitSubmit(affectedApps)
                        progress.hide()
                        recyclerApps.isVisible = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerApps.adapter = null
        super.onDestroyView()
    }
}