package de.hartz.software.parannoying.offline.adapters.view.settings

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hartz.software.parannoying.core.R
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(
    private val onClickRow: (File) -> Unit,
    private val onClickShareButton: (File) -> Unit,
) : RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    val files = mutableListOf<File>()
    private val selected = mutableSetOf<File>()

    fun submitList(newFiles: List<File>) {
        files.clear()
        files.addAll(newFiles)
        files.sortByDescending { it.lastModified() }
        notifyDataSetChanged()
    }

    fun getSelectedFiles(): List<File> = selected.toList()

    fun clearSelection() {
        selected.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = parent.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.row_export, parent, false)

        return FileViewHolder(rowView)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file, selected.contains(file))
        holder.itemView.setOnClickListener {
            if (selected.size > 0) {
                if (selected.contains(file)) selected.remove(file) else selected.add(file)
                notifyItemChanged(position)
            } else {
                onClickRow(file)
            }
        }
        holder.itemView.findViewById<View>(R.id.share).setOnClickListener {
            onClickShareButton(file)
        }
        holder.itemView.setOnLongClickListener {
            if (selected.contains(file)) selected.remove(file) else selected.add(file)
            notifyItemChanged(position)
            true
        }
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.filename)
        private val time = itemView.findViewById<TextView>(R.id.created_at)
        private val size = itemView.findViewById<TextView>(R.id.file_size)

        fun bind(file: File, isSelected: Boolean) {
            name.text = file.name
            time.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(Date(file.lastModified()))
            size.text = getFileSize(file)

            itemView.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.TRANSPARENT)
        }

        fun getFileSize(file: File): String {
            val sizeInBytes = file.length()

            // If size is less than 1MB, show it in KB
            return when {
                sizeInBytes < 1024 -> "${sizeInBytes} B"
                sizeInBytes < 1024 * 1024 -> "${DecimalFormat("#.##").format(sizeInBytes / 1024.0)} KB"
                sizeInBytes < 1024 * 1024 * 1024 -> "${DecimalFormat("#.##").format(sizeInBytes / (1024.0 * 1024))} MB"
                else -> "${DecimalFormat("#.##").format(sizeInBytes / (1024.0 * 1024 * 1024))} GB"
            }
        }
    }
}