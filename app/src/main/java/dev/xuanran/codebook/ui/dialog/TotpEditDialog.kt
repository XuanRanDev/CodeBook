package dev.xuanran.codebook.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.databinding.DialogTotpEditBinding
import dev.xuanran.codebook.model.Totp

class TotpEditDialog : BottomSheetDialogFragment() {
    private var _binding: DialogTotpEditBinding? = null
    private val binding get() = _binding!!

    private var totp: Totp? = null
    private var onSave: ((String, String, String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTotpEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupValidation()
        setupButtons()
    }

    private fun setupViews() {
        totp?.let { totp ->
            binding.etAppName.setText(totp.appName)
            binding.etAccountName.setText(totp.accountName)
        }
    }

    private fun setupValidation() {
        var isValid = false

        fun validateInputs() {
            isValid = binding.etAppName.text?.isNotBlank() == true &&
                    binding.etAccountName.text?.isNotBlank() == true &&
                    binding.etSecretKey.text?.isNotBlank() == true
            
            binding.btnSave.isEnabled = isValid
        }

        binding.etAppName.doAfterTextChanged { validateInputs() }
        binding.etAccountName.doAfterTextChanged { validateInputs() }
        binding.etSecretKey.doAfterTextChanged { validateInputs() }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val appName = binding.etAppName.text.toString()
            val accountName = binding.etAccountName.text.toString()
            val secretKey = binding.etSecretKey.text.toString()
            onSave?.invoke(appName, accountName, secretKey)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnScan.setOnClickListener {
            // TODO: 实现二维码扫描
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            totp: Totp? = null,
            onSave: (appName: String, accountName: String, secretKey: String) -> Unit
        ): TotpEditDialog {
            return TotpEditDialog().apply {
                this.totp = totp
                this.onSave = onSave
            }
        }
    }
} 