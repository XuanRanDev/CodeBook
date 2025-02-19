package dev.xuanran.codebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class App(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val accountName: String,
    val encryptedPassword: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)