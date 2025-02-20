package dev.xuanran.codebook.ui.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dev.xuanran.codebook.databinding.ItemTotpBinding
import dev.xuanran.codebook.model.Totp
import dev.xuanran.codebook.utils.TotpGenerator
import android.view.animation.LinearInterpolator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter

class TotpAdapter(
    private val onCopyClick: (Totp) -> Unit,
    private val onItemLongClick: (Totp) -> Unit,
    private val onItemClick: (Totp) -> Unit,
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

        private var animator: ValueAnimator? = null

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

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(totp: Totp) {
            binding.apply {
                tvAppName.text = totp.appName
                tvAccountName.text = totp.accountName
                
                // 生成TOTP代码
                val code = TotpGenerator.generateTOTP(
                    secret = totp.secretKey,
                    algorithm = totp.algorithm,
                    digits = totp.digits,
                    period = totp.period
                )
                tvTotpCode.text = code.chunked(3).joinToString(" ")
                
                // 取消之前的动画
                animator?.cancel()
                
                // 创建新的进度条动画
                animator = ValueAnimator.ofInt(0, 100).apply {
                    duration = totp.period * 1000L // 动画持续整个周期
                    interpolator = LinearInterpolator()
                    addUpdateListener { animation ->
                        progressExpiry.progress = animation.animatedValue as Int
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            // 动画结束时重新开始
                            if (!animation.isStarted) {
                                start()
                            }
                        }
                    })
                    start()
                }
                
                onTotpCodeGenerated(totp, code)
            }
        }

        fun onViewRecycled() {
            animator?.cancel()
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
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