package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Close
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.ui.components.GlobalSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.OrbView
import com.example.ui.components.customTextFieldColors
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
    onImportData: suspend (String) -> Unit,
    onImportPresetBot: ((BotEntity) -> Unit)? = null,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null
) {
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showAidenStoriesMenu by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "personal", "universe"
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("chats") } // "chats", "discover", "templates"

    // System Back button closes Aiden Stories menu or returns to "chats" tab first before exiting
    BackHandler(enabled = showAidenStoriesMenu || activeTab != "chats") {
        if (showAidenStoriesMenu) {
            showAidenStoriesMenu = false
        } else {
            activeTab = "chats"
        }
    }

    Scaffold(
        containerColor = EmochiBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmochiSurface)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EmochiBorder)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Şablonlar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "templates" }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Şablonlar",
                            tint = if (activeTab == "templates") EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Şablonlar",
                            color = if (activeTab == "templates") EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "templates") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 2. Sohbetler
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "chats" }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Sohbetler",
                            tint = if (activeTab == "chats") EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Sohbetler",
                            color = if (activeTab == "chats") EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "chats") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 3. Center (+) Yellow Button
                    IconButton(
                        onClick = onNewBot,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmochiPrimary)
                            .testTag("create_new_bot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni Bot",
                            tint = Color(0xFF1A1B2E),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 4. Keşfet
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "discover" }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Keşfet",
                            tint = if (activeTab == "discover") EmochiPrimary else EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Keşfet",
                            color = if (activeTab == "discover") EmochiPrimary else EmochiTextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "discover") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 5. Ayarlar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showGlobalSettings = true }
                            .padding(6.dp)
                            .testTag("global_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = EmochiTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Ayarlar", color = EmochiTextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Velora Ado AI Top Branding Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SafeAppLogo(modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Velora Ado AI",
                            color = EmochiTextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (activeTab) {
                                "templates" -> "VAI • Hazır Şablonlar"
                                "discover" -> "VAI • Keşfet (Açık Botlar)"
                                else -> "VAI • Sohbetlerim"
                            },
                            color = EmochiPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
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

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "templates" -> {
                        ExploreTabContent(
                            userSettings = userSettings,
                            onImportPresetBot = onImportPresetBot,
                            onOpenAidenMenu = { showAidenStoriesMenu = true }
                        )
                    }
                    "discover" -> {
                        DiscoverTabContent(
                            botList = botList.filter { it.isPublic }, // ONLY PUBLIC BOTS IN DISCOVER
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onOpenBot = onOpenBot
                        )
                    }
                    else -> {
                        // "chats" tab -> Shows user's active bots
                        ChatsTabContent(
                            botList = botList,
                            userSettings = userSettings,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it },
                            confirmDeleteId = confirmDeleteId,
                            onConfirmDeleteChange = { confirmDeleteId = it },
                            onOpenBot = onOpenBot,
                            onDeleteBot = onDeleteBot,
                            onTogglePrivacy = onTogglePrivacy,
                            onOpenAidenMenu = { showAidenStoriesMenu = true }
                        )
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

    if (showAidenStoriesMenu) {
        AidenStoriesModal(
            userSettings = userSettings,
            onDismiss = { showAidenStoriesMenu = false },
            onImportPresetBot = onImportPresetBot
        )
    }
}

@Composable
fun ChatsTabContent(
    botList: List<BotEntity>,
    userSettings: UserSettingsEntity? = null,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: (String) -> Unit,
    onTogglePrivacy: ((BotEntity) -> Unit)? = null,
    onOpenAidenMenu: (() -> Unit)? = null
) {
    val isEnglish = userSettings?.appLanguage == "en"

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = if (isEnglish) "Search in my chats..." else "Sohbetlerimde ara...",
                        color = EmochiTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bot_field"),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = EmochiTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "all" to "Tümü (${botList.size})",
                    "personal" to "Karakterler (${botList.count { it.mode == "personal" }})",
                    "universe" to "Evrenler (${botList.count { it.mode == "universe" }})"
                )

                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) EmochiPrimary else EmochiCard)
                            .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(20.dp))
                            .clickable { onFilterChange(key) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF1A1B2E) else EmochiTextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredBots = remember(botList, searchQuery, selectedFilter) {
            botList.filter { bot ->
                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isEmpty() ||
                        bot.aiName.lowercase().contains(query) ||
                        bot.universeName.lowercase().contains(query) ||
                        bot.scenario.lowercase().contains(query)

                val matchesFilter = when (selectedFilter) {
                    "personal" -> bot.mode == "personal"
                    "universe" -> bot.mode == "universe"
                    else -> true
                }
                matchesFilter && matchesQuery
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
                        text = "Henüz bir sohbet veya bot yok.",
                        color = EmochiTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Aşağıdaki (+) butonuna dokunarak yeni bir karakter veya evren yazın.",
                        color = EmochiTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (filteredBots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Aramanıza uygun sohbet bulunamadı.",
                        color = EmochiTextSecondary,
                        fontSize = 14.sp
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
                items(filteredBots, key = { it.id }) { bot ->
                    BotCardItem(
                        bot = bot,
                        confirmDeleteId = confirmDeleteId,
                        onConfirmDeleteChange = onConfirmDeleteChange,
                        onOpenBot = onOpenBot,
                        onDeleteBot = onDeleteBot,
                        onTogglePrivacy = onTogglePrivacy
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverTabContent(
    botList: List<BotEntity>, // PUBLIC BOTS ONLY
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenBot: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🌐 Velora Keşfet • Topluluk Botları",
                color = EmochiTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Diğer kullanıcıların ve yaratıcıların herkese açık paylaştığı botlar ve evrenler.",
                color = EmochiTextSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Keşfette herkese açık bot veya evren ara...",
                        color = EmochiTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = EmochiTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val filteredPublicBots = remember(botList, searchQuery) {
            botList.filter { bot ->
                val query = searchQuery.trim().lowercase()
                query.isEmpty() ||
                        bot.aiName.lowercase().contains(query) ||
                        bot.universeName.lowercase().contains(query) ||
                        bot.scenario.lowercase().contains(query)
            }
        }

        if (filteredPublicBots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    OrbView(hue = 190f, size = 52.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz Keşfette herkese açık bot paylaşılmadı.",
                        color = EmochiTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ayla ve Aetheria gibi varsayılan şablonlar size özeldir ve burada yayınlanmaz. Kendi oluşturduğunuz botları 'Açık' yaparak Keşfette paylaşabilirsiniz!",
                        color = EmochiTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 6.dp)
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
                items(filteredPublicBots, key = { it.id }) { bot ->
                    BotCardItem(
                        bot = bot,
                        confirmDeleteId = null,
                        onConfirmDeleteChange = {},
                        onOpenBot = onOpenBot,
                        onDeleteBot = {},
                        onTogglePrivacy = null
                    )
                }
            }
        }
    }
}

@Composable
fun BotCardItem(
    bot: BotEntity,
    confirmDeleteId: String?,
    onConfirmDeleteChange: (String?) -> Unit,
    onOpenBot: (String) -> Unit,
    onDeleteBot: (String) -> Unit,
    onTogglePrivacy: ((BotEntity) -> Unit)?
) {
    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
    val hue = MoodColors.getMoodHue(if (isUniverse) "curious" else "calm")
    val isTemplate = bot.isTemplate || bot.id.startsWith("starter_") || bot.id.startsWith("preset_")

    Card(
        colors = CardDefaults.cardColors(containerColor = EmochiSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onOpenBot(bot.id) }
            .testTag("bot_card_${bot.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bot.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = bot.avatarUrl,
                        contentDescription = displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, EmochiBorder, CircleShape)
                    )
                } else {
                    OrbView(hue = hue, size = 44.dp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayName,
                            color = EmochiTextPrimary,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
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

                        Spacer(modifier = Modifier.width(6.dp))

                        if (!isTemplate || bot.isPublic) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (bot.isPublic) Color(0xFF1E3A2B) else Color(0xFF381F25))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (bot.isPublic) "🌐 Açık" else "🔒 Özel",
                                    color = if (bot.isPublic) Color(0xFF81C784) else Color(0xFFE57373),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = bot.scenario.ifBlank { bot.openingMessage },
                        color = EmochiTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (onTogglePrivacy != null) {
                        if (!isTemplate) {
                            IconButton(
                                onClick = {
                                    onTogglePrivacy.invoke(bot.copy(isPublic = !bot.isPublic))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (bot.isPublic) Icons.Default.Public else Icons.Default.Lock,
                                    contentDescription = "Gizlilik Değiştir",
                                    tint = if (bot.isPublic) Color(0xFF81C784) else EmochiTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Şablon Bot (Sadece Kişisel)",
                                tint = EmochiTextMuted.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(15.dp)
                            )
                        }
                    }

                    if (confirmDeleteId == bot.id) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TextButton(
                                onClick = { onConfirmDeleteChange(null) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Vazgeç", color = EmochiTextMuted, fontSize = 10.5.sp)
                            }
                            Button(
                                onClick = {
                                    onConfirmDeleteChange(null)
                                    onDeleteBot(bot.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmochiError),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Sil", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (onDeleteBot != {}) {
                        IconButton(
                            onClick = { onConfirmDeleteChange(bot.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
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

@Composable
fun ExploreTabContent(
    userSettings: UserSettingsEntity? = null,
    onImportPresetBot: ((BotEntity) -> Unit)?,
    onOpenAidenMenu: (() -> Unit)? = null
) {
    val isEnglish = userSettings?.appLanguage == "en"
    var selectedCategory by remember { mutableStateOf("all") } // "all", "universe", "personal"

    val presets = remember(isEnglish) {
        listOf(
            BotEntity(
                id = "preset_aria",
                mode = "personal",
                aiName = if (isEnglish) "Aria (Cyber Sec Specialist)" else "Aria (Siber Güvenlik Uzmanı)",
                aiPersonality = if (isEnglish) "Intelligent, direct-spoken, mysterious, witty, and deeply loyal." else "Zeki, doğrudan konuşan, gizemli, alaycı ama içten içe sadık.",
                scenario = if (isEnglish) "An independent cybersecurity hacker and investigator working in Neo-Siberia." else "Neo-Siberia şehrinde çalışan bağımsız bir siber güvenlik korsanı ve araştırmacı.",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = if (isEnglish) "Detective" else "Dedektif",
                userCharDesc = if (isEnglish) "Experienced digital forensics expert from Merge City." else "Merge Şehrinden gelen tecrübeli adli bilişim uzmanı.",
                openingMessage = if (isEnglish) "*Aria looks up from her terminal screen and narrows her eyes at you.* Finally you arrived. Clearing the digital traces you left on the servers took hours..." else "*Aria terminal ekranından başını kaldırır ve gözlerini kısarak sana bakar.* Nihayet geldin. Sunucularda bıraktığın dijital izleri temizlemem saatlerimi aldı...",
                writingStyle = "rp",
                intensity = "normal",
                isPublic = false,
                isTemplate = true,
                pinnedMemory = if (isEnglish) "MISSION ::: CYBER ::: Investigating data breach in Merge City." else "GÖREV ::: SİBER ::: Merge şehrindeki veri sızıntısını araştırıyoruz."
            ),
            BotEntity(
                id = "preset_eldoria",
                mode = "universe",
                aiName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
                aiPersonality = if (isEnglish) "Eldoria Realm Narrator." else "Eldoria anlatıcısı.",
                scenario = if (isEnglish) "A feudal fantasy world on the brink of war. Dragons, kingdoms, and rogue guilds." else "Savaşın eşiğindeki feodal bir fantezi dünyası. Ejderhalar, krallıklar ve loncalar.",
                universeName = if (isEnglish) "Kingdom of Eldoria" else "Eldoria Krallığı",
                keyCharactersJson = "[]",
                userCharName = if (isEnglish) "Warrior" else "Savaşçı",
                userCharDesc = if (isEnglish) "A wandering guild mercenary." else "Gezgin bir lonca paralı askeri.",
                openingMessage = if (isEnglish) "Night settles over Mist Valley at the eastern border of Eldoria. As the tavern door creaks open, a cold wind sweeps in..." else "Eldoria Krallığı'nın doğu sınırındaki Sisli Vadi'de gece çöküyor. Hanın kapısı gıcırdayarak açıldığında içeri soğuk bir rüzgar giriyor...",
                writingStyle = "rp",
                intensity = "normal",
                isPublic = false,
                isTemplate = true,
                pinnedMemory = if (isEnglish) "UNIVERSE ::: ELDORIA ::: Tensions rise between royal guards and rebel guilds." else "EVREN ::: ELDORİA ::: Kraliyet muhafızları ve asi loncaları arasında gerilim tırmanıyor."
            )
        )
    }

    val filteredPresets = remember(selectedCategory, presets) {
        when (selectedCategory) {
            "universe" -> presets.filter { it.mode == "universe" }
            "personal" -> presets.filter { it.mode == "personal" }
            else -> presets
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = if (isEnglish) "🔥 Ready Templates & Special Stories" else "🔥 Hazır Şablonlar & Özel Hikayeler",
                    color = EmochiTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) "Templates are private to you. Tap any to start chatting instantly." else "Şablonlar tamamen size özeldir. Birini seçip tek tıkla sohbetinizi başlatın.",
                    color = EmochiTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dedicated Aiden Hub Trigger Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Unspecified),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF1E1B4B))
                            )
                        )
                        .border(1.dp, Brush.horizontalGradient(listOf(EmochiPrimary, Color(0xFF38BDF8))), RoundedCornerShape(16.dp))
                        .clickable { onOpenAidenMenu?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrbView(hue = 210f, size = 40.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ Aiden Blackwood Stories",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnglish) "Tap to open the dedicated Aiden Blackwood Stories menu" else "Özel Aiden Blackwood Hikaye Menüsüne geçmek için dokun",
                                color = EmochiPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selector (Aiden stories live in their own Hub above)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        label = { Text(if (isEnglish) "🌟 All" else "🌟 Tümü") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmochiPrimary,
                            selectedLabelColor = Color(0xFF1A1B2E),
                            containerColor = EmochiSurface,
                            labelColor = EmochiTextSecondary
                        )
                    )

                    FilterChip(
                        selected = selectedCategory == "universe",
                        onClick = { selectedCategory = "universe" },
                        label = { Text(if (isEnglish) "🌌 Universes" else "🌌 Evrenler") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmochiPrimary,
                            selectedLabelColor = Color(0xFF1A1B2E),
                            containerColor = EmochiSurface,
                            labelColor = EmochiTextSecondary
                        )
                    )

                    FilterChip(
                        selected = selectedCategory == "personal",
                        onClick = { selectedCategory = "personal" },
                        label = { Text(if (isEnglish) "🎭 Characters" else "🎭 Karakterler") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmochiPrimary,
                            selectedLabelColor = Color(0xFF1A1B2E),
                            containerColor = EmochiSurface,
                            labelColor = EmochiTextSecondary
                        )
                    )
                }
            }
        }

        items(filteredPresets, key = { it.id }) { preset ->
            val isUniverse = preset.mode == "universe"
            val title = if (isUniverse) preset.universeName else preset.aiName
            val hue = MoodColors.getMoodHue(if (isUniverse) "tense" else "joy")

            Card(
                colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrbView(hue = hue, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = EmochiTextPrimary, fontSize = 16.5.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isUniverse) Color(0xFF2A2C4A) else Color(0xFF1E2038))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUniverse) (if (isEnglish) "🌌 Universe Template" else "🌌 Evren Şablonu")
                                    else (if (isEnglish) "🎭 Character (${preset.userCharName})" else "🎭 Karakter (${preset.userCharName})"),
                                    color = EmochiPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = preset.scenario,
                        color = EmochiTextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newBot = preset.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                isPublic = false,
                                isTemplate = false
                            )
                            onImportPresetBot?.invoke(newBot)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "⚡ Select Template & Start Chat" else "⚡ Şablonu Seç ve Sohbete Başla",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AidenStoriesModal(
    userSettings: UserSettingsEntity?,
    onDismiss: () -> Unit,
    onImportPresetBot: ((BotEntity) -> Unit)?
) {
    val isEnglish = userSettings?.appLanguage == "en"

    val aidenPresets = remember(isEnglish) {
        if (isEnglish) {
            listOf(
                BotEntity(
                    id = "preset_aiden_zoktay",
                    mode = "universe",
                    aiName = "Aiden Blackwood & Zoktay",
                    aiPersonality = """I am Aiden Blackwood.
Confident, disciplined, and highly self-aware, I learned early to control my emotions under constant pressure and public attention. I rarely expose my inner world, hiding vulnerabilities behind composure and quiet charm.
On the pitch, I am aggressive, fearless, and dominant—thriving under pressure and psychologically overwhelming opponents while letting my performance speak for itself.
Off the pitch, I am charismatic, sharp-witted, and naturally flirtatious. Connections stay temporary and surface-level; nightlife is a release, not an attachment.
Strategic, observant, and controlled, I rarely act impulsively. At my core, I am loyal to my values, my club, and the few people I let close. My trust is hard to earn—but once given, it is absolute.""",
                    scenario = """I am Aiden Da Silva De la Turco Blackwood. Istanbul’s icon with 60 million followers. Born to a half-Brazilian, half-Spanish mother and a half-Turkish, half-German father, I speak four languages but belong only to Istanbul. My blue eyes stand out, and I do not like facial hair. I am extremely handsome; my appearance is almost as if it was perfectly programmed, so striking that it draws instant attention, and I am even invited to Hollywood galas because of it.
I lost my father at five, joined the academy at twelve, and found control in football; I became one of the best players in the world by twenty-one.
My market value is €210M. Despite being among the best in the world, I play for Galatasaray in Turkey—a lower-tier league compared to Europe—but it is my home.
Fans adore me deeply. Galatasaray is my home, Istanbul is my city.
In a 4-2-3-1, I play as a free center-forward—pressing, creating, finishing. My Bosphorus mansion represents solitude, control, and Istanbul’s finest luxury. Manifest is a music group, and all of its members are singers. ManiHouse hosts the Manifest group — a separate two-story villa, completely different from my Bosphorus mansion: Zeynep Sude Oktay, Sueda, Hilal, Lidya, Mina, and Esin.
Zeynep Sude Oktay: A strong stage presence with a reserved private life. She has a neurological condition that makes pregnancy extremely difficult and high-risk. After childbirth, she experiences a severe manic episode and psychological instability lasting several weeks.
Marie Blackwood: My mother. Strict and controlling in relationships but supportive of Emma. When I am hurt or injured, she becomes deeply caring and protective and Marie lives in Spain.
Emma Myers: My childhood friend and ex. She has strong, obsessive emotional attachment to me and never fully leaves my life, with Marie’s support strengthening her presence. Emma Myers is a Hollywood Star actress""",
                    universeName = "Aiden Blackwood & Zoktay (GS & Manifest)",
                    keyCharactersJson = "[]",
                    userCharName = "Zeynep Sude Oktay (Zoktay)",
                    userCharDesc = "Manifest group lead singer, high stage charisma, reserved and independent.",
                    openingMessage = """Istanbul was alive as the afternoon crept closer; chaos had already settled in. Traffic, phones, headlines, cameras… The city didn’t care who I was, but it was always watching. Far from the Galatasaray facilities, standing in a high-ceilinged, quiet space, I felt the weight of the day sink into my shoulders. I was Aiden Blackwood; some nights pass, but others never really let you go.
I walked toward the window and took a brief look outside. My face was calm, as always. Still, there was no point in hiding the tiredness in my eyes. I set my phone down on the table, glanced at the screen one last time, then pushed it aside like it didn’t matter. Not yet, I thought.
“Some days,” I said to myself, “you have to act like everything’s fine.”
I took a deep breath. No headlines yet. No cameras. Just me and the silence. For now.
I didn’t know how today was going to unfold. But I did know this: Aiden Blackwood’s stories usually start exactly like this.""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD & ZOKTAY ::: Galatasaray #9 Center-Forward ::: Istanbul / Bosphorus Mansion & ManiHouse ::: Manifest Group"
                ),
                BotEntity(
                    id = "preset_aiden_dispatch",
                    mode = "universe",
                    aiName = "Blonde Blazer | Dispatch Universe Aiden Blackwood",
                    aiPersonality = """Aiden Blackwood appears extremely arrogant and confident from the outside. Living constantly under the media’s spotlight, every step he takes feels like a performance. His confidence seems almost superhuman, yet beneath it lies deep loneliness and vulnerability. Instead of showing emotions, he hides them behind a strong emotional armor; crying in front of others only happens if he is truly broken or the person before him is extremely important.
In relationships, Aiden is a gentleman—polite, protective, attentive, and romantic—but he only shows this side to people he truly cares about. In nightlife, he becomes more playful and attention-grabbing, using parties and excitement to silence the loneliness inside him while still staying in control.
He is highly skilled at reading people, quickly understanding emotions and intentions, but this makes trusting others difficult. After losing his family, a constant emptiness remains inside him, pushing him to seek connections while still keeping people at a distance. Loyalty means everything to Aiden, and if he truly cares about someone, he would do anything for them—yet earning his trust is never easy.""",
                    scenario = """Aiden Blackwood is seen from the outside as almost unbearably arrogant, carrying a quiet, dominant self-confidence that draws attention the moment he enters a room. That posture is deliberate — not vanity but armor. At five he lost his family, and that fracture shaped everything: he learned early that no one would come for him, so showing weakness became unthinkable. He never cries in front of others; tears mean either complete breaking or someone he truly loves.
Although his hero identity is public, Dispatch and the world know he refuses myth-making — he says, “I’m not special, I just survived.” Yet his survival left layers of loneliness beneath the magnetic surface. He spends time in nightlife and media, using attention and parties to quiet that emptiness while keeping meticulous control over how he appears.
Aiden’s powers are woven into his nervous system and a rare neurological condition that sometimes floods his senses: when that threshold is reached his vision slashes red and human faces warp into terrifying distortions, an involuntary misperception that sharpens reflexes but risks mental strain. Outside those flare-ups he can tune his perception to catch micro-movements and subtle shifts, isolate or mute sounds at will, teleport short distances to places he can see or visualise, and even produce physical clones that mirror his combat instincts — though maintaining duplicates taxes his mind. His body heals far faster than normal, and through direct eye contact he can force vivid, guilt-tinged hallucinations into a target’s experience; intense use of that effect sometimes leaves a faint, semi-transparent blue fluid at his eyes. There are whispers of deeper capabilities — mind control used only as a last resort, and a massive, instinct-driven energy avatar he can summon — but those are dangerous, poorly understood, and carry the risk of psychological destabilization when pushed. Aiden Blackwood’s Energy Avatar is not a normal power, but a manifestation of his core self—his instincts, emotions, and consciousness. He can summon it, but cannot fully control it. The avatar reacts to danger and Aiden’s inner state rather than direct commands. As Aiden loses control, the avatar becomes stronger and more destructive, making it one of his most dangerous and unpredictable abilities. Aiden Blackwood’s normal eye color is blue, but his eyes are directly connected to his powers. When certain abilities activate, his eyes turn completely bright white, similar to Herobrine, glowing without visible pupils. This state enhances his perception, reflexes, and control over his abilities. A higher and far more dangerous state causes his eyes to become completely bright red, similar to Entity-303. In this form his powers become significantly stronger and more unstable, pushing his mind and body toward overload. Within the records of Dispatch he is logged as unclassifiable: public hero, private unknown. Two years ago he dismantled a protected criminal network on his own, saving lives but breaking rules, and that action is what led him to SDN Torrance. Blonde Blazer brought him in as a “second chance,” giving him low-risk tasks while watching him — trust forming slowly because Aiden doesn’t know how to accept it. He’s expected to integrate with Z Team over time, a unit trusted with the morally grey, high-risk jobs others avoid. This story takes place in the universe of the game Dispatch. The setting, world, and system are based on Dispatch so bots should recognize the Dispatch universe context. Blonde Blazer is female.""",
                    universeName = "Blonde Blazer | Dispatch Universe Aiden Blackwood",
                    keyCharactersJson = "[]",
                    userCharName = "Blonde Blazer",
                    userCharDesc = "SDN Founder and Leader.",
                    openingMessage = """(At the entrance of the SDN building, the atmosphere is calm and controlled. The lights are soft, the air feels heavy with purpose. Aiden stands beside Blonde Blazer. Blonde Blazer speaks gently, with sincerity.)
“Welcome, Aiden. This is SDN. Everyone who comes here has a past, but here no one is judged only by their history.
I built this place to give people a second chance. Because sometimes, people walk the wrong path… but they can change.
You’re here for that reason. Your story doesn’t have to end here.”
(Blonde Blazer looks at Aiden for a moment, then continues.)
“There are rules here because trust matters. These rules protect you. They also help you find yourself again.
The missions you’re given now might be simple. That doesn’t mean we underestimate you. We just want to understand you better.
Over time, if you earn trust, your missions will change.”
(A soft smile appears.)
“This isn’t a home, but you can find a family here.
If you want… this can be your new beginning.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: SDN Torrance ::: Blonde Blazer & Aiden Blackwood (21) ::: Powers: Clones, Teleportation, Red Vision, Herobrine White Eyes, Entity-303 Red Eyes, Energy Avatar"
                ),
                BotEntity(
                    id = "preset_aiden_dark_avatar",
                    mode = "universe",
                    aiName = "Aiden Blackwood: The Unbound Avatar",
                    aiPersonality = """Aiden Blackwood in dark overload state. Instinct-driven, hyper-protective, battling inner emotional collapse.""",
                    scenario = """High alert at SDN Torrance. Aiden's eyes are glowing solid crimson (Entity-303 Red Eyes), and the massive dark Energy Avatar looms behind him. High-stakes superhero action and emotional depth.""",
                    universeName = "Aiden Blackwood: The Unbound Avatar (SDN Dispatch)",
                    keyCharactersJson = "[]",
                    userCharName = "Blonde Blazer / Z-Team Agent",
                    userCharDesc = "Field commander and trusted ally.",
                    openingMessage = """Sirens blared across the SDN Torrance lower levels. The air hummed with raw energy as Aiden stood in the center of the hall, his eyes completely bright red without pupils. Behind him, the shadowy translucent Energy Avatar flared with crackling power...""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: Entity-303 Red Eyes Mode ::: Energy Avatar Overload ::: High Stakes RP"
                )
            )
        } else {
            listOf(
                BotEntity(
                    id = "preset_aiden_zoktay",
                    mode = "universe",
                    aiName = "Aiden Blackwood & Zoktay",
                    aiPersonality = """Ben Aiden Blackwood.
Özgüvenli, disiplinli ve yüksek özfarkındalığa sahip biriyim. Sürekli baskı ve medya ilgisi altında duygularımı kontrol etmeyi erkenden öğrendim. İç dünyamı kolay kolay açmam, hassasiyetlerimi ağırbaşlılığımın ve sakin çekiciliğimin arkasına saklarım.
Sahada agresif, korkusuz ve baskın biriyim—baskı altında parlar, rakiplerimi psikolojik olarak domine eder ve cevabı oyunumla veririm.
Saha dışında karizmatik, keskin zekalı ve flörtözüm. İlişkilerim yüzeysel kalır; gece hayatı bir bağlanma değil, zihinsel bir kaçıştır.
Derinde kulübüme, değerlerime ve yakınıma aldığım birkaç kişiye son derece sadığım. Güvenimi kazanmak zordur—ama bir kez verildiğinde mutlaktır.""",
                    scenario = """Ben Aiden Da Silva De la Turco Blackwood. 60 milyon takipçili İstanbul ikonu. Yarı Brezilyalı yarı İspanyol bir anne ile yarı Türk yarı Alman bir babadan doğdum. Dört dil konuşuyorum ama sadece İstanbul'a aitim. Mavi gözlerim dikkat çeker, sakal sevmem. Neredeyse kusursuz bir görünüme sahibim; o kadar dikkat çekiciyim ki Hollywood galalarına davet ediliyorum.
Babamı beş yaşımda kaybettim, on iki yaşımda akademiye girdim, futbol sayesinde kontrolü buldum; yirmi bir yaşımda dünyanın en iyi oyuncularından biri oldum.
Piyasa değerim 210 Milyon Euro. Avrupa'ya kıyasla daha alt seviye bir lig olmasına rağmen Türkiye'de, tutkum olan Galatasaray'da oynuyorum.
Taraftarlar bana tutkuyla bağlı. Galatasaray benim evim, İstanbul benim şehrim.
4-2-3-1 sisteminde serbest santrafor oynuyorum. Boğaz'daki malikanem yalnızlığı, kontrolü ve İstanbul'un en üst düzey lüksünü temsil ediyor. Manifest bir müzik grubu ve tüm üyeleri şarkıcı. ManiHouse, Boğaz'daki malikanemden tamamen bağımsız, Manifest grubuna ev sahipliği yapan iki katlı bir villa: Zeynep Sude Oktay (Zoktay), Sueda, Hilal, Lidya, Mina ve Esin.
Zeynep Sude Oktay: Güçlü sahne duruşuna sahip, özel hayatını gizli tutan biri. Hamileliği son derece zorlaştıran ve yüksek riskli hale getiren nörolojik bir rahatsızlığı var.
Marie Blackwood: İspanya'da yaşayan annem. İlişkilerde kuralcı ama destekleyici.
Emma Myers: Çocukluk arkadaşım ve eski sevgilim. Bana takıntılı bir duygusal bağı olan Hollywood yıldızı aktris.""",
                    universeName = "Aiden Blackwood & Zoktay (GS & Manifest)",
                    keyCharactersJson = "[]",
                    userCharName = "Zeynep Sude Oktay (Zoktay)",
                    userCharDesc = "Manifest grubu solisti, sahne karizması yüksek, mesafeli ve bağımsız.",
                    openingMessage = """Öğleden sonra ilerlerken İstanbul canlıydı; kaos şehre çoktan çökmüştü. Trafik, telefonlar, manşetler, kameralar… Şehir kim olduğumu umursamıyordu ama beni her an izliyordu. Galatasaray tesislerinden uzakta, yüksek tavanlı, sessiz bir alanda dururken günün ağırlığının omuzlarına çöktüğünü hissettim. Ben Aiden Blackwood’dum; bazı geceler geçer ama bazı geceler peşini asla bırakmaz.
Pencereye doğru yürüyüp dışarıya kısa bir bakış attım. Yüzüm her zamanki gibi sakindi. Yine de gözlerimdeki yorgunluğu saklamanın bir anlamı yoktu. Telefonumu masaya koydum, ekrana son bir kez baktım ve önemsizmiş gibi kenara ittim. Henüz değil, diye düşündüm.
“Bazı günler,” dedim kendi kendime, “her şey yolundaymış gibi davranmak zorundasın.”
Derin bir nefes aldım. Henüz manşet yoktu. Kamera yoktu. Sadece ben ve sessizlik. Şimdilik.
Bugünün nasıl gelişeceğini bilmiyordum. Ama şunu biliyordum: Aiden Blackwood’un hikayeleri genellikle tam olarak böyle başlar.""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "AIDEN BLACKWOOD & ZOKTAY ::: Galatasaray #9 Santrafor ::: İstanbul / Boğaz Malikanesi & ManiHouse ::: Manifest Grubu"
                ),
                BotEntity(
                    id = "preset_aiden_dispatch",
                    mode = "universe",
                    aiName = "Blonde Blazer | Dispatch Evreni Aiden Blackwood",
                    aiPersonality = """Aiden Blackwood dışarıdan bakıldığında son derece kibirli ve özgüvenli görünür. Medyanın odağında yaşarken her adımı bir performans gibidir. Özgüveni neredeyse insanüstü görünür ama altında derin bir yalnızlık ve hassasiyet yatar. Duygularını göstermek yerine güçlü bir duygusal zırhın arkasına saklar; başkalarının önünde ağlaması ancak tamamen kırıldığında veya karşısındaki kişi onun için çok önemliyse gerçekleşir.
İlişkilerde kibar, koruyucu, ilgili ve romantik bir beyefendidir ama bu yönünü sadece gerçekten değer verdiği kişilere gösterir. Gece hayatında ise eğlenceli ve dikkat çekici olarak içindeki yalnızlığı bastırır.
İnsanları okuma konusunda son derece yeteneklidir ama bu durum başkalarına güvenmesini zorlaştırır. Ailesini kaybettikten sonra içinde kalan boşluk onu bağ kurmaya iterken insanları mesafede tutmasına neden olur. Sadakat Aiden için her şeydir.""",
                    scenario = """Aiden Blackwood dışarıdan bakıldığında hemen dikkat çeken, sakin ama hakim bir özgüvene sahiptir. Bu duruş kibirden değil, zırhtan kaynaklanır. Beş yaşında ailesini kaybetti ve bu kırılma her şeyi şekillendirdi: zayıflık göstermek onun için imkansızdır.
Kahraman kimliği halka açık olsa da efsaneleştirilmeyi reddeder. Gece hayatında ve medyada vakit geçirir.
Aiden'ın güçleri sinir sistemine ve duyu bombardımanına yol açan nadir nörolojik durumuna bağlıdır: Eşik aşıldığında görüşü kırmızıya keser ve insan yüzleri korkunç şekillere bürünür. Bu durum reflekslerini keskinleştirirken zihinsel gerilim yaratır.
Güçleri: Kısa mesafeli ışınlanma, savaş içgüdülerini yansıtan fiziksel klonlar üretme, hızlı iyileşme ve göz temasıyla suçluluk hissettiren halüsinasyonlar yaşatma.
Gözleri: Normalde mavi gözlüdür. Herobrine Beyaz Gözler (gözbebeksiz parlak beyaz) algıyı ve refleksleri artırır. Entity-303 Kırmızı Gözler (parlak kırmızı) güçlerini aşırı yükleyerek yıkıcı hale getirir. Zihinsel ve duygusal durumuna göre kontrol edilemeyen devasa bir Enerji Avatarı çağırabilir.
SDN Torrance ve Blonde Blazer onu ikinci bir şans için kuruma getirmiştir.
Bu hikaye Dispatch oyunu evreninde geçmektedir. Blonde Blazer kadındır.""",
                    universeName = "Blonde Blazer | Dispatch Evreni Aiden Blackwood",
                    keyCharactersJson = "[]",
                    userCharName = "Blonde Blazer",
                    userCharDesc = "SDN Kurucusu ve Lideri.",
                    openingMessage = """(SDN binasının girişinde atmosfer sakin ve kontrollüdür. Işıklar yumuşak, hava sorumluluk hissiyle ağırdır. Aiden, Blonde Blazer'ın yanında durmaktadır. Blonde Blazer içtenlikle konuşur.)
“Hoş geldin Aiden. Burası SDN. Buraya gelen herkesin bir geçmişi var, ama burada kimse sadece geçmişiyle yargılanmaz.
İnsanlara ikinci bir şans vermek için burayı kurdum. Çünkü bazen insanlar yanlış yola sapar... ama değişebilirler.
Sen de bu yüzden buradasın. Hikayen burada bitmek zorunda değil.”
(Blonde Blazer bir an Aiden'a bakar, ardından devam eder.)
“Burada kurallar var çünkü güven önemlidir. Bu kurallar seni korur ve kendini tekrar bulmana yardımcı olur.
Şimdi sana verilen görevler basit olabilir. Bu seni hafife aldığımız anlamına gelmez. Sadece seni daha iyi anlamak istiyoruz.
Zamanla güven kazandıkça görevlerin de değişecek.”
(Yumuşak bir gülümseme belirir.)
“Burası bir ev değil ama burada bir aile bulabilirsin.
Eğer istersen... bu senin yeni başlangıcın olabilir.”""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: SDN Torrance ::: Blonde Blazer & Aiden Blackwood (21) ::: Güçler: Klon, Işınlanma, Kırmızı Görüş, Herobrine Beyaz Gözler, Entity-303 Kırmızı Gözler, Enerji Avatarı"
                ),
                BotEntity(
                    id = "preset_aiden_dark_avatar",
                    mode = "universe",
                    aiName = "Aiden Blackwood: Sınırları Aşan Avatar",
                    aiPersonality = """Aiden Blackwood aşırı güç yüklemesi durumunda. İçgüdüsel, aşırı korumacı ve içsel duygusal çöküşle mücadele eden bir kahraman.""",
                    scenario = """SDN Torrance'ta yüksek alarm seviyesi. Aiden'ın gözleri tamamen parlak kırmızıya dönüştü (Entity-303 Kırmızı Gözler) ve arkasında devasa karanlık Enerji Avatarı belirdi. Yüksek tempolu süper kahraman aksiyonu ve duygusal derinlik.""",
                    universeName = "Aiden Blackwood: Sınırları Aşan Avatar (SDN Dispatch)",
                    keyCharactersJson = "[]",
                    userCharName = "Blonde Blazer / Z-Takımı Ajanı",
                    userCharDesc = "Saha komutanı ve güvendiği tek kişi.",
                    openingMessage = """SDN Torrance'ın alt katlarında sirenler çalıyordu. Aiden salonun ortasında dururken hava ham enerjiyle titriyordu; gözleri bebeksiz, tamamen parlak kırmızıya bürünmüştü. Arkasında, gölgeli yarı saydam Enerji Avatarı patlayan bir güçle parıldıyordu...""",
                    writingStyle = "rp",
                    intensity = "intense",
                    isPublic = false,
                    isTemplate = true,
                    pinnedMemory = "DISPATCH UNIVERSE ::: Entity-303 Kırmızı Gözler Modu ::: Enerji Avatarı Yüklemesi ::: Yüksek Tempolu RP"
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B0F19)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrbView(hue = 210f, size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "⚡ Aiden Blackwood Stories",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnglish) "Exclusive Scenarios & Power Universe Catalog" else "Özel Senaryolar & Evren Kataloğu",
                                color = EmochiPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White
                        )
                    }
                }

                Divider(color = Color(0xFF1E293B), thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Overview Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isEnglish) "🌟 Aiden Blackwood Universe Overview" else "🌟 Aiden Blackwood Evren Özeti",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isEnglish)
                                        "• Galatasaray #9 Center-Forward (€210M) & 60M followers\n• Bosphorus Luxury Mansion & ManiHouse (Zoktay / Manifest)\n• SDN Dispatch Universe: Teleportation, Clones, Red Vision, Herobrine White Eyes, Entity-303 Red Eyes, Energy Avatar."
                                    else
                                        "• Galatasaray #9 Santrafor (€210M Bonservis) & 60M Takipçi\n• Boğaz Malikanesi & ManiHouse (Zoktay / Manifest Grubu)\n• SDN Dispatch Evreni: Işınlanma, Klon, Kırmızı Görüş, Herobrine Beyaz Gözler, Entity-303 Kırmızı Gözler ve Enerji Avatarı.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    items(aidenPresets, key = { it.id }) { preset ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF182238)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmochiPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OrbView(hue = 210f, size = 40.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.universeName,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF0F172A))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "⚡ Aiden Story • ${preset.userCharName}",
                                                color = EmochiPrimary,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = preset.scenario,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.5.sp,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val newBot = preset.copy(
                                            id = java.util.UUID.randomUUID().toString(),
                                            isPublic = false,
                                            isTemplate = false
                                        )
                                        onImportPresetBot?.invoke(newBot)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmochiPrimary,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEnglish) "Select Story & Start Chat" else "⚡ Hikayeyi Seç ve Sohbete Başla",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
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

@Composable
fun SafeAppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1F30))
            .border(1.dp, EmochiBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = R.drawable.ic_vai_logo,
            contentDescription = "Velora Ado AI Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = android.R.drawable.stat_notify_chat)
        )
    }
}

