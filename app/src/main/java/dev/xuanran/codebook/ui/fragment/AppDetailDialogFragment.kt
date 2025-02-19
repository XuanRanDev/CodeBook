package dev.xuanran.codebook.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
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
            if (!app.packageNames.isNullOrEmpty()) {
                try {
                    val packageName = app.packageNames!!.split(",")[0]
                    val icon = requireContext().packageManager.getApplicationIcon(packageName)
                    ivAppIcon.setImageDrawable(icon)
                } catch (e: PackageManager.NameNotFoundException) {
                    ivAppIcon.setImageResource(R.mipmap.ic_launcher)
                }
            } else {
                ivAppIcon.setImageResource(R.mipmap.ic_launcher)
            }

            // 设置复制密码按钮点击事件
            btnCopyPassword.setOnClickListener {
                // TODO 解密密码
                val password = "pass"
                copyToClipboard(password)
                showCopySuccessMessage()
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

        // 获取关联的应用列表
        val packageNames = app.packageNames?.split(",") ?: emptyList()
        linkedAppAdapter.submitList(packageNames)
    }

    private fun setupOtherInfo() {
        binding.apply {
            etUrl.setText(app.url)
            etRemark.setText(app.remark)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("密码", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showCopySuccessMessage() {
        Snackbar.make(
            binding.root,
            "密码已复制到剪贴板",
            Snackbar.LENGTH_SHORT
        ).show()
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