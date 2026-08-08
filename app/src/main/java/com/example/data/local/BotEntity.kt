package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: String,
    val mode: String, // "personal" or "universe"
    val aiName: String,
    val aiPersonality: String,
    val scenario: String,
    val universeName: String,
    val keyCharactersJson: String, // JSON array of characters
    val userCharName: String,
    val userCharDesc: String,
    val openingMessage: String,
    val writingStyle: String, // "rp" or "chat"
    val intensity: String, // "normal" or "intense"
    val customLength: String = "default", // "default", "short", "standard", "long"
    val isNsfw: Boolean = true, // +18 / Filtresiz RP
    val avatarUrl: String = "",
    val chatBgUrl: String = "", // Bot sohbet arka planı
    val isPublic: Boolean = false, // Herkese açık mı yoksa sadece kendine özel mi? (Varsayılan kişiye özel)
    val isTemplate: Boolean = false, // Şablon veya hazır varsayılan bot mu?
    val enableOoc: Boolean = true, // Parantez İçi Yönlendirme / OOC Modu (... Bu böyle olmalı)
    val pinnedMemory: String = "",
    val storyNotes: String = "",
    val memoryNotes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)


