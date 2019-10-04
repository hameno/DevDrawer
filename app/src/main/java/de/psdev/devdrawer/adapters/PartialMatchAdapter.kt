package de.psdev.devdrawer.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase

class PartialMatchAdapter(activity: Activity,
                          private val items: List<String>,
                          private val devDrawerDatabase: DevDrawerDatabase,
                          private val editMode: Boolean = false): BaseAdapter(), Filterable {
    private val filteredItems = mutableListOf<String>()
    private val layoutInflater: LayoutInflater = activity.layoutInflater
    private val packageFilter = object: Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            return if (charSequence == null) {
                FilterResults().apply {
                    count = items.size
                    values = items
                }
            } else {
                val existingFilters = devDrawerDatabase.packageFilterDao()
                    .filters()
                    .blockingFirst()
                    .map { it.filter }
                val existingFilterRegexes = existingFilters
                    .map { it.replace("*", ".*").toRegex() }
                val filteredItems = items
                    // Filter items already added
                    .filterNot { !editMode && existingFilters.contains(it) }
                    // Filter item matching existing filters with regex
                    .filterNot { !editMode && existingFilterRegexes.any { regex -> regex.matches(it) } }
                    // Filter matching
                    .filter { it.toLowerCase().contains(charSequence.toString().toLowerCase()) }
                FilterResults().apply {
                    count = filteredItems.size
                    values = filteredItems
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
            filteredItems.clear()
            filteredItems.addAll(filterResults.values as Collection<String>)
            notifyDataSetChanged()
        }
    }

    // ==========================================================================================================================
    // BaseAdapter
    // ==========================================================================================================================

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): Any = filteredItems[position]

    override fun getItemId(position: Int): Long = filteredItems[position].hashCode().toLong()

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View? {
        val view = convertView ?: createView(viewGroup)
        bindView(position, view)
        return view
    }

    // ==========================================================================================================================
    // Filterable
    // ==========================================================================================================================

    override fun getFilter(): Filter = packageFilter

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun createView(parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.dropdown_list_item, parent, false).apply {
            tag = ViewHolder(this)
        }
    }

    private fun bindView(position: Int, view: View) {
        val holder: ViewHolder = view.tag as ViewHolder
        holder.textView.text = filteredItems[position]
    }

    // ==========================================================================================================================
    // Inner classes
    // ==========================================================================================================================

    class ViewHolder(view: View) {
        var textView: TextView = view.findViewById(android.R.id.text1)
    }
}
