package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE botId = :botId ORDER BY timestamp ASC")
    fun getMessagesForBot(botId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE botId = :botId ORDER BY timestamp ASC")
    suspend fun getMessagesForBotList(botId: String): List<MessageEntity>

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesList(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM messages WHERE botId = :botId")
    suspend fun deleteMessagesForBot(botId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
