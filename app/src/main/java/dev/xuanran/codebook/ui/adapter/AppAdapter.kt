package dev.xuanran.codebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.xuanran.codebook.databinding.ItemAppBinding
import dev.xuanran.codebook.model.App

class AppAdapter(
    private val onCopyClick: (App) -> Unit,
    private val onItemLongClick: (App) -> Unit
) : ListAdapter<App, AppAdapter.ViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnCopy.setOnClickListener {
                val position = adapterPosition  // 改为 absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCopyClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition  // 改为 absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(app: App) {
            binding.tvAppName.text = app.appName
            binding.tvAccountName.text = app.accountName
        }
    }

    private class AppDiffCallback : DiffUtil.ItemCallback<App>() {
        override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem == newItem
        }
    }
} 