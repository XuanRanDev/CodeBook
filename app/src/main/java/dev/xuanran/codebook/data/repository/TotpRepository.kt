package dev.xuanran.codebook.data.repository

import android.content.Context
import dev.xuanran.codebook.data.dao.TotpDao
import dev.xuanran.codebook.model.Totp
import dev.xuanran.codebook.security.Encryption
import kotlinx.coroutines.flow.Flow

class TotpRepository(
    private val totpDao: TotpDao,
    context: Context
) {
    private val encryption = Encryption.getInstance(context)

    val allTotps: Flow<List<Totp>> = totpDao.getAllTotps()

    suspend fun insert(appName: String, accountName: String, secretKey: String): Long {
        val encryptedSecretKey = encryption.encrypt(secretKey)
        return totpDao.insert(
            Totp(
                appName = appName,
                accountName = accountName,
                secretKey = encryptedSecretKey,
                expiryTime = calculateExpiryTime()
            )
        )
    }

    suspend fun update(totp: Totp, newSecretKey: String? = null) {
        val updatedTotp = if (newSecretKey != null) {
            totp.copy(
                secretKey = encryption.encrypt(newSecretKey),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            totp.copy(updatedAt = System.currentTimeMillis())
        }
        totpDao.update(updatedTotp)
    }

    suspend fun updateLastUsed(totp: Totp) {
        val updatedTotp = totp.copy(
            lastUsed = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        totpDao.update(updatedTotp)
    }

    suspend fun delete(totp: Totp) {
        totpDao.delete(totp)
    }

    suspend fun getTotpById(id: Long): Totp? {
        return totpDao.getTotpById(id)
    }

    fun searchTotps(query: String): Flow<List<Totp>> {
        return totpDao.searchTotps(query)
    }

    fun getDecryptedSecretKey(totp: Totp): String {
        return encryption.decrypt(totp.secretKey)
    }

    private fun calculateExpiryTime(): Long {
        // TOTP 默认30秒过期
        return System.currentTimeMillis() + 30_000
    }
} 