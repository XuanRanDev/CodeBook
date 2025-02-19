package dev.xuanran.codebook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.xuanran.codebook.data.database.CodeBookDatabase
import dev.xuanran.codebook.data.repository.AppRepository
import dev.xuanran.codebook.model.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState

    init {
        val database = CodeBookDatabase.getDatabase(application)
        repository = AppRepository(database.appDao(), application)
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            repository.allApps
                .catch { 
                    _uiState.value = AppUiState.Error(it.message ?: "未知错误")
                }
                .collect { apps ->
                    _uiState.value = AppUiState.Success(apps)
                }
        }
    }

    fun addApp(appName: String, accountName: String, password: String) {
        viewModelScope.launch {
            try {
                repository.insert(appName, accountName, password)
            } catch (e: Exception) {
                _uiState.value = AppUiState.Error(e.message ?: "添加失败")
            }
        }
    }

    fun updateApp(app: App, newPassword: String? = null) {
        viewModelScope.launch {
            try {
                repository.update(app, newPassword)
            } catch (e: Exception) {
                _uiState.value = AppUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun deleteApp(app: App) {
        viewModelScope.launch {
            try {
                repository.delete(app)
            } catch (e: Exception) {
                _uiState.value = AppUiState.Error(e.message ?: "删除失败")
            }
        }
    }

    fun searchApps(query: String) {
        viewModelScope.launch {
            repository.searchApps(query)
                .catch { 
                    _uiState.value = AppUiState.Error(it.message ?: "搜索失败")
                }
                .collect { apps ->
                    _uiState.value = AppUiState.Success(apps)
                }
        }
    }

    fun getDecryptedPassword(app: App): String {
        return repository.getDecryptedPassword(app)
    }
}

sealed class AppUiState {
    object Loading : AppUiState()
    data class Success(val apps: List<App>) : AppUiState()
    data class Error(val message: String) : AppUiState()
} 