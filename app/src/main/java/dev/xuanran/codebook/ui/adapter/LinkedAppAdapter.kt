package dev.xuanran.codebook.ui.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.ItemLinkedAppBinding

class LinkedAppAdapter(
    private val context: Context
) : ListAdapter<String, LinkedAppAdapter.ViewHolder>(PackageDiffCallback()) {

    private val packageManager = context.packageManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLinkedAppBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLinkedAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(packageName: String) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                binding.apply {
                    ivAppIcon.setImageDrawable(appInfo.loadIcon(packageManager))
                    tvAppName.text = appInfo.loadLabel(packageManager)
                    tvPackageName.text = packageName
                    tvStatus.text = ""
                }
            } catch (e: PackageManager.NameNotFoundException) {
                binding.apply {
                    ivAppIcon.setImageResource(R.mipmap.ic_launcher)
                    tvAppName.text = context.getString(R.string.unknown_app)
                    tvPackageName.text = packageName
                    tvStatus.text = context.getString(R.string.not_installed)
                }
            }
        }
    }

    private class PackageDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 