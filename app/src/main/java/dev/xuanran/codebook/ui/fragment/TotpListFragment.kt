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
import dev.xuanran.codebook.ui.interfaces.FabClickListener
import dev.xuanran.codebook.ui.viewmodel.TotpUiState
import dev.xuanran.codebook.ui.viewmodel.TotpViewModel
import dev.xuanran.codebook.utils.TotpGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class TotpListFragment : Fragment(), FabClickListener {
    private var _binding: FragmentTotpListBinding? = null
    private val binding get() = _binding!!
    val viewModel: TotpViewModel by viewModels()
    private lateinit var adapter: TotpAdapter

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
        setupSwipeRefresh()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = TotpAdapter(
            onCopyClick = { totp ->
                val code = TotpGenerator.generateTOTP(
                    secret = viewModel.getDecryptedSecretKey(totp),
                    algorithm = totp.algorithm,
                    digits = totp.digits,
                    period = totp.period
                )
                copyToClipboard(code)
                viewModel.updateLastUsed(totp)
                Toast.makeText(requireContext(), "验证码已复制", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { totp ->
                showEditDialog(totp)
            },
            onItemClick = { totp ->
                TotpDetailDialogFragment.newInstance(totp)
                    .show(childFragmentManager, "TotpDetail")
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun showEditDialog(totp: Totp? = null) {
        TotpEditDialog.newInstance(
            totp = totp,
            onSave = { newTotp ->
                if (totp == null) {
                    viewModel.addTotp(newTotp)
                } else {
                    viewModel.updateTotp(newTotp)
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
            binding.emptyView.visibility = View.GONE
            binding.loadingView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            viewModel.loadTotps(isRefreshing = true)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFabClick() {
        showEditDialog()
    }
} 