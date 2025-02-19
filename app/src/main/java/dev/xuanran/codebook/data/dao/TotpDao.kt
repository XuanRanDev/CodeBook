package dev.xuanran.codebook.data.dao

import androidx.room.*
import dev.xuanran.codebook.model.Totp
import kotlinx.coroutines.flow.Flow

@Dao
interface TotpDao {
    @Query("SELECT * FROM totp ORDER BY lastUsed DESC")
    fun getAllTotps(): Flow<List<Totp>>

    @Query("SELECT * FROM totp WHERE id = :id")
    suspend fun getTotpById(id: Long): Totp?

    @Insert
    suspend fun insert(totp: Totp): Long

    @Update
    suspend fun update(totp: Totp)

    @Delete
    suspend fun delete(totp: Totp)

    @Query("SELECT * FROM totp WHERE appName LIKE '%' || :query || '%' OR accountName LIKE '%' || :query || '%'")
    fun searchTotps(query: String): Flow<List<Totp>>
} 