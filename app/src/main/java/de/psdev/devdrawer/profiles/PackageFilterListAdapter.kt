package de.psdev.devdrawer.profiles

import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.databinding.ListItemPackageFilterBinding
import de.psdev.devdrawer.utils.layoutInflater
import java.util.*

class PackageFilterListAdapter(
    private val onDeleteClickListener: ((String) -> Unit)
): ListAdapter<PackageFilter, PackageFilterListAdapter.PackageFilterViewHolder>(PackageFilter.DIFF_CALLBACK) {

    var selectionTracker: SelectionTracker<String>? = null

    // ==========================================================================================================================
    // ListAdapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageFilterViewHolder {
        val onClickListener: (String) -> Unit = { selectedItem: String ->
            selectionTracker?.select(selectedItem)
        }
        return PackageFilterViewHolder(
            binding = ListItemPackageFilterBinding.inflate(parent.layoutInflater, parent, false),
            onClickListener = onClickListener,
            onDeleteClickListener = onDeleteClickListener
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
        private val onClickListener: (String) -> Unit,
        private val onDeleteClickListener: (String) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        var currentItem: PackageFilter? = null
            private set

        fun bindTo(packageFilter: PackageFilter, isActivated: Boolean = false) {
            currentItem = packageFilter
            itemView.isActivated = isActivated
            itemView.setOnClickListener {
                onClickListener(packageFilter.id)
            }
            binding.btnInfo.isInvisible = packageFilter.type == FilterType.PACKAGE_NAME
            binding.btnInfo.setOnClickListener {
                val text = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> packageFilter.description
                    FilterType.SIGNATURE -> "SHA256: ${packageFilter.filter.toUpperCase(Locale.ROOT).chunkedSequence(2).joinToString(separator = ":")}"
                }
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle(R.string.info)
                    .setMessage(text)
                    .setPositiveButton(R.string.close, null)
                    .show()
            }
            with(binding) {
                txtName.text = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> "Package name: ${packageFilter.filter}"
                    FilterType.SIGNATURE -> "App signature: ${packageFilter.description}"
                }

                btnDelete.setOnClickListener {
                    onDeleteClickListener(packageFilter.id)
                }
            }

        }
    }

}