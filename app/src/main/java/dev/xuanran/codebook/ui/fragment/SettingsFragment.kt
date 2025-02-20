package dev.xuanran.codebook.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dev.xuanran.codebook.R
import dev.xuanran.codebook.ui.dialog.PasswordDialog
import dev.xuanran.codebook.utils.BackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : PreferenceFragmentCompat() {

    private val backupManager by lazy { BackupManager(requireContext()) }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showPasswordDialog { password ->
                    exportData(uri, password)
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showPasswordDialog { password ->
                    importData(uri, password)
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("export_data")?.setOnPreferenceClickListener {
            startExport()
            true
        }

        findPreference<Preference>("import_data")?.setOnPreferenceClickListener {
            startImport()
            true
        }
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "codebook_backup.json")
        }
        exportLauncher.launch(intent)
    }

    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun showPasswordDialog(onPasswordEntered: (String) -> Unit) {
        PasswordDialog.newInstance { password ->
            if (password.length >= 16) {
                onPasswordEntered(password)
            } else {
                Toast.makeText(requireContext(), "密码长度必须至少16位", Toast.LENGTH_SHORT).show()
            }
        }.show(childFragmentManager, "password_dialog")
    }

    private fun exportData(uri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                backupManager.exportData(uri, password)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导出成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导出失败: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun importData(uri: Uri, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                backupManager.importData(uri, password)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导入成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导入失败: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
} 