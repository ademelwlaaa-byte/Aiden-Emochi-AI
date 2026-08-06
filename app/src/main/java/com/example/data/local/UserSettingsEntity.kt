package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val customApiKey: String = "", // Gemini Key
    val groqApiKey: String = "",
    val claudeApiKey: String = "",
    val openaiApiKey: String = "",
    val backupApiKey: String = "",
    val selectedProvider: String = "gemini", // "gemini", "groq", "claude", "openai"
    val selectedModel: String = "gemini-2.0-flash",
    val fallbackModel: String = "gemini-1.5-flash",
    val responseLength: String = "standard", // "short", "standard", "long"
    val enableNsfw: Boolean = true, // +18 / Filtresiz RP modu
    val enableAutoFallback: Boolean = true,
    val enableTts: Boolean = true,
    val totalPromptTokens: Long = 0L,
    val totalCandidateTokens: Long = 0L
)

