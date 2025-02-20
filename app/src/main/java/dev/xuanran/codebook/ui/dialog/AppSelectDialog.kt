package dev.xuanran.codebook.ui.dialog

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.xuanran.codebook.R
import dev.xuanran.codebook.databinding.DialogAppSelectBinding
import dev.xuanran.codebook.ui.adapter.AppInfo
import dev.xuanran.codebook.ui.adapter.AppSelectAdapter

class AppSelectDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAppSelectBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AppSelectAdapter
    private var selectedPackage: String? = null
    private var onConfirm: ((String?, String?) -> Unit)? = null
    private var lastCheckedPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        loadInstalledApps()
        updateConfirmButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_cancel -> {
                    dismiss()
                    true
                }
                R.id.action_confirm -> {
                    onConfirm?.invoke(selectedPackage, null)
                    dismiss()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AppSelectAdapter(
            initialSelectedPackage = selectedPackage,
            onItemSelected = { packageName, appName, position, isChecked ->
                if (isChecked) {
                    if (lastCheckedPosition != -1 && lastCheckedPosition != position) {
                        adapter.notifyItemChanged(lastCheckedPosition)
                    }
                    selectedPackage = packageName
                    lastCheckedPosition = position
                } else {
                    if (position == lastCheckedPosition) {
                        selectedPackage = null
                        lastCheckedPosition = -1
                    }
                }
                updateConfirmButton()
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AppSelectDialog.adapter
        }
    }

    private fun loadInstalledApps() {
        val pm = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { AppInfo(
                packageName = it.packageName,
                appName = pm.getApplicationLabel(it).toString(),
                icon = pm.getApplicationIcon(it.packageName)
            )}
            .sortedBy { it.appName }
        
        adapter.submitList(installedApps)
    }

    private fun updateConfirmButton() {
        binding.toolbar.menu.findItem(R.id.action_confirm)?.apply {
            isEnabled = selectedPackage != null
            title = if (selectedPackage != null) "确定" else "确定"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            selectedPackage: String? = null,
            onConfirm: (packageName: String?, appName: String?) -> Unit
        ): AppSelectDialog {
            return AppSelectDialog().apply {
                this.selectedPackage = selectedPackage
                this.onConfirm = onConfirm
            }
        }
    }
} 