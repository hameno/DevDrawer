package de.psdev.devdrawer.adapters

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter

abstract class BaseListAdapter<T: Any, VH: BaseListAdapter.ViewHolder>: ListAdapter {
    private val dataSetObservable = DataSetObservable()

    private val items = mutableListOf<T>()

    // ==========================================================================================================================
    // BaseAdapter
    // ==========================================================================================================================

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view = convertView?.tag as? VH ?: onCreateViewHolder(parent, getItemViewType(position))
        onBindViewHolder(view, position)
        return view.itemView
    }

    override fun getItem(position: Int): T = items[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = items.size
    override fun isEmpty(): Boolean = items.isEmpty()
    override fun getItemViewType(position: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEnabled(position: Int): Boolean = true
    override fun hasStableIds(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true

    override fun registerDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.unregisterObserver(observer)
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    fun notifyDataSetChanged() {
        dataSetObservable.notifyChanged()
    }

    /**
     * Notifies the attached observers that the underlying data is no longer valid
     * or available. Once invoked this adapter is no longer valid and should
     * not report further data set changes.
     */
    fun notifyDataSetInvalidated() {
        dataSetObservable.notifyInvalidated()
    }

    // ==========================================================================================================================
    // Public API
    // ==========================================================================================================================

    fun update(data: List<T>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    // ==========================================================================================================================
    // Internal API
    // ==========================================================================================================================

    protected abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    protected abstract fun onBindViewHolder(holder: VH, position: Int)

    abstract class ViewHolder(internal val itemView: View)

}