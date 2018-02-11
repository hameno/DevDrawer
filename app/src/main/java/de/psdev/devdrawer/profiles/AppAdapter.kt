package de.psdev.devdrawer.profiles

import android.view.ViewGroup
import de.psdev.devdrawer.adapters.BaseListAdapter
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.databinding.ListItemAppBinding
import de.psdev.devdrawer.utils.layoutInflater
import mu.KLogging

class AppAdapter: BaseListAdapter<AppInfo, AppAdapter.ViewHolder>() {

    companion object: KLogging()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(ListItemAppBinding.inflate(parent.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(private val binding: ListItemAppBinding): BaseListAdapter.ViewHolder(binding.root) {

        fun bindTo(appInfo: AppInfo) {
            binding.icon.setImageDrawable(appInfo.appIcon)
            binding.text1.text = appInfo.name
        }

    }

}
