package de.psdev.devdrawer.profiles

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.databinding.ListItemAppBinding
import de.psdev.devdrawer.utils.layoutInflater

class AppListAdapter(
    private val onAppClickListener: AppInfoActionListener
) : ListAdapter<AppInfo, AppListAdapter.AppInfoViewHolder>(AppInfo.DIFF_CALLBACK) {

    // ==========================================================================================================================
    // ListAdapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoViewHolder = AppInfoViewHolder(
        binding = ListItemAppBinding.inflate(parent.layoutInflater, parent, false),
        onClickListener = onAppClickListener
    )

    override fun onBindViewHolder(holder: AppInfoViewHolder, position: Int) {
        val appInfo = getItem(position)
        holder.bindTo(appInfo)
    }

    public override fun getItem(position: Int): AppInfo = super.getItem(position)

    class AppInfoViewHolder(
        private val binding: ListItemAppBinding,
        private val onClickListener: AppInfoActionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(appInfo: AppInfo) {
            binding.icon.setImageDrawable(appInfo.appIcon)
            binding.text1.text = appInfo.name
            binding.root.setOnClickListener {
                onClickListener(appInfo)
            }
        }
    }

}