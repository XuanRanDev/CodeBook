package dev.xuanran.codebook.ui.dialog

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.DialogTotpEditBinding
import dev.xuanran.codebook.model.Totp
import android.content.Intent
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.google.zxing.integration.android.IntentIntegrator
import dev.xuanran.codebook.ui.activity.CustomScanActivity

class TotpEditDialog : BottomSheetDialogFragment() {
    private var _binding: DialogTotpEditBinding? = null
    private val binding get() = _binding!!

    private var totp: Totp? = null
    private var onSave: ((Totp) -> Unit)? = null
    private var isExpanded = false

    private val algorithms = listOf("SHA1", "SHA256", "SHA512")

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
        setupToolbar()
        setupViews()
        setupValidation()
        setupButtons()
        setupAlgorithmDropdown()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (totp == null) "添加TOTP验证码" else "编辑TOTP验证码"
    }

    private fun setupViews() {
        totp?.let { totp ->
            binding.apply {
                etAppName.setText(totp.appName)
                etAccountName.setText(totp.accountName)
                etSecretKey.setText(totp.secretKey)
                etIssuer.setText(totp.issuer)
                etAlgorithm.setText(totp.algorithm)
                etDigits.setText(totp.digits.toString())
                etPeriod.setText(totp.period.toString())
                etUrl.setText(totp.url)
                etRemark.setText(totp.remark)
            }
        } ?: run {
            // 设置默认值
            binding.apply {
                etAlgorithm.setText("SHA1")
                etDigits.setText("6")
                etPeriod.setText("30")
            }
        }
    }

    private fun setupValidation() {
        fun validateInputs() {
            val isValid = binding.apply {
                val appName = etAppName.text?.isNotBlank() == true
                val accountName = etAccountName.text?.isNotBlank() == true
                val secretKey = etSecretKey.text?.isNotBlank() == true
                val digits = etDigits.text?.toString()?.toIntOrNull() in 6..8
                val period = etPeriod.text?.toString()?.toIntOrNull() in 15..60

                btnSave.isEnabled = appName && accountName && secretKey && digits && period
            }
        }

        binding.apply {
            etAppName.doAfterTextChanged { validateInputs() }
            etAccountName.doAfterTextChanged { validateInputs() }
            etSecretKey.doAfterTextChanged { validateInputs() }
            etDigits.doAfterTextChanged { validateInputs() }
            etPeriod.doAfterTextChanged { validateInputs() }
        }
    }

    private fun setupAlgorithmDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            algorithms
        )
        binding.etAlgorithm.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.apply {
            btnExpand.setOnClickListener {
                isExpanded = !isExpanded
                layoutAdditionalFields.visibility = if (isExpanded) View.VISIBLE else View.GONE
                btnExpand.apply {
                    text = if (isExpanded) "隐藏高级选项" else "显示高级选项"
                    icon = ContextCompat.getDrawable(
                        requireContext(),
                        if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                    )
                }
            }

            btnScan.setOnClickListener {
                requestCameraPermissionAndScan()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            btnSave.setOnClickListener {
                saveTotp()
            }
        }
    }

    private fun requestCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startQRScanner()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner()
            } else {
                Toast.makeText(requireContext(), "需要相机权限来扫描二维码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("将二维码放入框内扫描")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CustomScanActivity::class.java)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            try {
                val uri = Uri.parse(result.contents)
                if (uri.scheme == "otpauth" && uri.host == "totp") {
                    parseAndFillTotpUri(uri)
                } else {
                    Toast.makeText(requireContext(), "无效的TOTP二维码", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "解析二维码失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseAndFillTotpUri(uri: Uri) {
        // otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP&issuer=Example&algorithm=SHA1&digits=6&period=30
        binding.apply {
            val path = uri.path?.removePrefix("/") ?: return
            val parts = path.split(":")
            if (parts.size == 2) {
                etIssuer.setText(parts[0])
                etAccountName.setText(parts[1])
            } else {
                etAccountName.setText(path)
            }

            uri.getQueryParameter("secret")?.let { etSecretKey.setText(it) }
            uri.getQueryParameter("issuer")?.let { etIssuer.setText(it) }
            uri.getQueryParameter("algorithm")?.let { etAlgorithm.setText(it) }
            uri.getQueryParameter("digits")?.let { etDigits.setText(it) }
            uri.getQueryParameter("period")?.let { etPeriod.setText(it) }
        }
    }

    private fun saveTotp() {
        val newTotp = Totp(
            id = totp?.id ?: 0,
            appName = binding.etAppName.text.toString(),
            accountName = binding.etAccountName.text.toString(),
            secretKey = binding.etSecretKey.text.toString(),
            algorithm = binding.etAlgorithm.text.toString(),
            digits = binding.etDigits.text.toString().toInt(),
            period = binding.etPeriod.text.toString().toInt(),
            issuer = binding.etIssuer.text.toString().takeIf { it.isNotBlank() },
            url = binding.etUrl.text.toString().takeIf { it.isNotBlank() },
            remark = binding.etRemark.text.toString().takeIf { it.isNotBlank() },
            lastUsed = totp?.lastUsed ?: System.currentTimeMillis(),
            expiryTime = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
            createdAt = totp?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        onSave?.invoke(newTotp)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100

        fun newInstance(
            totp: Totp? = null,
            onSave: (Totp) -> Unit
        ): TotpEditDialog {
            return TotpEditDialog().apply {
                this.totp = totp
                this.onSave = onSave
            }
        }
    }
} 