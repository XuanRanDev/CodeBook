package dev.xuanran.codebook.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.databinding.DialogPasswordBinding

class PasswordDialog : BottomSheetDialogFragment() {
    private var _binding: DialogPasswordBinding? = null
    private val binding get() = _binding!!
    private var onPasswordEntered: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            val password = binding.etPassword.text.toString()
            onPasswordEntered?.invoke(password)
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
        fun newInstance(onPasswordEntered: (String) -> Unit): PasswordDialog {
            return PasswordDialog().apply {
                this.onPasswordEntered = onPasswordEntered
            }
        }
    }
} 