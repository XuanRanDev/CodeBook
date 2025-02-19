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
    }

    private fun setupViews() {
        app?.let { app ->
            binding.etAppName.setText(app.appName)
            binding.etAccountName.setText(app.accountName)
        }
    }

    private fun setupValidation() {
        var isValid = false

        fun validateInputs() {
            isValid = binding.etAppName.text?.isNotBlank() == true &&
                    binding.etAccountName.text?.isNotBlank() == true &&
                    binding.etPassword.text?.isNotBlank() == true
        }

        binding.etAppName.doAfterTextChanged { validateInputs() }
        binding.etAccountName.doAfterTextChanged { validateInputs() }
        binding.etPassword.doAfterTextChanged { validateInputs() }
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