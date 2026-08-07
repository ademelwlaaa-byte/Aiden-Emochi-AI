package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.components.BotSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.components.TypingDots
import com.example.ui.components.customTextFieldColors
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiCoralEnd
import com.example.ui.theme.EmochiCoralStart
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import com.example.ui.theme.EmochiUserBubbleText

@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    userSettings: UserSettingsEntity?,
    isSending: Boolean,
    errorMessage: String?,
    keyCharacters: List<KeyCharacter>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRegenerate: () -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onSaveBotProfile: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: () -> Unit,
    onDeleteBot: () -> Unit,
    onSpeakText: ((String) -> Unit)? = null
) {
    var inputText by remember { mutableStateOf("") }
    var showBotSettings by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }

    // Mood detection based on last AI text
    val lastAiText = messages.lastOrNull { it.role == "assistant" }?.text ?: ""
    val detectedMood = remember(lastAiText) {
        val lower = lastAiText.lowercase()
        when {
            listOf("harika", "süper", "mutlu", "güldü", "🎉", "😊").any { lower.contains(it) } -> "joy"
            listOf("canım", "değerlisin", "sarıl", "güzelim", "sevgi", "💕", "❤️").any { lower.contains(it) } -> "warm"
            listOf("üzgün", "üzülüyorum", "kötü", "yalnız", "😢").any { lower.contains(it) } -> "sad"
            listOf("tehlike", "sinirli", "öfke", "korktu").any { lower.contains(it) } -> "tense"
            listOf("merak", "acaba", "ilginç").any { lower.contains(it) } -> "curious"
            else -> "calm"
        }
    }

    val hue = MoodColors.getMoodHue(detectedMood)
    val moodLabel = MoodColors.getMoodLabel(detectedMood)

    val approxTokens = remember(bot.memoryNotes, bot.storyNotes, bot.pinnedMemory) {
        val chars = bot.memoryNotes.length + bot.storyNotes.length + bot.pinnedMemory.length
        val tok = chars / 4
        if (tok < 1000) "$tok token" else "${"%.1f".format(tok / 1000f)}K"
    }

    val lastAiIndex = messages.indexOfLast { it.role == "assistant" }

    Scaffold(
        containerColor = EmochiBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(EmochiSurface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = EmochiTextPrimary)
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bot.avatarUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = bot.avatarUrl,
                                contentDescription = displayName,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, EmochiBorder, CircleShape)
                            )
                        } else {
                            OrbView(hue = hue, size = 32.dp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayName,
                                color = EmochiTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSending) "yazıyor..." else moodLabel,
                                color = EmochiTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { showBotSettings = true },
                        modifier = Modifier.testTag("bot_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = EmochiTextSecondary)
                    }
                }

                // Sub-header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E2038))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buildString {
                                append(userSettings?.selectedModel?.ifBlank { "gemini-2.5-flash" } ?: "gemini-2.5-flash")
                                val effLen = if (bot.customLength != "default") bot.customLength else userSettings?.responseLength
                                when (effLen) {
                                    "short" -> append(" (Kısa)")
                                    "long" -> append(" (Uzun)")
                                    else -> append(" (Standart RP)")
                                }
                                if ((userSettings?.enableNsfw == true) && bot.isNsfw) {
                                    append(" 🔥")
                                }
                            },
                            color = EmochiTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = EmochiTextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$approxTokens hafıza", color = EmochiTextMuted, fontSize = 11.sp)
                    }
                }
            }
        },
        bottomBar = {
            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmochiSurface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("${bot.userCharName.ifBlank { "Kullanıcı" }} olarak yaz...", color = EmochiTextMuted, fontSize = 13.5.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = customTextFieldColors(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSending) {
                            val text = inputText
                            inputText = ""
                            onSendMessage(text)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) EmochiPrimary else EmochiCard)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Gönder",
                        tint = if (inputText.isNotBlank()) Color(0xFF1A1B2E) else EmochiTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            errorMessage?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3A1414))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(err, color = EmochiError, fontSize = 12.sp)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "user"
                    val isLastAi = !isUser && messages.indexOf(msg) == lastAiIndex

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        if (editingMessageId == msg.id) {
                            // Edit box
                            Card(
                                colors = CardDefaults.cardColors(containerColor = EmochiCard),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    OutlinedTextField(
                                        value = editingText,
                                        onValueChange = { editingText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = customTextFieldColors()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { editingMessageId = null }) {
                                            Text("Vazgeç", color = EmochiTextMuted, fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = {
                                                val text = editingText
                                                editingMessageId = null
                                                onEditMessage(msg.id, text)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary)
                                        ) {
                                            Text("Kaydet", color = Color(0xFF1A1B2E), fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Bubble
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) Brush.linearGradient(listOf(EmochiCoralStart, EmochiCoralEnd))
                                        else Brush.linearGradient(listOf(EmochiCard, EmochiCard))
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) Color.Transparent else EmochiBorder,
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) EmochiUserBubbleText else EmochiTextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }

                            // Message actions row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                            ) {
                                if (isUser) {
                                    Text(
                                        text = "Düzenle",
                                        color = EmochiTextMuted,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.clickable {
                                            editingMessageId = msg.id
                                            editingText = msg.text
                                        }
                                    )
                                } else if (userSettings?.enableTts == true && onSpeakText != null) {
                                    Text(
                                        text = "🔊 Sesli Oku",
                                        color = EmochiPrimary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onSpeakText(msg.text) }
                                    )
                                }
                                Text(
                                    text = "Sil",
                                    color = EmochiTextMuted,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.clickable { onDeleteMessage(msg.id) }
                                )
                                if (isLastAi && !isSending) {
                                    Text(
                                        text = "Yeniden Oluştur",
                                        color = EmochiPrimary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { onRegenerate() }
                                    )
                                }
                            }
                        }
                    }
                }

                if (isSending) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(EmochiCard)
                                    .border(1.dp, EmochiBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                TypingDots()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBotSettings) {
        BotSettingsModal(
            bot = bot,
            keyCharacters = keyCharacters,
            onDismiss = { showBotSettings = false },
            onSave = onSaveBotProfile,
            onResetChat = onResetChat,
            onDeleteBot = onDeleteBot
        )
    }
}
