package dev.xuanran.codebook.data.dao

import androidx.room.*
import dev.xuanran.codebook.model.App
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY updatedAt DESC")
    fun getAllApps(): Flow<List<App>>

    @Query("SELECT * FROM apps WHERE id = :id")
    suspend fun getAppById(id: Long): App?

    @Insert
    suspend fun insert(app: App): Long

    @Update
    suspend fun update(app: App)

    @Delete
    suspend fun delete(app: App)

    @Query("""
        SELECT * FROM apps 
        WHERE appName LIKE '%' || :query || '%' 
        OR accountName LIKE '%' || :query || '%'
        OR packageNames LIKE '%' || :query || '%'
        OR url LIKE '%' || :query || '%'
        OR remark LIKE '%' || :query || '%'
    """)
    fun searchApps(query: String): Flow<List<App>>
} 