package dev.xuanran.codebook.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.xuanran.codebook.databinding.FragmentTotpListBinding
import dev.xuanran.codebook.model.Totp
import dev.xuanran.codebook.ui.adapter.TotpAdapter
import dev.xuanran.codebook.ui.dialog.TotpEditDialog
import dev.xuanran.codebook.ui.viewmodel.TotpUiState
import dev.xuanran.codebook.ui.viewmodel.TotpViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TotpListFragment : Fragment() {
    private var _binding: FragmentTotpListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TotpViewModel by viewModels()
    private lateinit var adapter: TotpAdapter
    private var updateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotpListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        observeUiState()
        startPeriodicUpdate()
    }

    private fun setupRecyclerView() {
        adapter = TotpAdapter(
            onCopyClick = { totp ->
                // TODO: 获取当前有效的TOTP代码
                val code = "123456" // 这里需要实现真实的TOTP生成逻辑
                copyToClipboard(code)
                viewModel.updateLastUsed(totp)
                Toast.makeText(requireContext(), "验证码已复制", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { totp ->
                showEditDialog(totp)
            },
            onTotpCodeGenerated = { totp, code ->
                // 可以在这里处理新生成的TOTP代码
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showEditDialog()
        }
    }

    private fun showEditDialog(totp: Totp? = null) {
        TotpEditDialog.newInstance(
            totp = totp,
            onSave = { appName, accountName, secretKey ->
                if (totp == null) {
                    viewModel.addTotp(appName, accountName, secretKey)
                } else {
                    viewModel.updateTotp(totp.copy(
                        appName = appName,
                        accountName = accountName
                    ), secretKey)
                }
            }
        ).show(childFragmentManager, "totp_edit")
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TotpUiState.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.loadingView.visibility = View.GONE
                            
                            if (state.totps.isEmpty()) {
                                binding.emptyView.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            } else {
                                binding.emptyView.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                adapter.submitList(state.totps)
                            }
                        }
                        is TotpUiState.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.loadingView.visibility = View.GONE
                            binding.emptyView.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TotpUiState.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.loadingView.visibility = View.VISIBLE
                                binding.emptyView.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadTotps()
        }
        binding.swipeRefresh.setColorSchemeResources(
            com.google.android.material.R.color.design_default_color_primary
        )
    }

    private fun startPeriodicUpdate() {
        updateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                adapter.notifyDataSetChanged() // 更新所有项目以刷新进度条
                delay(1000) // 每秒更新一次
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        _binding = null
    }
} 