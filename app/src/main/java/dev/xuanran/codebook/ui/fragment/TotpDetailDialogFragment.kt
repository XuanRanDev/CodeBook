package dev.xuanran.codebook.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.FragmentTotpDetailBinding
import dev.xuanran.codebook.model.Totp
import dev.xuanran.codebook.ui.viewmodel.TotpViewModel
import dev.xuanran.codebook.utils.TotpGenerator
import kotlinx.coroutines.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.content.Intent
import android.net.Uri

class TotpDetailDialogFragment : DialogFragment() {

    private var _binding: FragmentTotpDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var totp: Totp
    private val viewModel: TotpViewModel by activityViewModels()
    private var updateJob: Job? = null
    private var progressAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_CodeBook_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotpDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        totp = requireArguments().getParcelable(ARG_TOTP)!!
        
        setupToolbar()
        setupTotpInfo()
        setupButtons()
        startTotpUpdate()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setupTotpInfo() {
        binding.apply {
            tvAppName.text = totp.appName
            tvAccountName.text = totp.accountName
            tvAlgorithm.text = totp.algorithm
            tvDigits.text = "${totp.digits} 位"
            tvPeriod.text = "${totp.period} 秒"
            tvIssuer.text = totp.issuer ?: "未设置"
            
            // 处理 URL 的显示和按钮事件
            if (!totp.url.isNullOrBlank()) {
                layoutUrl.visibility = View.VISIBLE
                tvUrl.text = totp.url
                
                btnCopyUrl.setOnClickListener {
                    copyToClipboard(totp.url!!)
                    showCopySuccessMessage("URL已复制到剪贴板")
                }
                
                btnOpenUrl.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(totp.url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        showCopySuccessMessage("无法打开此URL")
                    }
                }
            } else {
                layoutUrl.visibility = View.GONE
            }
            
            // 处理备注的显示和复制功能
            if (!totp.remark.isNullOrBlank()) {
                layoutRemark.visibility = View.VISIBLE
                tvRemark.text = totp.remark
                
                btnCopyRemark.setOnClickListener {
                    copyToClipboard(totp.remark!!)
                    showCopySuccessMessage("备注已复制到剪贴板")
                }
            } else {
                layoutRemark.visibility = View.GONE
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnCopyCode.setOnClickListener {
                val code = TotpGenerator.generateTOTP(
                    secret = viewModel.getDecryptedSecretKey(totp),
                    algorithm = totp.algorithm,
                    digits = totp.digits,
                    period = totp.period
                )
                copyToClipboard(code)
                viewModel.updateLastUsed(totp)
                showCopySuccessMessage("验证码已复制到剪贴板")
            }

            btnDelete.setOnClickListener {
                showDeleteConfirmDialog()
            }
        }
    }

    private fun startTotpUpdate() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val code = TotpGenerator.generateTOTP(
                    secret = viewModel.getDecryptedSecretKey(totp),
                    algorithm = totp.algorithm,
                    digits = totp.digits,
                    period = totp.period
                )
                binding.tvTotpCode.text = code.chunked(3).joinToString(" ")
                
                // 获取当前时间在周期内的位置
                val currentTimeMillis = System.currentTimeMillis()
                val period = totp.period * 1000L // 转换为毫秒
                val elapsedTime = currentTimeMillis % period
                
                // 取消之前的动画
                progressAnimator?.cancel()
                
                // 设置进度条动画
                progressAnimator = ValueAnimator.ofInt((elapsedTime * 30000f / period).toInt(), 30000).apply {
                    duration = period - elapsedTime
                    interpolator = LinearInterpolator()
                    addUpdateListener { animation ->
                        _binding?.progressExpiry?.progress = animation.animatedValue as Int
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (isActive) {
                                // 重新生成验证码
                                val newCode = TotpGenerator.generateTOTP(
                                    secret = viewModel.getDecryptedSecretKey(totp),
                                    algorithm = totp.algorithm,
                                    digits = totp.digits,
                                    period = totp.period
                                )
                                _binding?.tvTotpCode?.text = newCode.chunked(3).joinToString(" ")
                            }
                        }
                    })
                    start()
                }

                delay(period - elapsedTime)
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showCopySuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？此操作不可恢复。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteTotp(totp)
                dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        progressAnimator?.cancel()
        _binding = null
    }

    companion object {
        private const val ARG_TOTP = "totp"

        fun newInstance(totp: Totp): TotpDetailDialogFragment {
            return TotpDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TOTP, totp)
                }
            }
        }
    }
} 