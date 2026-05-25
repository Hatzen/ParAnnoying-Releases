package de.hartz.software.parannoying.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.view.IconicsImageView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.model.view.ActionViewItem

class GenericActionViewItemAdapter(values : List<ActionViewItem>, val myContext:Context)
        : ArrayAdapter<ActionViewItem>(myContext, R.layout.item_main_action) {
    var values: List<ActionViewItem> = values

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup): View {
        val inflater = myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.item_main_action, parent, false)

        val entry = values[position]
        rowView.findViewById<TextView>(R.id.text).text = entry.text

        if (entry.icon == null) {
            rowView.findViewById<IconicsImageView>(R.id.icon).visibility = View.INVISIBLE
        } else {
            rowView.findViewById<IconicsImageView>(R.id.icon).icon = entry.icon as IconicsDrawable
        }

        if (entry.description == null) {
            rowView.findViewById<IconicsImageView>(R.id.info).visibility = View.GONE
        } else {
            rowView.findViewById<IconicsImageView>(R.id.info).setOnClickListener {
                DialogHelper.showDialog(myContext, "Info", entry.description!!)
            }
        }

        rowView.setOnClickListener { view ->
            entry.action()
        }

        return rowView
    }
}