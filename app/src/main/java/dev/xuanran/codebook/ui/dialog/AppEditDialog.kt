package dev.xuanran.codebook.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.databinding.DialogAppEditBinding
import dev.xuanran.codebook.model.App

class AppEditDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAppEditBinding? = null
    private val binding get() = _binding!!

    private var app: App? = null
    private var onSave: ((String, String, String) -> Unit)? = null

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
            val appName = binding.etAppName.text.toString()
            val accountName = binding.etAccountName.text.toString()
            val password = binding.etPassword.text.toString()
            onSave?.invoke(appName, accountName, password)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            app: App? = null,
            onSave: (appName: String, accountName: String, password: String) -> Unit
        ): AppEditDialog {
            return AppEditDialog().apply {
                this.app = app
                this.onSave = onSave
            }
        }
    }
} 