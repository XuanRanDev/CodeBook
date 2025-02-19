package dev.xuanran.codebook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.xuanran.codebook.data.database.CodeBookDatabase
import dev.xuanran.codebook.data.repository.TotpRepository
import dev.xuanran.codebook.model.Totp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TotpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TotpRepository
    private val _uiState = MutableStateFlow<TotpUiState>(TotpUiState.Loading)
    val uiState: StateFlow<TotpUiState> = _uiState

    init {
        val database = CodeBookDatabase.getDatabase(application)
        repository = TotpRepository(database.totpDao(), application)
        loadTotps()
    }

    private fun loadTotps() {
        viewModelScope.launch {
            repository.allTotps
                .catch { 
                    _uiState.value = TotpUiState.Error(it.message ?: "未知错误")
                }
                .collect { totps ->
                    _uiState.value = TotpUiState.Success(totps)
                }
        }
    }

    fun addTotp(appName: String, accountName: String, secretKey: String) {
        viewModelScope.launch {
            try {
                repository.insert(appName, accountName, secretKey)
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "添加失败")
            }
        }
    }

    fun updateTotp(totp: Totp, newSecretKey: String? = null) {
        viewModelScope.launch {
            try {
                repository.update(totp, newSecretKey)
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun updateLastUsed(totp: Totp) {
        viewModelScope.launch {
            try {
                repository.updateLastUsed(totp)
            } catch (e: Exception) {
                _uiState.value = TotpUiState.Error(e.message ?: "更新使用时间失败")
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

    fun getDecryptedSecretKey(totp: Totp): String {
        return repository.getDecryptedSecretKey(totp)
    }
}

sealed class TotpUiState {
    object Loading : TotpUiState()
    data class Success(val totps: List<Totp>) : TotpUiState()
    data class Error(val message: String) : TotpUiState()
} 