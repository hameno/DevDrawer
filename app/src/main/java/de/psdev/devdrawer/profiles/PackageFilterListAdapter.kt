package de.psdev.devdrawer.profiles

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.databinding.ListItemPackageFilterBinding
import de.psdev.devdrawer.utils.layoutInflater

class PackageFilterListAdapter(
    private val onDeleteClickListener: PackageFilterActionListener,
    private val onPreviewFilterClickListener: PackageFilterActionListener
) : ListAdapter<PackageFilter, PackageFilterListAdapter.PackageFilterViewHolder>(PackageFilter.DIFF_CALLBACK) {

    var selectionTracker: SelectionTracker<String>? = null

    // ==========================================================================================================================
    // ListAdapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageFilterViewHolder {
        val onClickListener: PackageFilterActionListener = { packageFilter ->
            selectionTracker?.select(packageFilter.id)
        }
        return PackageFilterViewHolder(
            binding = ListItemPackageFilterBinding.inflate(parent.layoutInflater, parent, false),
            onClickListener = onClickListener,
            onDeleteClickListener = onDeleteClickListener,
            onPreviewFilterClickListener = onPreviewFilterClickListener
        )
    }

    override fun onBindViewHolder(holder: PackageFilterViewHolder, position: Int) {
        val packageFilter = getItem(position)
        val isSelected = selectionTracker?.isSelected(packageFilter.id) ?: false
        holder.bindTo(packageFilter, isSelected)
    }

    public override fun getItem(position: Int): PackageFilter = super.getItem(position)

    class PackageFilterViewHolder(
        private val binding: ListItemPackageFilterBinding,
        private val onClickListener: PackageFilterActionListener,
        private val onDeleteClickListener: PackageFilterActionListener,
        private val onPreviewFilterClickListener: PackageFilterActionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        var currentItem: PackageFilter? = null
            private set

        fun bindTo(packageFilter: PackageFilter, isActivated: Boolean = false) {
            currentItem = packageFilter
            with(binding) {
                root.isActivated = isActivated
                root.setOnClickListener {
                    onClickListener(packageFilter)
                }
                val iconRes = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> R.drawable.ic_regex
                    FilterType.SIGNATURE -> R.drawable.ic_certificate
                }
                imgIcon.setImageResource(iconRes)
                txtName.text = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> packageFilter.filter
                    FilterType.SIGNATURE -> packageFilter.description
                }

                with(btnPreview) {
                    setOnClickListener {
                        onPreviewFilterClickListener(packageFilter)
                    }
                }
                with(btnInfo) {
                    isVisible = packageFilter.type == FilterType.SIGNATURE
                    setOnClickListener {
                        val text = when (packageFilter.type) {
                            FilterType.PACKAGE_NAME -> packageFilter.description
                            FilterType.SIGNATURE -> "SHA256: ${
                                packageFilter.filter.uppercase().chunkedSequence(2)
                                    .joinToString(separator = ":")
                            }"
                        }
                        MaterialAlertDialogBuilder(itemView.context)
                            .setTitle(R.string.info)
                            .setMessage(text)
                            .setPositiveButton(R.string.close, null)
                            .show()
                    }
                }
                btnDelete.setOnClickListener {
                    onDeleteClickListener(packageFilter)
                }
            }
        }
    }

}