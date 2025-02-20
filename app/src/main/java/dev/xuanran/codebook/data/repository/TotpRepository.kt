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

    suspend fun insert(totp: Totp): Long {
        // TODO 在此处加密密钥
        // val encryptedSecretKey = encryption.encrypt(secretKey)
        return totpDao.insert(totp)
    }

    suspend fun update(totp: Totp) {
        // TODO 在此处对密钥进行加密
        var res = totp.copy(updatedAt = System.currentTimeMillis())
        totpDao.update(res)
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