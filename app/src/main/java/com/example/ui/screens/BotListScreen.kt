package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "personal", "universe"
    var privacyFilter by remember { mutableStateOf("all") } // "all", "public", "private"
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("chats") } // "chats", "discover", "templates"

    // System Back button returns to "chats" tab first before exiting
    BackHandler(enabled = activeTab != "chats") {
        activeTab = "chats"
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
                    // 1. Şablonlar (Hazır Karakterler)
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

                    // 2. Sohbetler (Aktif Chatler)
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

                    // 4. Keşfet (Arama ve Karakter Havuzu)
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
                    AsyncImage(
                        model = R.drawable.ic_vai_logo,
                        contentDescription = "Velora Ado AI Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, EmochiBorder, RoundedCornerShape(10.dp))
                    )
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
                                "templates" -> "VAI • Şablonlar & Senaryolar"
                                "discover" -> "VAI • Keşfet & Karakter Havuzu"
                                else -> "VAI • Aktif Sohbetler"
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

            if (activeTab == "templates") {
                ExploreTabContent(onImportPresetBot = onImportPresetBot)
            } else {
                // Discover or Chats Tab View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (activeTab == "discover") "Keşfette bot, karakter veya evren ara..." else "Sohbetlerimde ara...",
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
                        if (activeTab == "discover") {
                            val privFilters = listOf(
                                "all" to "Tümü (${botList.size})",
                                "public" to "🌐 Herkese Açık (${botList.count { it.isPublic }})",
                                "private" to "🔒 Kendine Özel (${botList.count { !it.isPublic }})"
                            )

                            privFilters.forEach { (key, label) ->
                                val isSelected = privacyFilter == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) EmochiPrimary else EmochiCard)
                                        .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(20.dp))
                                        .clickable { privacyFilter = key }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF1A1B2E) else EmochiTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        } else {
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
                                        .clickable { selectedFilter = key }
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
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredBots = remember(botList, searchQuery, selectedFilter, privacyFilter, activeTab) {
                    botList.filter { bot ->
                        val query = searchQuery.trim().lowercase()
                        val matchesQuery = query.isEmpty() ||
                                bot.aiName.lowercase().contains(query) ||
                                bot.universeName.lowercase().contains(query) ||
                                bot.scenario.lowercase().contains(query)

                        if (activeTab == "discover") {
                            val matchesPrivacy = when (privacyFilter) {
                                "public" -> bot.isPublic
                                "private" -> !bot.isPublic
                                else -> true
                            }
                            matchesPrivacy && matchesQuery
                        } else {
                            val matchesFilter = when (selectedFilter) {
                                "personal" -> bot.mode == "personal"
                                "universe" -> bot.mode == "universe"
                                else -> true
                            }
                            matchesFilter && matchesQuery
                        }
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
                                text = "Aşağıdaki (+) butonuna dokunarak ilk Velora AI karakterinizi yazın.",
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
                                text = "Aramanıza uygun bot bulunamadı.",
                                color = EmochiTextSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Farklı bir arama terimi veya filtre deneyin.",
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
                        items(filteredBots, key = { it.id }) { bot ->
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

                                            // Privacy Badge
                                            Spacer(modifier = Modifier.width(6.dp))
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

                                        Text(
                                            text = bot.scenario.ifBlank { bot.openingMessage },
                                            color = EmochiTextSecondary,
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Quick Privacy Toggle / Delete Button
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                onTogglePrivacy?.invoke(bot.copy(isPublic = !bot.isPublic))
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

@Composable
fun ExploreTabContent(
    onImportPresetBot: ((BotEntity) -> Unit)?
) {
    val presets = remember {
        listOf(
            BotEntity(
                id = java.util.UUID.randomUUID().toString(),
                mode = "personal",
                aiName = "Aria",
                aiPersonality = "Zeki, doğrudan konuşan, gizemli, alaycı ama içten içe sadık.",
                scenario = "Neo-Siberia şehrinde çalışan bağımsız bir siber güvenlik korsanı ve araştırmacı.",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Dedektif",
                userCharDesc = "Merge Şehrinden gelen tecrübeli adli bilişim uzmanı.",
                openingMessage = "*Aria terminal ekranından başını kaldırır ve gözlerini kısarak sana bakar.* Nihayet geldin. Sunucularda bıraktığın dijital izleri temizlemem saatlerimi aldı...",
                writingStyle = "rp",
                intensity = "normal",
                pinnedMemory = "GÖREV ::: SİBER ::: Merge şehrindeki veri sızıntısını araştırıyoruz."
            ),
            BotEntity(
                id = java.util.UUID.randomUUID().toString(),
                mode = "personal",
                aiName = "Lord Valerius",
                aiPersonality = "Ağırbaşlı, gizemli, yüksek özgüvenli, kütüphanelerinde kaybolmayı seven antika meraklısı.",
                scenario = "Unutulmuş Ruhlar Kulesi'nin karanlık ve kudretli büyücüsü.",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Çırak",
                userCharDesc = "Karanlık büyü sanatlarını öğrenmek isteyen yetenekli genç büyücü.",
                openingMessage = "*Karanlık kütüphanenin yüksek pencerelerinden süzülen mor ışık altında kadim kitabı kapatır.* Adımların tereddütlü. Karşıma çıkmaya hazır olduğuna emin misin, çırak?",
                writingStyle = "rp",
                intensity = "intense",
                pinnedMemory = "BÜYÜ ::: AKADEMİ ::: Kadim rünlerin kökenini araştırıyoruz."
            ),
            BotEntity(
                id = java.util.UUID.randomUUID().toString(),
                mode = "personal",
                aiName = "Sora",
                aiPersonality = "Enerjik, neşeli, empati gücü yüksek, eski günleri yâd etmeyi seven sıcakkanlı.",
                scenario = "Yıllar sonra karşılaştığın neşeli ve cana yakın çocukluk arkadaşın.",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Dostum",
                userCharDesc = "Uzun zamandır memleketine dönmemiş eski arkadaşı.",
                openingMessage = "*Sora gözleri parlayarak sana doğru koşar.* İnanmıyorum! Gerçekten sensin! Kaç yıl oldu, hiç değişmemişsin!",
                writingStyle = "chat",
                intensity = "normal",
                pinnedMemory = "ARKADAŞLIK ::: HATIRA ::: Küçükken mahalledeki eski ahşap evde oynardık."
            ),
            BotEntity(
                id = java.util.UUID.randomUUID().toString(),
                mode = "universe",
                aiName = "Eldoria Krallığı",
                aiPersonality = "Eldoria anlatıcısı.",
                scenario = "Savaşın eşiğindeki feodal bir fantezi dünyası. Ejderhalar, krallıklar ve loncalar.",
                universeName = "Eldoria Krallığı",
                keyCharactersJson = "[]",
                userCharName = "Savaşçı",
                userCharDesc = "Gezgin bir lonca paralı askeri.",
                openingMessage = "Eldoria Krallığı'nın doğu sınırındaki Sisli Vadi'de gece çöküyor. Hanın kapısı gıcırdayarak açıldığında içeri soğuk bir rüzgar giriyor...",
                writingStyle = "rp",
                intensity = "normal",
                pinnedMemory = "EVREN ::: ELDORİA ::: Kraliyet muhafızları ve asi loncaları arasında gerilim tırmanıyor."
            ),
            BotEntity(
                id = java.util.UUID.randomUUID().toString(),
                mode = "universe",
                aiName = "Sığınak 13",
                aiPersonality = "Sığınak 13 anlatıcısı.",
                scenario = "Nükleer serpinti ve mutant yaratıkların kol gezdiği çorak topraklar.",
                universeName = "Sığınak 13: Kıyamet Sonrası",
                keyCharactersJson = "[]",
                userCharName = "Hayatta Kalan",
                userCharDesc = "Kaynak arayan yalnız bir izci.",
                openingMessage = "Geiger sayacının cızırtısı kulağında yankılanıyor. Yıkılmış köprünün altında paslı bir sığınak kapısı duruyor...",
                writingStyle = "rp",
                intensity = "intense",
                pinnedMemory = "EVREN ::: KIYAMET ::: Temiz su ve tıbbi malzeme stoku tükenmek üzere."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🔥 Popüler Hazır Şablonlar & Evrenler", color = EmochiTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            Text("Hazır karakter ve evren şablonlarından birini seçerek anında sohbet simülasyonuna başlayın.", color = EmochiTextSecondary, fontSize = 12.sp)
        }

        items(presets) { preset ->
            val isUniverse = preset.mode == "universe"
            val title = if (isUniverse) preset.universeName else preset.aiName
            val hue = MoodColors.getMoodHue(if (isUniverse) "tense" else "joy")

            Card(
                colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrbView(hue = hue, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = EmochiTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isUniverse) Color(0xFF2A2C4A) else Color(0xFF1E2038))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUniverse) "🌌 Evren Şablonu" else "🎭 Karakter (${preset.userCharName})",
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
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newBot = preset.copy(id = java.util.UUID.randomUUID().toString())
                            onImportPresetBot?.invoke(newBot)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("⚡ Şablonu Kullan ve Sohbete Başla", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
