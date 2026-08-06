package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bots ORDER BY updatedAt DESC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots")
    suspend fun getAllBotsList(): List<BotEntity>

    @Query("SELECT * FROM bots WHERE id = :id")
    suspend fun getBotById(id: String): BotEntity?

    @Query("SELECT * FROM bots WHERE id = :id")
    fun getBotByIdFlow(id: String): Flow<BotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(bot: BotEntity)

    @Query("DELETE FROM bots WHERE id = :id")
    suspend fun deleteBotById(id: String)

    @Query("DELETE FROM bots")
    suspend fun deleteAllBots()
}
