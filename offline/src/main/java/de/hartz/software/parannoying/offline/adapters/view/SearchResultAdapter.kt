package de.hartz.software.parannoying.offline.adapters.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.SearchResult
import de.hartz.software.parannoying.offline.model.domain.Type

class SearchResultAdapter(
    private val onClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            // Assuming SearchResult is uniquely identified by type + text + baseDialog.id
            return oldItem.text == newItem.text &&
                   oldItem.type == newItem.type &&
                   oldItem.baseDialog.persistenceId == newItem.baseDialog.persistenceId &&
                    oldItem.message?.persistenceId == newItem.message?.persistenceId
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headline: TextView = view.findViewById(R.id.headline)
        private val subtext: TextView = view.findViewById(R.id.subtext)

        fun bind(result: SearchResult) {
            when (result.type) {
                Type.USER -> {
                    headline.text = result.text
                    subtext.text = ""
                }
                Type.MESSAGE -> {
                    headline.text = result.baseDialog.nickname
                    subtext.text = result.text.take(250).let {
                        if (result.text.length > 250) "$it..." else it
                    }
                }
            }

            itemView.setOnClickListener { onClick(result) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}