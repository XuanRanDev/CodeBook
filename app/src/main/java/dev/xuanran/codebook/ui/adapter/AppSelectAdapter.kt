package dev.xuanran.codebook.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.xuanran.codebook.databinding.ItemAppSelectBinding

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)

class AppSelectAdapter(
    private val initialSelectedPackages: Set<String>,
    private val onItemSelected: (String, Boolean) -> Unit
) : ListAdapter<AppInfo, AppSelectAdapter.ViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAppSelectBinding.inflate(
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
        private val binding: ItemAppSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(app: AppInfo) {
            binding.apply {
                ivAppIcon.setImageDrawable(app.icon)
                tvAppName.text = app.appName
                tvPackageName.text = app.packageName
                
                // 设置初始选中状态
                checkbox.isChecked = initialSelectedPackages.contains(app.packageName)
                
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onItemSelected(app.packageName, isChecked)
                }
            }
        }
    }

    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
} 