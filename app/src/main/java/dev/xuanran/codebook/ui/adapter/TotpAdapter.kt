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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

class TotpAdapter(
    private val onCopyClick: (Totp) -> Unit,
    private val onItemLongClick: (Totp) -> Unit,
    private val onItemClick: (Totp) -> Unit
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

        private var progressAnimator: ValueAnimator? = null
        private var updateJob: Job? = null

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
                
                // 取消之前的动画和更新
                progressAnimator?.cancel()
                updateJob?.cancel()

                // 设置进度条动画
                setupProgressAnimation(totp)
            }
        }

        private fun setupProgressAnimation(totp: Totp) {
            val period = totp.period * 1000L // 转换为毫秒
            updateJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    val currentTimeMillis = System.currentTimeMillis()
                    val timeStep = currentTimeMillis / period // 获取当前时间步
                    val elapsedTime = currentTimeMillis % period
                    
                    // 计算进度 - 从0到30000
                    val progress = (elapsedTime * 30000f / period).toInt()

                    // 如果接近更新时间，更新验证码
                    if (elapsedTime < 100 || elapsedTime > period - 100) {
                        notifyItemChanged(adapterPosition)
                    }

                    // 设置平滑动画
                    progressAnimator = ValueAnimator.ofInt(progress, 30000).apply {
                        duration = period - elapsedTime
                        interpolator = LinearInterpolator()
                        addUpdateListener { animation ->
                            binding.progressExpiry.progress = animation.animatedValue as Int
                        }
                        start()
                    }

                    delay(period - elapsedTime)
                }
            }
        }

        fun cleanup() {
            progressAnimator?.cancel()
            updateJob?.cancel()
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
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