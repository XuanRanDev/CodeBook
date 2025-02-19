package dev.xuanran.codebook.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.xuanran.codebook.data.database.CodeBookDatabase
import dev.xuanran.codebook.data.repository.AppRepository
import dev.xuanran.codebook.model.App
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState

    private var currentSortOrder = SortOrder.NAME
    
    init {
        val database = CodeBookDatabase.getDatabase(application)
        repository = AppRepository(database.appDao(), application)
        // 从SharedPreferences加载排序方式
        val prefs = application.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        currentSortOrder = SortOrder.valueOf(prefs.getString("sort_order", SortOrder.NAME.name)!!)
        loadApps()
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
            loadApps()
        }
    }

    fun loadApps(isRefreshing: Boolean = false) {
        _uiState.value = AppUiState.Loading
        viewModelScope.launch {
            if (isRefreshing) {
                delay(2000)
            }
            repository.allApps
                .catch { 
                    _uiState.value = AppUiState.Error(it.message ?: "未知错误")
                }
                .collect { apps ->
                    val sortedApps = when (currentSortOrder) {
                        SortOrder.NAME -> apps.sortedBy { it.appName }
                        SortOrder.TIME -> apps.sortedByDescending { it.createdAt }
                    }
                    _uiState.value = AppUiState.Success(sortedApps)
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