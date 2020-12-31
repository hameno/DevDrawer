package de.psdev.devdrawer.profiles

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.adapters.PartialMatchAdapter
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.databinding.AddPackageFilterBottomSheetDialogFragmentBinding
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.getExistingPackages
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class AddPackageFilterBottomSheetDialogFragment : BottomSheetDialogFragment(), TextWatcher {
    companion object : KLogging()

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    private var _binding: AddPackageFilterBottomSheetDialogFragmentBinding? = null
    private val binding get() = _binding!!

    private val navArgs: AddPackageFilterBottomSheetDialogFragmentArgs by navArgs()

    private val appPackages: List<String> by lazy { requireActivity().packageManager.getExistingPackages() }
    private val packageNameCompletionAdapter: PartialMatchAdapter by lazy {
        PartialMatchAdapter(
            requireActivity(),
            navArgs.widgetProfileId,
            appPackages,
            devDrawerDatabase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AddPackageFilterBottomSheetDialogFragmentBinding.inflate(inflater).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            editPackageName.setAdapter(packageNameCompletionAdapter)
            editPackageName.addTextChangedListener(this@AddPackageFilterBottomSheetDialogFragment)

            btnAdd.setOnClickListener {
                val filter = editPackageName.text.toString()
                if (filter.isNotEmpty()) {
                    lifecycleScope.launchWhenResumed {
                        val filters = devDrawerDatabase.packageFilterDao()
                            .findAllByProfile(navArgs.widgetProfileId)
                        if (filters.none { it.filter == filter }) {
                            val packageFilter = PackageFilter(
                                filter = filter,
                                profileId = navArgs.widgetProfileId
                            )
                            devDrawerDatabase.packageFilterDao().insert(packageFilter)
                            editPackageName.text.clear()
                            UpdateReceiver.send(requireContext())
                            dismiss()
                        } else {
                            inputLayoutPackage.error = "Filter already exists"
                        }
                    }
                }
            }
        }
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