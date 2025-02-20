package dev.xuanran.codebook.data.repository

import android.content.Context
import dev.xuanran.codebook.data.dao.AppDao
import dev.xuanran.codebook.model.App
import dev.xuanran.codebook.security.Encryption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(
    private val appDao: AppDao,
    context: Context
) {
    private val encryption = Encryption.getInstance(context)

    val allApps: Flow<List<App>> = appDao.getAllApps()

    suspend fun insert(
        appName: String, 
        accountName: String, 
        password: String,
        packageName: String? = null,
        url: String? = null,
        remark: String? = null
    ): Long {
        val encryptedPassword = encryption.encrypt(password)
        return appDao.insert(
            App(
                appName = appName,
                accountName = accountName,
                encryptedPassword = encryptedPassword,
                packageName = packageName,
                url = url,
                remark = remark
            )
        )
    }

    suspend fun update(app: App, newPassword: String? = null) {
        val updatedApp = if (newPassword != null) {
            app.copy(
                encryptedPassword = encryption.encrypt(newPassword),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            app.copy(updatedAt = System.currentTimeMillis())
        }
        appDao.update(updatedApp)
    }

    suspend fun delete(app: App) {
        appDao.delete(app)
    }

    suspend fun getAppById(id: Long): App? {
        return appDao.getAppById(id)
    }

    fun searchApps(query: String): Flow<List<App>> {
        return appDao.searchApps(query)
    }

    fun getDecryptedPassword(app: App): String {
        return encryption.decrypt(app.encryptedPassword)
    }
} 