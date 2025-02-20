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
    private val initialSelectedPackage: String?,
    private val onItemSelected: (packageName: String, appName: String, position: Int, isChecked: Boolean) -> Unit
) : ListAdapter<AppInfo, AppSelectAdapter.ViewHolder>(AppDiffCallback()) {

    private var lastCheckedPosition = -1

    init {
        // 找到初始选中的位置
        currentList.forEachIndexed { index, appInfo ->
            if (appInfo.packageName == initialSelectedPackage) {
                lastCheckedPosition = index
                return@forEachIndexed
            }
        }
    }

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
                
                // 设置选中状态
                checkbox.isChecked = app.packageName == initialSelectedPackage
                
                // 整个项目可点击
                root.setOnClickListener {
                    val newCheckedState = !checkbox.isChecked
                    
                    // 如果是选中状态，先取消其他选中项
                    if (newCheckedState && lastCheckedPosition != -1 && lastCheckedPosition != adapterPosition) {
                        notifyItemChanged(lastCheckedPosition)
                    }
                    
                    checkbox.isChecked = newCheckedState
                    lastCheckedPosition = if (newCheckedState) adapterPosition else -1
                    onItemSelected(app.packageName, app.appName, adapterPosition, newCheckedState)
                }
                
                // 禁用 checkbox 的直接点击
                checkbox.isClickable = false
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