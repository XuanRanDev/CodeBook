package dev.xuanran.codebook.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "apps")
data class App(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val accountName: String,
    val encryptedPassword: String,
    val packageNames: String? = null,
    val url: String? = null,
    val remark: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable