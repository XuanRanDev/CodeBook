package dev.xuanran.codebook.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import dev.xuanran.codebook.data.database.CodeBookDatabase
import dev.xuanran.codebook.model.App
import dev.xuanran.codebook.model.Totp
import dev.xuanran.codebook.security.AESCrypt
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/**
 * TODO 数据的导入导出
 */

class BackupManager(private val context: Context) {

    private val gson = Gson()
    private val database = CodeBookDatabase.getDatabase(context)

    data class BackupData(
        val apps: List<App>,
        val totps: List<Totp>
    )

    suspend fun exportData(uri: Uri, password: String) {
        /*val apps = database.appDao().getAllApps().value
        val totps = database.totpDao().getAllTotps().value

        val backupData = BackupData(apps, totps)
        val jsonData = gson.toJson(backupData)
        val encryptedData = AESCrypt.encrypt(password, jsonData)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(encryptedData)
            }
        }*/
    }

    suspend fun importData(uri: Uri, password: String) {
 /*       val encryptedData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw Exception("无法读取文件")

        val jsonData = AESCrypt.decrypt(password, encryptedData)
        val backupData = gson.fromJson(jsonData, BackupData::class.java)

        // 清空现有数据
        database.appDao().deleteAll()
        database.totpDao().deleteAll()

        // 导入新数据
        backupData.apps.forEach { app ->
            database.appDao().insert(app)
        }
        backupData.totps.forEach { totp ->
            database.totpDao().insert(totp)
        }*/
    }
} 