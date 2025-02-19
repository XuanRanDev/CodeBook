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
import dev.xuanran.codebook.databinding.FragmentAppListBinding
import dev.xuanran.codebook.model.App
import dev.xuanran.codebook.ui.adapter.AppAdapter
import dev.xuanran.codebook.ui.dialog.AppEditDialog
import dev.xuanran.codebook.ui.interfaces.FabClickListener
import dev.xuanran.codebook.ui.viewmodel.AppUiState
import dev.xuanran.codebook.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class AppListFragment : Fragment(), FabClickListener {
    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!
    val viewModel: AppViewModel by viewModels()
    private lateinit var adapter: AppAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter(
            onCopyClick = { app ->
                val password = viewModel.getDecryptedPassword(app)
                copyToClipboard(password)
                Toast.makeText(requireContext(), "密码已复制", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { app ->
                showEditDialog(app)
            }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun showEditDialog(app: App? = null) {
        AppEditDialog.newInstance(
            app = app,
            onSave = { appName, accountName, password ->
                if (app == null) {
                    viewModel.addApp(appName, accountName, password)
                } else {
                    viewModel.updateApp(app.copy(
                        appName = appName,
                        accountName = accountName
                    ), password)
                }
            }
        ).show(childFragmentManager, "app_edit")
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AppUiState.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            binding.loadingView.visibility = View.GONE
                            
                            if (state.apps.isEmpty()) {
                                binding.emptyView.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                            } else {
                                binding.emptyView.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                adapter.submitList(state.apps)
                            }
                        }
                        is AppUiState.Error -> {
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
                        AppUiState.Loading -> {
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
            viewModel.loadApps(isRefreshing = true)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("密码", text)
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