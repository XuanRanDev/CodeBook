package dev.xuanran.codebook.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.FragmentAppDetailBinding
import dev.xuanran.codebook.model.App
import dev.xuanran.codebook.ui.adapter.LinkedAppAdapter
import dev.xuanran.codebook.ui.viewmodel.AppViewModel

class AppDetailDialogFragment : DialogFragment() {

    private var _binding: FragmentAppDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: App
    private lateinit var linkedAppAdapter: LinkedAppAdapter

    private val viewModel: AppViewModel by activityViewModels()

    private var isPasswordVisible = false
    private var decryptedPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_CodeBook_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        app = requireArguments().getParcelable(ARG_APP)!!
        
        setupToolbar()
        setupAppInfo()
        setupLinkedApps()
        setupOtherInfo()
        setupViews()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setupAppInfo() {
        binding.apply {
            tvAppName.text = app.appName
            tvAccountName.text = app.accountName
            
            // 如果有包名，尝试获取应用图标
            val packageName = app.packageName // 先保存到本地变量
            if (packageName != null) {
                try {
                    val icon = requireContext().packageManager.getApplicationIcon(packageName)
                    ivAppIcon.setImageDrawable(icon)
                } catch (e: PackageManager.NameNotFoundException) {
                    ivAppIcon.setImageResource(R.mipmap.ic_launcher)
                }
            } else {
                ivAppIcon.setImageResource(R.mipmap.ic_launcher)
            }

            // 设置复制账号按钮点击事件
            btnCopyAccount.setOnClickListener {
                copyToClipboard(app.accountName)
                showCopySuccessMessage("账号已复制到剪贴板")
            }

            // 设置密码显示/隐藏按钮点击事件
            btnTogglePassword.setOnClickListener {
                togglePasswordVisibility()
            }

            // 设置复制密码按钮点击事件
            btnCopyPassword.setOnClickListener {
                copyPassword()
            }

            // 设置删除按钮点击事件
            btnDelete.setOnClickListener {
                showDeleteConfirmDialog()
            }
        }
    }

    private fun setupLinkedApps() {
        linkedAppAdapter = LinkedAppAdapter(requireContext())
        
        binding.rvLinkedApps.apply {
            adapter = linkedAppAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        // 获取关联的应用
        val packageName = app.packageName?.let { listOf(it) } ?: emptyList()
        linkedAppAdapter.submitList(packageName)
    }

    private fun setupOtherInfo() {
        binding.apply {
            tvUrl.text = app.url
            tvRemark.text = app.remark
        }
    }

    private fun setupViews() {
        app?.let { app ->
            // 设置 URL
            binding.apply {
                app.url?.let { url ->
                    layoutUrl.visibility = View.VISIBLE
                    tvUrl.text = url
                } ?: run {
                    layoutUrl.visibility = View.GONE
                }
                
                // 设置备注
                app.remark?.let { remark ->
                    layoutRemark.visibility = View.VISIBLE
                    tvRemark.text = remark
                } ?: run {
                    layoutRemark.visibility = View.GONE
                }
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            // URL 相关按钮
            btnCopyUrl.setOnClickListener {
                app?.url?.let { url ->
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("URL", url)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "已复制URL", Toast.LENGTH_SHORT).show()
                }
            }

            btnOpenUrl.setOnClickListener {
                app?.url?.let { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "无法打开此URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 备注复制按钮
            btnCopyRemark.setOnClickListener {
                app?.remark?.let { remark ->
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("备注", remark)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "已复制备注", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("密码", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showCopySuccessMessage(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？此操作不可恢复。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteApp(app)
                dismiss()
            }
            .show()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        // 延迟获取密码,仅在需要时解密
        if (isPasswordVisible && decryptedPassword == null) {
            // TODO: 实现密码解密
            decryptedPassword = "decrypted_password"
        }
        
        binding.apply {
            // 更新密码文本
            tvPassword.text = if (isPasswordVisible) {
                decryptedPassword
            } else {
                "••••••••"
            }
            
            // 更新图标
            btnTogglePassword.setIconResource(
                if (isPasswordVisible) {
                    R.drawable.ic_visibility_off
                } else {
                    R.drawable.ic_visibility
                }
            )
        }
    }

    private fun copyPassword() {
        // 延迟获取密码,仅在需要时解密
        if (decryptedPassword == null) {
            // TODO: 实现密码解密
            decryptedPassword = "decrypted_password"
        }
        
        copyToClipboard(decryptedPassword!!)
        showCopySuccessMessage("密码已复制到剪贴板")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_APP = "app"

        fun newInstance(app: App): AppDetailDialogFragment {
            return AppDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_APP, app)
                }
            }
        }
    }
} 