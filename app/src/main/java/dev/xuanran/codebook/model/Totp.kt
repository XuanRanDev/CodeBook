package dev.xuanran.codebook.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "totp")
data class Totp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val accountName: String,
    val secretKey: String,
    val lastUsed: Long = System.currentTimeMillis(),
    val expiryTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
