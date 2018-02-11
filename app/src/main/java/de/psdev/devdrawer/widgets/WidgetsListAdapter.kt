package de.psdev.devdrawer.widgets

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.databinding.ListItemWidgetBinding
import de.psdev.devdrawer.utils.layoutInflater
import mu.KLogging

class WidgetsListAdapter(
    private val clickListener: (Widget) -> Unit
): ListAdapter<Widget, WidgetsListAdapter.WidgetsListViewHolder>(Widget.DIFF_CALLBACK) {

    companion object: KLogging()

    // ==========================================================================================================================
    // RecyclerView.Adapter
    // ==========================================================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetsListViewHolder =
        WidgetsListViewHolder(
            ListItemWidgetBinding.inflate(parent.layoutInflater, parent, false),
            clickListener
        )

    override fun onBindViewHolder(holder: WidgetsListViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    // ==========================================================================================================================
    // WidgetsListViewHolder
    // ==========================================================================================================================

    class WidgetsListViewHolder(
        private val binding: ListItemWidgetBinding,
        private val clickListener: (Widget) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {

        fun bindTo(widget: Widget) {
            binding.txtName.text = widget.name
            itemView.setOnClickListener { clickListener(widget) }
        }
    }
}

