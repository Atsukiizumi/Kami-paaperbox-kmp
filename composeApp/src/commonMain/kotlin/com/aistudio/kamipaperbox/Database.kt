package com.aistudio.kamipaperbox

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vault")
data class VaultEntity(
    @PrimaryKey val key: String,
    val source: String,
    val id: String,
    val title: String,
    val author: String,
    val thumb: String,
    val originalUrl: String,
    val savedAt: Long,
    val tags: String // Comma separated
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val key: String,
    val source: String,
    val id: String,
    val title: String,
    val thumb: String,
    val originalUrl: String,
    val viewedAt: Long
)

@Dao
interface GalleryDao {
    @Query("SELECT * FROM vault ORDER BY savedAt DESC")
    fun getVaultItems(): Flow<List<VaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultEntity)

    @Query("DELETE FROM vault WHERE `key` = :key")
    suspend fun deleteVaultItem(key: String)

    @Query("SELECT EXISTS(SELECT 1 FROM vault WHERE `key` = :key)")
    suspend fun isVaultItem(key: String): Boolean

    @Query("SELECT * FROM history ORDER BY viewedAt DESC LIMIT 100")
    fun getHistoryItems(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Database(entities = [VaultEntity::class, HistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun galleryDao(): GalleryDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .build()
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
