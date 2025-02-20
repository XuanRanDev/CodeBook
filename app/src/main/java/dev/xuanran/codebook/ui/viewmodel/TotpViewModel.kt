package dev.xuanran.codebook.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.xuanran.codebook.data.database.CodeBookDatabase
import dev.xuanran.codebook.data.repository.TotpRepository
import dev.xuanran.codebook.model.Totp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TotpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TotpRepository
    private val _uiState = MutableStateFlow<TotpUiState>(TotpUiState.Loading)
    val uiState: StateFlow<TotpUiState> = _uiState

    private var currentSortOrder = SortOrder.NAME
    
    init {
        val database = CodeBookDatabase.getDatabase(application)
        repository = TotpRepository(database.totpDao(), application)
        // 从SharedPreferences加载排序方式
        val prefs = application.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        currentSortOrder = SortOrder.valueOf(prefs.getString("sort_order", SortOrder.NAME.name)!!)
        loadTotps()
    }

    fun setSortOrder(order: SortOrder) {
        if (currentSortOrder != order) {
            currentSortOrder = order
            // 保存排序方式到SharedPreferences
            getApplication<Application>().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                .edit()
                .putString("sort_order", order.name)
                .apply()
            // 重新加载数据
            loadTotps()
        }
    }

    fun loadTotps(isRefreshing: Boolean = false) {
        _uiState.value = TotpUiState.Loading
        viewModelScope.launch {
            if (isRefreshing) {
                delay(2000)
            }
            repository.allTotps
                .catch { 
                    _uiState.value = TotpUiState.Error(it.message ?: "未知错误")
                }
                .collect { totps ->
                    val sortedTotps = when (currentSortOrder) {
                        SortOrder.NAME -> totps.sortedBy { it.appName }
                        SortOrder.TIME -> totps.sortedByDescending { it.lastUsed }
                    }
                    _uiState.value = TotpUiState.Success(sortedTotps)
                }
        }
    }

    fun addTotp(totp: Totp) {
        viewModelScope.launch {
            try {
                // 保存到数据库
                repository.insert(totp)
                loadTotps()
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "添加失败")
            }
        }
    }

    fun updateTotp(totp: Totp) {
        viewModelScope.launch {
            try {
                repository.update(totp)
                loadTotps()
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun deleteTotp(totp: Totp) {
        viewModelScope.launch {
            try {
                repository.delete(totp)
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "删除失败")
            }
        }
    }

    fun searchTotps(query: String) {
        viewModelScope.launch {
            repository.searchTotps(query)
                .catch { 
                    _uiState.value = TotpUiState.Error(it.message ?: "搜索失败")
                }
                .collect { totps ->
                    _uiState.value = TotpUiState.Success(totps)
                }
        }
    }

    fun updateLastUsed(totp: Totp) {
        viewModelScope.launch {
            try {
                repository.updateLastUsed(totp)
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun getDecryptedSecretKey(totp: Totp): String {
        return repository.getDecryptedSecretKey(totp)
    }
}

sealed class TotpUiState {
    object Loading : TotpUiState()
    data class Success(val totps: List<Totp>) : TotpUiState()
    data class Error(val message: String) : TotpUiState()
}
