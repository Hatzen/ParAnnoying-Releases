package de.hartz.software.parannoying.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.model.domain.SideFact

class SideFactCardAdapter(
    val sideFacts: List<SideFact>
) : RecyclerView.Adapter<SideFactCardAdapter.SideFactViewHolder>() {

    class SideFactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sideFactIdView: TextView = view.findViewById(R.id.sideFactId)
        val categoryView: TextView = view.findViewById(R.id.category)
        val labelView: TextView = view.findViewById(R.id.labelView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SideFactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_side_fact_card, parent, false)
        return SideFactViewHolder(view)
    }

    override fun onBindViewHolder(holder: SideFactViewHolder, position: Int) {
        val sideFact = sideFacts[position]
        holder.sideFactIdView.text = sideFact.id
        holder.categoryView.text = sideFact.category
        holder.labelView.text = sideFact.content
    }

    override fun getItemCount(): Int = sideFacts.size
}