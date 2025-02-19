package dev.xuanran.codebook.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.DialogAppEditBinding
import dev.xuanran.codebook.model.App
import androidx.core.content.ContextCompat

class AppEditDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAppEditBinding? = null
    private val binding get() = _binding!!

    private var app: App? = null
    private var onSave: ((String, String, String, String?, String?, String?) -> Unit)? = null

    private var selectedPackages = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupValidation()
        setupButtons()
        setupToolbar()
        
        // 设置展开/折叠按钮
        binding.btnExpand.setOnClickListener {
            val isExpanded = binding.layoutAdditionalFields.visibility == View.VISIBLE
            binding.layoutAdditionalFields.visibility = if (isExpanded) View.GONE else View.VISIBLE
            binding.btnExpand.text = if (isExpanded) "显示更多选项" else "隐藏更多选项"
            binding.btnExpand.icon = ContextCompat.getDrawable(
                requireContext(),
                if (isExpanded) R.drawable.ic_expand_more else R.drawable.ic_expand_less
            )
        }

        // 设置应用选择按钮
        binding.btnSelectApps.setOnClickListener {
            AppSelectDialog.newInstance(
                selectedPackages = selectedPackages,
                onConfirm = { packages ->
                    selectedPackages.clear()
                    selectedPackages.addAll(packages)
                    updateSelectedAppsButton()
                }
            ).show(childFragmentManager, "app_select")
        }

        // 如果是编辑模式，加载现有数据
        app?.let { existingApp ->
            binding.etUrl.setText(existingApp.url)
            binding.etRemark.setText(existingApp.remark)
            existingApp.packageNames?.split(",")?.let { packages ->
                selectedPackages.addAll(packages)
                updateSelectedAppsButton()
            }
        }
    }

    private fun setupViews() {
        binding.toolbar.title = if (app == null) "添加账号密码" else "编辑账号密码"
        
        app?.let { app ->
            binding.etAppName.setText(app.appName)
            binding.etAccountName.setText(app.accountName)
        }
    }

    private fun setupValidation() {
        fun validateInputs() {
            val isValid = binding.etAppName.text?.isNotBlank() == true &&
                    binding.etAccountName.text?.isNotBlank() == true &&
                    binding.etPassword.text?.isNotBlank() == true
            
            binding.btnSave.isEnabled = isValid
        }

        binding.etAppName.doAfterTextChanged { validateInputs() }
        binding.etAccountName.doAfterTextChanged { validateInputs() }
        binding.etPassword.doAfterTextChanged { validateInputs() }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveApp()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_app_edit)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_generate_password -> {
                    PasswordGeneratorDialog.newInstance { password ->
                        binding.etPassword.setText(password)
                    }.show(childFragmentManager, "password_generator")
                    true
                }
                else -> false
            }
        }
    }

    private fun updateSelectedAppsButton() {
        binding.btnSelectApps.text = if (selectedPackages.isEmpty()) {
            "选择关联应用"
        } else {
            "已选择 ${selectedPackages.size} 个应用"
        }
    }

    private fun saveApp() {
        val appName = binding.etAppName.text.toString()
        val accountName = binding.etAccountName.text.toString()
        val password = binding.etPassword.text.toString()
        val urls = binding.etUrl.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(",")
            .takeIf { it.isNotEmpty() }
        val remark = binding.etRemark.text.toString().takeIf { it.isNotBlank() }
        val packageNames = selectedPackages.takeIf { it.isNotEmpty() }?.joinToString(",")

        onSave?.invoke(appName, accountName, password, urls, remark, packageNames)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            app: App? = null,
            onSave: (appName: String, accountName: String, password: String, url: String?, remark: String?, packageNames: String?) -> Unit
        ): AppEditDialog {
            return AppEditDialog().apply {
                this.app = app
                this.onSave = onSave
            }
        }
    }
} 