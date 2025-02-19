package dev.xuanran.codebook.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.databinding.DialogPasswordGeneratorBinding

class PasswordGeneratorDialog : BottomSheetDialogFragment() {
    private var _binding: DialogPasswordGeneratorBinding? = null
    private val binding get() = _binding!!

    private var onGenerate: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGenerate.setOnClickListener {
            val length = binding.sliderLength.value.toInt()
            val useUpperCase = binding.switchUppercase.isChecked
            val useLowerCase = binding.switchLowercase.isChecked
            val useNumbers = binding.switchNumbers.isChecked
            val useSpecial = binding.switchSpecial.isChecked

            val password = generatePassword(length, useUpperCase, useLowerCase, useNumbers, useSpecial)
            onGenerate?.invoke(password)
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun generatePassword(
        length: Int,
        useUpperCase: Boolean,
        useLowerCase: Boolean,
        useNumbers: Boolean,
        useSpecial: Boolean
    ): String {
        val upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val special = "!@#$^*()_+-=[]|:,.<>?"

        val charPool = StringBuilder().apply {
            if (useUpperCase) append(upperCase)
            if (useLowerCase) append(lowerCase)
            if (useNumbers) append(numbers)
            if (useSpecial) append(special)
        }.toString()

        if (charPool.isEmpty()) {
            return ""
        }

        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(onGenerate: (String) -> Unit): PasswordGeneratorDialog {
            return PasswordGeneratorDialog().apply {
                this.onGenerate = onGenerate
            }
        }
    }
} 