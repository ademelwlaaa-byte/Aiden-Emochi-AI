package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.ui.components.GlobalSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary

@Composable
fun BotListScreen(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity?,
    onOpenBot: (String) -> Unit,
    onNewBot: () -> Unit,
    onDeleteBot: (String) -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Unit
) {
    var showGlobalSettings by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = EmochiBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNewBot,
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_new_bot_button"),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Bot Oluştur", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Botlarım",
                        color = EmochiTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = EmochiPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buildString {
                                append(userSettings?.selectedModel?.ifBlank { "gemini-2.5-flash" } ?: "gemini-2.5-flash")
                                val lenLabel = when(userSettings?.responseLength) {
                                    "short" -> " ⚡ Kısa"
                                    "long" -> " 📖 Uzun"
                                    else -> " 📜 Standart"
                                }
                                append(" |$lenLabel")
                                if (userSettings?.enableNsfw == true) {
                                    append(" | 🔥 +18")
                                }
                            },
                            color = EmochiTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showGlobalSettings = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmochiCard)
                        .testTag("global_settings_button")
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Ayarlar",
                        tint = EmochiTextSecondary
                    )
                }
            }

            if (botList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OrbView(hue = 275f, size = 56.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz bir bot oluşturmadınız.",
                            color = EmochiTextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Aşağıdaki butona dokunarak ilk AI karakterinizi yazın.",
                            color = EmochiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(botList, key = { it.id }) { bot ->
                        val isUniverse = bot.mode == "universe"
                        val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
                        val hue = MoodColors.getMoodHue(if (isUniverse) "curious" else "calm")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenBot(bot.id) }
                                .testTag("bot_card_${bot.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (bot.avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = bot.avatarUrl,
                                        contentDescription = displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, EmochiBorder, CircleShape)
                                    )
                                } else {
                                    OrbView(hue = hue, size = 42.dp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = displayName,
                                            color = EmochiTextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isUniverse) Color(0xFF2A2C4A) else Color(0xFF1E2038))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isUniverse) "Evren" else "Karakter",
                                                color = EmochiPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Text(
                                        text = bot.scenario.ifBlank { bot.openingMessage },
                                        color = EmochiTextSecondary,
                                        fontSize = 12.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                if (confirmDeleteId == bot.id) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = { confirmDeleteId = null }) {
                                            Text("Vazgeç", color = EmochiTextMuted, fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = {
                                                confirmDeleteId = null
                                                onDeleteBot(bot.id)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmochiError),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Sil", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    IconButton(onClick = { confirmDeleteId = bot.id }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            tint = EmochiTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGlobalSettings && userSettings != null) {
        GlobalSettingsModal(
            settings = userSettings,
            onDismiss = { showGlobalSettings = false },
            onSaveSettings = onSaveSettings,
            onExportData = onExportData,
            onImportData = onImportData
        )
    }
}
