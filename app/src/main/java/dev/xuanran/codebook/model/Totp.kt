package dev.xuanran.codebook.model;

import android.os.Parcelable
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import kotlinx.parcelize.Parcelize;

@Parcelize
@Entity(tableName = "totp")
data class Totp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val accountName: String,
    val secretKey: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val issuer: String? = null,
    val url: String? = null,
    val remark: String? = null,
    val lastUsed: Long = System.currentTimeMillis(),
    val expiryTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
