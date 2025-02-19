package dev.xuanran.codebook.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.xuanran.codebook.databinding.ItemTotpBinding
import dev.xuanran.codebook.model.Totp

class TotpAdapter(
    private val onCopyClick: (Totp) -> Unit,
    private val onItemLongClick: (Totp) -> Unit,
    private val onTotpCodeGenerated: (Totp, String) -> Unit
) : ListAdapter<Totp, TotpAdapter.ViewHolder>(TotpDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTotpBinding.inflate(
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
        private val binding: ItemTotpBinding
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

        fun bind(totp: Totp) {
            binding.apply {
                tvAppName.text = totp.appName
                tvAccountName.text = totp.accountName
                
                // 计算剩余时间百分比
                val currentTime = System.currentTimeMillis()
                val progress = ((totp.expiryTime - currentTime) / 300f * 100).toInt()
                    .coerceIn(0, 100)
                progressExpiry.progress = progress

                // TODO: 生成TOTP代码
                val totpCode = "123456" // 这里需要实现真实的TOTP生成逻辑
                tvTotpCode.text = totpCode.chunked(3).joinToString(" ")
                onTotpCodeGenerated(totp, totpCode)
            }
        }
    }

    private class TotpDiffCallback : DiffUtil.ItemCallback<Totp>() {
        override fun areItemsTheSame(oldItem: Totp, newItem: Totp): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Totp, newItem: Totp): Boolean {
            return oldItem == newItem
        }
    }
} 