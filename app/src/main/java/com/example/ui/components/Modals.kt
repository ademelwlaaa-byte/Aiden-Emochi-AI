package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BotEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.theme.EmochiBorder
import com.example.ui.theme.EmochiCard
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiErrorContainer
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiSurface
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import kotlinx.coroutines.launch

data class ModelSpec(
    val key: String,
    val name: String,
    val provider: String,
    val tokenCostRate: String,
    val badgeColor: Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsModal(
    settings: UserSettingsEntity,
    onDismiss: () -> Unit,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Unit
) {
    var geminiApiKey by remember { mutableStateOf(settings.customApiKey) }
    var groqApiKey by remember { mutableStateOf(settings.groqApiKey) }
    var claudeApiKey by remember { mutableStateOf(settings.claudeApiKey) }
    var openaiApiKey by remember { mutableStateOf(settings.openaiApiKey) }
    var backupApiKey by remember { mutableStateOf(settings.backupApiKey) }

    var selectedModel by remember { mutableStateOf(settings.selectedModel) }
    var fallbackModel by remember { mutableStateOf(settings.fallbackModel) }
    var responseLength by remember { mutableStateOf(settings.responseLength) }
    var enableNsfw by remember { mutableStateOf(settings.enableNsfw) }
    var enableOoc by remember { mutableStateOf(settings.enableOoc) }
    var enableFlirty by remember { mutableStateOf(settings.enableFlirty) }
    var enableHardcore by remember { mutableStateOf(settings.enableHardcore) }
    var enableFetish by remember { mutableStateOf(settings.enableFetish) }
    var enableDarkRp by remember { mutableStateOf(settings.enableDarkRp) }
    var enableSweet by remember { mutableStateOf(settings.enableSweet) }
    var enablePrimal by remember { mutableStateOf(settings.enablePrimal) }
    var enableAutoFallback by remember { mutableStateOf(settings.enableAutoFallback) }
    var enableTts by remember { mutableStateOf(settings.enableTts) }
    var ttsSpeed by remember { mutableStateOf(settings.ttsSpeed) }
    var ttsPitch by remember { mutableStateOf(settings.ttsPitch) }
    var selectedVoiceName by remember { mutableStateOf(settings.selectedVoiceName) }
    var appLanguage by remember { mutableStateOf(settings.appLanguage) }

    var showKeys by remember { mutableStateOf(false) }

    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val content = context.contentResolver.openInputStream(it)?.use { stream ->
                        stream.bufferedReader().use { reader -> reader.readText() }
                    } ?: ""
                    if (content.isNotBlank()) {
                        onImportData(content)
                        Toast.makeText(context, "Yedek dosyadan yüklendi!", Toast.LENGTH_SHORT).show()
                        statusMessage = "Dosya başarıyla içe aktarıldı."
                    }
                } catch (e: Exception) {
                    statusMessage = "Yükleme hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isBusy = true
                    val json = if (exportJson.isNotBlank()) exportJson else onExportData()
                    exportJson = json
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Yedek dosyaya kaydedildi!", Toast.LENGTH_SHORT).show()
                    statusMessage = "Yedek dosyaya kaydedildi."
                } catch (e: Exception) {
                    statusMessage = "Kaydetme hatası: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val modelsList = listOf(
        ModelSpec(
            key = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash",
            provider = "Google Gemini",
            tokenCostRate = "🟢 Düşük (~0.5x Token Tüketimi)",
            badgeColor = Color(0xFF4CAF50),
            description = "En gelişmiş, dengeli ve hızlı Gemini modeli. Düşük token harcaması ile yüksek kaliteli rol yapma yanıtları verir."
        ),
        ModelSpec(
            key = "gemini-3.5-flash",
            name = "Gemini 3.5 Flash",
            provider = "Google Gemini",
            tokenCostRate = "⚡ Hızlı & Yeni Nesil",
            badgeColor = Color(0xFF00BCD4),
            description = "Yeni nesil ultra hızlı yanıt süresi. Karmaşık senaryolar ve sohbetler için optimize edilmiştir."
        ),
        ModelSpec(
            key = "gemini-2.5-pro",
            name = "Gemini 2.5 Pro",
            provider = "Google Gemini",
            tokenCostRate = "🔴 Yüksek (~2.5x Token Tüketimi)",
            badgeColor = Color(0xFFE91E63),
            description = "Üst düzey zeka, derin kurgu ve detaylı roman kalitesinde tutarlı karakter anlatımı."
        ),
        ModelSpec(
            key = "llama-3.3-70b-versatile",
            name = "Groq Llama 3.3 70B",
            provider = "Groq API",
            tokenCostRate = "🟡 Orta (~1.0x Token Tüketimi)",
            badgeColor = Color(0xFFFF9800),
            description = "Groq sunucularında ultra hızlı yanıt süresi ve doğal Türkçe rol yapma kabiliyeti."
        ),
        ModelSpec(
            key = "deepseek-r1-distill-llama-70b",
            name = "Groq DeepSeek R1",
            provider = "Groq API",
            tokenCostRate = "🔴 Yüksek (~2.0x Token Tüketimi)",
            badgeColor = Color(0xFF9C27B0),
            description = "Derin mantık ve karmaşık kurgu senaryolarında akıl yürütme odaklı karakter yanıtları."
        ),
        ModelSpec(
            key = "claude-3-5-sonnet-20241022",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            tokenCostRate = "🔴 Çok Yüksek (~3.0x Token Tüketimi)",
            badgeColor = Color(0xFFF44336),
            description = "Edebi anlatım, yüksek duygusal derinlik ve roman kalitesinde akıcı diyaloglar."
        ),
        ModelSpec(
            key = "claude-3-5-haiku-20241022",
            name = "Claude 3.5 Haiku",
            provider = "Anthropic",
            tokenCostRate = "🟡 Orta (~1.2x Token Tüketimi)",
            badgeColor = Color(0xFFFFC107),
            description = "Hızlı ve seri Claude kalitesi. Kısa ve orta boy diyaloglar için ideal."
        ),
        ModelSpec(
            key = "gpt-4o-mini",
            name = "OpenAI GPT-4o Mini",
            provider = "OpenAI",
            tokenCostRate = "🟢 Düşük (~0.8x Token Tüketimi)",
            badgeColor = Color(0xFF4CAF50),
            description = "Ekonomik, tutarlı ve akıcı OpenAI sohbet altyapısı."
        ),
        ModelSpec(
            key = "deepseek-chat",
            name = "DeepSeek V3",
            provider = "DeepSeek",
            tokenCostRate = "🟢 Düşük (~0.6x Token Tüketimi)",
            badgeColor = Color(0xFF009688),
            description = "Bütçe dostu, geniş bağlamlı yüksek akıl yürütme gücü sunan model."
        )
    )

    val modelsMap = modelsList.associate { it.key to "${it.name} [${it.tokenCostRate}]" }

    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var fallbackDropdownExpanded by remember { mutableStateOf(false) }

    val totalTokensUsed = settings.totalPromptTokens + settings.totalCandidateTokens

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = EmochiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Genel AI & Model Ayarları",
                        color = EmochiTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Model Stats Dashboard Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Model & Token Kullanım Paneli", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Girdi Token", color = EmochiTextMuted, fontSize = 11.sp)
                                Text("${settings.totalPromptTokens}", color = EmochiTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Çıktı Token", color = EmochiTextMuted, fontSize = 11.sp)
                                Text("${settings.totalCandidateTokens}", color = EmochiTextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Maliyet Tahmini", color = EmochiTextMuted, fontSize = 11.sp)
                                Text("Ücretsiz / Ücretli Key", color = EmochiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toplam Harcanan: $totalTokensUsed token",
                            color = EmochiTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Response Length Options
                Text(text = "Yanıt Uzunluğu (Token Tüketim Dengesi)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "short" to "⚡ Kısa (Min. Token)",
                        "standard" to "📜 Standart RP",
                        "long" to "📖 Uzun Roman"
                    ).forEach { (key, label) ->
                        val isSelected = responseLength == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EmochiPrimary.copy(alpha = 0.2f) else EmochiCard)
                                .border(
                                    1.dp,
                                    if (isSelected) EmochiPrimary else EmochiBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { responseLength = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) EmochiPrimary else EmochiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // +18 / NSFW Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("+18 Yetişkin / Filtresiz RP Modu", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Gerilim, cinsel ima, karanlık ve ham duyguların sansürsüz dürüstçe işlenmesine izin verir.", color = EmochiTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = enableNsfw,
                        onCheckedChange = { enableNsfw = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1A1B2E),
                            checkedTrackColor = EmochiPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Parantez İçi OOC Yönlendirme Switch
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💬 (... Parantez İçi OOC Yönlendirme)",
                                color = EmochiTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sohbet esnasında parantez içindeki (... Bu böyle olmalı) ifadelerini AI'a hikaye dışı meta yönlendirme komutu olarak iletir.",
                                color = EmochiTextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = enableOoc,
                            onCheckedChange = { enableOoc = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1A1B2E),
                                checkedTrackColor = EmochiPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary AI Model Selection
                Text(text = "Ana AI Modeli", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = modelDropdownExpanded,
                    onExpandedChange = { modelDropdownExpanded = !modelDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    val currentSpec = modelsList.find { it.key == selectedModel } ?: modelsList.first()
                    OutlinedTextField(
                        value = "${currentSpec.name} — ${currentSpec.tokenCostRate}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modelDropdownExpanded,
                        onDismissRequest = { modelDropdownExpanded = false },
                        modifier = Modifier
                            .background(EmochiCard)
                            .padding(vertical = 4.dp)
                    ) {
                        modelsList.forEach { spec ->
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = spec.name,
                                                color = EmochiTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(spec.badgeColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = spec.provider,
                                                    color = spec.badgeColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Token Miktarı: ${spec.tokenCostRate}",
                                            color = EmochiPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = spec.description,
                                            color = EmochiTextMuted,
                                            fontSize = 10.5.sp,
                                            maxLines = 2
                                        )
                                    }
                                },
                                onClick = {
                                    selectedModel = spec.key
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selected Model Details Info Box
                val currentSelectedSpec = modelsList.find { it.key == selectedModel } ?: modelsList.first()
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentSelectedSpec.badgeColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Seçili Model Özellikleri",
                                color = EmochiTextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentSelectedSpec.tokenCostRate,
                                color = currentSelectedSpec.badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSelectedSpec.description,
                            color = EmochiTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fallback Model
                Text(text = "Yedek Model (Hata / Kota Aşımında Geçilir)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = fallbackDropdownExpanded,
                    onExpandedChange = { fallbackDropdownExpanded = !fallbackDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    val currentFallbackSpec = modelsList.find { it.key == fallbackModel } ?: modelsList.first()
                    OutlinedTextField(
                        value = "${currentFallbackSpec.name} — ${currentFallbackSpec.tokenCostRate}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fallbackDropdownExpanded) },
                        colors = customTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fallbackDropdownExpanded,
                        onDismissRequest = { fallbackDropdownExpanded = false },
                        modifier = Modifier.background(EmochiCard)
                    ) {
                        modelsList.forEach { spec ->
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = "${spec.name} (${spec.provider})",
                                            color = EmochiTextPrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tüketim: ${spec.tokenCostRate}",
                                            color = EmochiPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    fallbackModel = spec.key
                                    fallbackDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Language Selection Section
                Text(text = "🌐 Uygulama & Yanıt Dili (App Language)", color = EmochiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Senaryo veya karakter metinleri hangi dilde yazılırsa yazılsın, yapay zeka seçilen dilde otomatik olarak yanıt verir.",
                    color = EmochiTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.FilterChip(
                        selected = appLanguage == "tr",
                        onClick = { appLanguage = "tr" },
                        label = { Text("🇹🇷 Türkçe (Varsayılan)", color = if (appLanguage == "tr") Color(0xFF1A1B2E) else EmochiTextPrimary, fontWeight = FontWeight.Bold) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmochiPrimary,
                            containerColor = EmochiCard
                        )
                    )
                    androidx.compose.material3.FilterChip(
                        selected = appLanguage == "en",
                        onClick = { appLanguage = "en" },
                        label = { Text("🇬🇧 English", color = if (appLanguage == "en") Color(0xFF1A1B2E) else EmochiTextPrimary, fontWeight = FontWeight.Bold) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmochiPrimary,
                            containerColor = EmochiCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // API Key Options Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "API Anahtarları (Gemini, Groq, Claude, OpenAI)", color = EmochiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showKeys = !showKeys }) {
                        Icon(if (showKeys) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = EmochiPrimary)
                    }
                }

                Text(
                    text = "Boş bırakılan servisler için sistem anahtarı veya yedek Gemini anahtarı kullanılır.",
                    color = EmochiTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Gemini Key
                Text("Gemini API Key", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { geminiApiKey = it },
                    placeholder = { Text("Gemini API Key...", fontSize = 12.sp, color = EmochiTextMuted) },
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                // Groq Key
                Text("Groq API Key (Llama 3.3, DeepSeek R1)", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = groqApiKey,
                    onValueChange = { groqApiKey = it },
                    placeholder = { Text("gsk_...", fontSize = 12.sp, color = EmochiTextMuted) },
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                // Claude Key
                Text("Claude (Anthropic) API Key", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = claudeApiKey,
                    onValueChange = { claudeApiKey = it },
                    placeholder = { Text("sk-ant-...", fontSize = 12.sp, color = EmochiTextMuted) },
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                // OpenAI / DeepSeek Key
                Text("OpenAI / DeepSeek Key", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = openaiApiKey,
                    onValueChange = { openaiApiKey = it },
                    placeholder = { Text("sk-...", fontSize = 12.sp, color = EmochiTextMuted) },
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                // Backup Key
                Text("Yedek Gemini Key", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = backupApiKey,
                    onValueChange = { backupApiKey = it },
                    placeholder = { Text("Yedek Gemini Key...", fontSize = 12.sp, color = EmochiTextMuted) },
                    visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 12.dp),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Otomatik Model/API Geçişi", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Modelde hata veya kota aşımı olursa otomatik yedeğe geç.", color = EmochiTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = enableAutoFallback,
                        onCheckedChange = { enableAutoFallback = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1A1B2E),
                            checkedTrackColor = EmochiPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sesli Okuma (Text-To-Speech)", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Yanıtlara sesli okuma butonu ekle ve okuma hızını ayarla.", color = EmochiTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = enableTts,
                        onCheckedChange = { enableTts = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1A1B2E),
                            checkedTrackColor = EmochiPrimary
                        )
                    )
                }

                if (enableTts) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(EmochiCard, RoundedCornerShape(12.dp))
                            .border(1.dp, EmochiBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ Okuma Hızı: ${"%.1f".format(ttsSpeed)}x",
                            color = EmochiTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.8f to "0.8x (Yavaş)", 1.0f to "1.0x (Normal)", 1.25f to "1.25x (Hızlı)", 1.5f to "1.5x (Çok Hızlı)").forEach { (sp, lbl) ->
                                val sel = ttsSpeed == sp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) EmochiPrimary.copy(alpha = 0.25f) else EmochiSurface)
                                        .border(1.dp, if (sel) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable { ttsSpeed = sp }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl, color = if (sel) EmochiPrimary else EmochiTextSecondary, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🎵 Ses Tonu (Pitch): ${"%.1f".format(ttsPitch)}x",
                            color = EmochiTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.8f to "Kalın (0.8x)", 1.0f to "Normal (1.0x)", 1.2f to "İnce (1.2x)").forEach { (p, lbl) ->
                                val sel = ttsPitch == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) EmochiPrimary.copy(alpha = 0.25f) else EmochiSurface)
                                        .border(1.dp, if (sel) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                        .clickable { ttsPitch = p }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl, color = if (sel) EmochiPrimary else EmochiTextSecondary, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // İÇERİK FİLTRELERİ (NSFW) Section (Matching Screenshot 1)
                Text(
                    text = "İÇERİK FİLTRELERİ (NSFW)",
                    color = Color(0xFFE53935),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A141A)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33202E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)) {
                        FilterToggleRow(label = "🔓  18+ (NSFW) Kilidini Aç", checked = enableNsfw) { enableNsfw = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "💖  Çapkınlık (Flirty)", checked = enableFlirty) { enableFlirty = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "🔥  Sert Mod (Hardcore)", checked = enableHardcore) { enableHardcore = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "🎭  Fantezi (Fetish)", checked = enableFetish) { enableFetish = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "👻  Karanlık (Dark RP)", checked = enableDarkRp) { enableDarkRp = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "🍬  Romantik (Sweet)", checked = enableSweet) { enableSweet = it }
                        Divider(color = Color(0xFF2D1E2A))
                        FilterToggleRow(label = "🐾  Vahşi (Primal)", checked = enablePrimal) { enablePrimal = it }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSaveSettings(
                            settings.copy(
                                customApiKey = geminiApiKey.trim(),
                                groqApiKey = groqApiKey.trim(),
                                claudeApiKey = claudeApiKey.trim(),
                                openaiApiKey = openaiApiKey.trim(),
                                backupApiKey = backupApiKey.trim(),
                                selectedModel = selectedModel,
                                fallbackModel = fallbackModel,
                                responseLength = responseLength,
                                enableNsfw = enableNsfw,
                                enableOoc = enableOoc,
                                enableFlirty = enableFlirty,
                                enableHardcore = enableHardcore,
                                enableFetish = enableFetish,
                                enableDarkRp = enableDarkRp,
                                enableSweet = enableSweet,
                                enablePrimal = enablePrimal,
                                enableAutoFallback = enableAutoFallback,
                                enableTts = enableTts,
                                ttsSpeed = ttsSpeed,
                                ttsPitch = ttsPitch,
                                selectedVoiceName = selectedVoiceName,
                                appLanguage = appLanguage
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ayarları Kaydet", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Export / Import section
                Text(text = "Yedekleme & Geri Yükleme (JSON / Dosya)", color = EmochiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Tüm botlarınızı ve sohbet geçmişinizi yedekleyin, dosyaya aktarın veya hazır karakter/yedek dosyalarını geri yükleyin.",
                    color = EmochiTextSecondary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                try {
                                    val json = onExportData()
                                    exportJson = json
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Yedek panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                                    statusMessage = "Yedek kopyalandı."
                                } catch (e: Exception) {
                                    statusMessage = "Hata: ${e.message}"
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📋 Kopyala", color = EmochiPrimary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            saveFileLauncher.launch("emochi_backup_${System.currentTimeMillis() / 1000}.json")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("💾 Kaydet", color = EmochiPrimary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                try {
                                    val json = if (exportJson.isNotBlank()) exportJson else onExportData()
                                    exportJson = json
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Yedeği Paylaş")
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    statusMessage = "Paylaşım hatası: ${e.message}"
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📤 Paylaş", color = EmochiPrimary, fontSize = 11.5.sp)
                    }
                }

                if (exportJson.isNotBlank()) {
                    OutlinedTextField(
                        value = exportJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(top = 8.dp),
                        colors = customTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Geri Yükle & Karakter Yükle", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("application/json") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📂 Dosya Seç (.json)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            clipboardManager.getText()?.let { text ->
                                importJson = text.text
                                Toast.makeText(context, "Metin panodan yapıştırıldı!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📋 Yapıştır", color = EmochiPrimary, fontSize = 11.5.sp)
                    }
                }

                OutlinedTextField(
                    value = importJson,
                    onValueChange = { importJson = it },
                    placeholder = { Text("Yedek JSON veya karakter verisini yapıştırın...", fontSize = 12.sp, color = EmochiTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(top = 6.dp),
                    colors = customTextFieldColors()
                )

                Button(
                    onClick = {
                        if (importJson.isBlank()) return@Button
                        scope.launch {
                            isBusy = true
                            try {
                                onImportData(importJson)
                                Toast.makeText(context, "Geri yükleme tamamlandı!", Toast.LENGTH_SHORT).show()
                                statusMessage = "Tüm botlar ve veriler başarıyla yüklendi."
                                importJson = ""
                            } catch (e: Exception) {
                                statusMessage = "İçe aktarma hatası: ${e.message}"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    enabled = importJson.isNotBlank() && !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("✅ İçe Aktarımı Başlat", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }

                statusMessage?.let { msg ->
                    Text(text = msg, color = EmochiTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun BotSettingsModal(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    onDismiss: () -> Unit,
    onSave: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: () -> Unit,
    onDeleteBot: () -> Unit
) {
    var aiName by remember { mutableStateOf(bot.aiName) }
    var universeName by remember { mutableStateOf(bot.universeName) }
    var personality by remember { mutableStateOf(bot.aiPersonality) }
    var scenario by remember { mutableStateOf(bot.scenario) }
    var userCharName by remember { mutableStateOf(bot.userCharName) }
    var userCharDesc by remember { mutableStateOf(bot.userCharDesc) }
    var writingStyle by remember { mutableStateOf(bot.writingStyle) }
    var intensity by remember { mutableStateOf(bot.intensity) }
    var customLength by remember { mutableStateOf(bot.customLength) }
    var isNsfw by remember { mutableStateOf(bot.isNsfw) }
    var enableOoc by remember { mutableStateOf(bot.enableOoc) }
    var avatarUrl by remember { mutableStateOf(bot.avatarUrl) }
    var chatBgUrl by remember { mutableStateOf(bot.chatBgUrl) }
    var isPublic by remember { mutableStateOf(bot.isPublic) }
    var pinnedMemory by remember { mutableStateOf(bot.pinnedMemory) }
    var storyNotes by remember { mutableStateOf(bot.storyNotes) }
    var memoryNotes by remember { mutableStateOf(bot.memoryNotes) }

    var charList by remember { mutableStateOf(keyCharacters.toMutableList()) }

    var confirmResetChat by remember { mutableStateOf(false) }
    var confirmDeleteBot by remember { mutableStateOf(false) }
    var showNeuralVault by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { avatarUrl = it.toString() }
    }

    val bgPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { chatBgUrl = it.toString() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = EmochiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bot Profil & Hafıza Ayarları", color = EmochiTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar Photo Picker UI
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(29.dp))
                                .background(Color(0xFF252535)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Bot Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(24.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bot Görseli / Fotoğrafı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("Galeriden veya URL ile kapak resmi yükle", color = EmochiTextMuted, fontSize = 11.sp)
                            
                            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Galeri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (avatarUrl.isNotBlank()) {
                                    TextButton(
                                        onClick = { avatarUrl = "" },
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Kaldır", fontSize = 11.sp, color = EmochiError)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chat Background Wallpaper Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF252535)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chatBgUrl.isNotBlank()) {
                                AsyncImage(
                                    model = chatBgUrl,
                                    contentDescription = "Sohbet Arka Planı",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(24.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sohbet Arka Plan Resmi", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("Sohbet içi özel duvar kağıdı belirleyin", color = EmochiTextMuted, fontSize = 11.sp)
                            
                            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { bgPhotoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Galeri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (chatBgUrl.isNotBlank()) {
                                    TextButton(
                                        onClick = { chatBgUrl = "" },
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Kaldır", fontSize = 11.sp, color = EmochiError)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bot Privacy Toggle Card (Public / Private)
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPublic) "🌐 Herkese Açık Bot" else "🔒 Sadece Kendine Özel",
                                color = EmochiTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPublic) "Diğer kullanıcılar 'Keşfet' bölümünde botunuzu bulabilir." else "Bu bot gizlidir, sadece siz görebilirsiniz.",
                                color = EmochiTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isPublic,
                            onCheckedChange = { isPublic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1A1B2E),
                                checkedTrackColor = EmochiPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Neural Vault Button (Screenshot 2 Trigger)
                Button(
                    onClick = { showNeuralVault = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141424), contentColor = Color(0xFF2196F3)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🧠 Neural Vault (Gelişmiş AI Belleği)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }

                if (showNeuralVault) {
                    NeuralVaultModal(
                        bot = bot.copy(
                            pinnedMemory = pinnedMemory,
                            storyNotes = storyNotes,
                            memoryNotes = memoryNotes
                        ),
                        onDismiss = { showNeuralVault = false },
                        onSaveMemory = { updatedBot ->
                            pinnedMemory = updatedBot.pinnedMemory
                            storyNotes = updatedBot.storyNotes
                            memoryNotes = updatedBot.memoryNotes
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (bot.mode == "personal") {
                    Text("Karakter Adı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = aiName,
                        onValueChange = { aiName = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Kişilik Detayları", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 2.dp),
                        colors = customTextFieldColors()
                    )
                } else {
                    Text("Evren / Dünya Adı", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = universeName,
                        onValueChange = { universeName = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        colors = customTextFieldColors(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Senaryo & Bağlam", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = scenario,
                    onValueChange = { scenario = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 2.dp),
                    colors = customTextFieldColors()
                )

                // Per Bot Response Length
                Spacer(modifier = Modifier.height(10.dp))
                Text("Bu Bot İçin Özel Yanıt Uzunluğu", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "default" to "Genel",
                        "short" to "Kısa",
                        "standard" to "Standart",
                        "long" to "Uzun"
                    ).forEach { (key, label) ->
                        val isSelected = customLength == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmochiPrimary.copy(alpha = 0.2f) else EmochiCard)
                                .border(1.dp, if (isSelected) EmochiPrimary else EmochiBorder, RoundedCornerShape(8.dp))
                                .clickable { customLength = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) EmochiPrimary else EmochiTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Per Bot +18 NSFW Switch
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+18 / Filtresiz RP İzni", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isNsfw,
                        onCheckedChange = { isNsfw = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1A1B2E),
                            checkedTrackColor = EmochiPrimary
                        )
                    )
                }

                // Per Bot OOC Parantez İçi Switch Card
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💬 (... Parantez İçi Yönlendirme / OOC)",
                                color = EmochiTextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mesajınızdaki parantez içi (... Bu böyle olmalı) ifadelerini hikaye dışı AI komutu kabul eder.",
                                color = EmochiTextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = enableOoc,
                            onCheckedChange = { enableOoc = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1A1B2E),
                                checkedTrackColor = EmochiPrimary
                            )
                        )
                    }
                }

                if (bot.mode == "universe") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Karakter Kadrosu", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    charList.forEachIndexed { index, char ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = char.name,
                                onValueChange = { newName ->
                                    charList = charList.toMutableList().apply {
                                        this[index] = this[index].copy(name = newName)
                                    }
                                },
                                placeholder = { Text("İsim", fontSize = 11.sp, color = EmochiTextMuted) },
                                modifier = Modifier.weight(1f),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = char.desc,
                                onValueChange = { newDesc ->
                                    charList = charList.toMutableList().apply {
                                        this[index] = this[index].copy(desc = newDesc)
                                    }
                                },
                                placeholder = { Text("Tanım", fontSize = 11.sp, color = EmochiTextMuted) },
                                modifier = Modifier.weight(2f),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                charList = charList.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Sil", tint = EmochiError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            charList = charList.toMutableList().apply { add(KeyCharacter(name = "", desc = "")) }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmochiPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Karakter Ekle", color = EmochiPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Senin Karakterin", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = userCharName,
                    onValueChange = { userCharName = it },
                    placeholder = { Text("İsim", fontSize = 12.sp, color = EmochiTextMuted) },
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    colors = customTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Kalıcı Hafıza (Asla Silinmez)", color = EmochiPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "AI'nın her zaman bilmesini istediğiniz kuralları, ilişkileri veya gerçekleri buraya yazın.",
                    color = EmochiTextSecondary,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = pinnedMemory,
                    onValueChange = { pinnedMemory = it },
                    modifier = Modifier.fillMaxWidth().height(90.dp).padding(top = 4.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Otomatik Hikaye Durumu", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmochiCard)
                        .border(1.dp, EmochiBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = storyNotes.ifBlank { "Henüz kayıtlı hikaye durumu yok." },
                        color = EmochiTextSecondary,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Otomatik Hafıza Özetleri", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmochiCard)
                        .border(1.dp, EmochiBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = memoryNotes.ifBlank { "Henüz kayıtlı hafıza özeti yok." },
                        color = EmochiTextSecondary,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updated = bot.copy(
                            aiName = aiName,
                            universeName = universeName,
                            aiPersonality = personality,
                            scenario = scenario,
                            userCharName = userCharName,
                            userCharDesc = userCharDesc,
                            writingStyle = writingStyle,
                            intensity = intensity,
                            customLength = customLength,
                            isNsfw = isNsfw,
                            enableOoc = enableOoc,
                            avatarUrl = avatarUrl,
                            chatBgUrl = chatBgUrl,
                            isPublic = isPublic,
                            pinnedMemory = pinnedMemory,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updated, charList)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(16.dp))

                if (!confirmResetChat) {
                    Button(
                        onClick = { confirmResetChat = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard, contentColor = EmochiTextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmochiPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sohbet Geçmişini Sıfırla")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Bu sohbetin tüm mesajları silinecek!", color = EmochiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { confirmResetChat = false }) {
                                    Text("Vazgeç", color = EmochiTextSecondary)
                                }
                                Button(
                                    onClick = {
                                        confirmResetChat = false
                                        onResetChat()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiError)
                                ) {
                                    Text("Sıfırla", color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!confirmDeleteBot) {
                    Button(
                        onClick = { confirmDeleteBot = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = EmochiError),
                        modifier = Modifier.fillMaxWidth().border(1.dp, EmochiError.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Botu Tamamen Sil")
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmochiErrorContainer),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Bu bot ve tüm geçmişi kalıcı olarak silinecektir!", color = EmochiError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { confirmDeleteBot = false }) {
                                    Text("Vazgeç", color = EmochiTextSecondary)
                                }
                                Button(
                                    onClick = {
                                        confirmDeleteBot = false
                                        onDeleteBot()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmochiError)
                                ) {
                                    Text("Sil", color = Color.White)
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
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EmochiPrimary,
    unfocusedBorderColor = EmochiBorder,
    focusedContainerColor = EmochiCard,
    unfocusedContainerColor = EmochiCard,
    focusedTextColor = EmochiTextPrimary,
    unfocusedTextColor = EmochiTextPrimary
)

@Composable
fun FilterToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color(0xFFB0B0B0),
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralVaultModal(
    bot: BotEntity,
    onDismiss: () -> Unit,
    onSaveMemory: (BotEntity) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bilgi (Core)") }
    var memoryText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("Tümü") }
    
    var pinnedMemoryText by remember { mutableStateOf(bot.pinnedMemory) }
    
    val memoriesList = remember(pinnedMemoryText) {
        if (pinnedMemoryText.isBlank()) emptyList()
        else {
            pinnedMemoryText.lines().filter { it.isNotBlank() }.map { line ->
                val parts = line.split(":::", limit = 3)
                if (parts.size >= 3) {
                    Triple(parts[0].trim(), parts[1].trim(), parts[2].trim())
                } else if (parts.size == 2) {
                    Triple("BELLEK", "BİLGİ", parts[1].trim())
                } else {
                    Triple("GENEL", "BİLGİ", line.trim())
                }
            }
        }
    }
    
    val filteredMemories = memoriesList.filter { (key, cat, body) ->
        val matchesCategory = categoryFilter == "Tümü" || cat.contains(categoryFilter, ignoreCase = true) || (categoryFilter == "Bilgi (Core)" && (cat.contains("BİLGİ", ignoreCase = true) || cat.contains("CORE", ignoreCase = true)))
        val matchesSearch = searchQuery.isBlank() || key.contains(searchQuery, ignoreCase = true) ||
                cat.contains(searchQuery, ignoreCase = true) ||
                body.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F14)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Matching Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Neural Vault",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                    }
                }

                // Stats Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${memoriesList.size} Kayıt", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("V4.0 Sync", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Long-Term Memory", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Inject Memory Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252535)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YENİ BELLEK ENJEKTE ET",
                                color = Color(0xFF2196F3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = keyword,
                                onValueChange = { keyword = it },
                                placeholder = { Text("Anahtar Kelime", color = Color.Gray, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f),
                                colors = customTextFieldColors(),
                                singleLine = true
                            )
                            
                            // Category Selector Field (Opens Popup Matching Screenshot 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E28))
                                    .border(1.dp, Color(0xFF333344), RoundedCornerShape(8.dp))
                                    .clickable { showCategoryPicker = true }
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Kategori Seç",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Hazır Seçenekler / Şablonlar
                        val quickPresets = listOf(
                            Triple("TAKMA İSİM", "Bilgi (Core)", "Kullanıcıya özel samimi bir unvan veya takma isimle hitap eder."),
                            Triple("GİZLİ DUYGU", "İlişki", "Kullanıcıya karşı derin bir çekim ve gizli bir bağlılık hisseder."),
                            Triple("GEÇMİŞ SIR", "Biyografi", "Geçmişte yaşadığı kırılma noktasını sadece kullanıcıya anlatmıştır."),
                            Triple("ÖZEL MEKAN", "Dünya/Mekan", "Yalnızca ikisinin bildiği gizli ve tenha bir buluşma noktaları vardır."),
                            Triple("YETİŞKİN RP", "İlişki", "Yetişkin etkileşimlerde filtresiz, tutkulu ve tabusuz bir yakınlık benimser."),
                            Triple("SADAKAT BAĞI", "İlişki", "Kullanıcıya koşulsuz sadakat gösterir ve onu her koşulda korur.")
                        )

                        Text(
                            text = "HAZIR SEÇENEKLER & ŞABLONLAR",
                            color = Color(0xFF80CBC4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPresets.forEach { (presetKey, presetCat, presetDesc) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF212130))
                                        .border(1.dp, Color(0xFF38384D), RoundedCornerShape(8.dp))
                                        .clickable {
                                            keyword = presetKey
                                            category = presetCat
                                            memoryText = presetDesc
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF80CBC4), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(presetKey, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = memoryText,
                            onValueChange = { memoryText = it },
                            placeholder = { Text("Karakter neyi hatırlamalı? (Örn: En sevdiği yemek makarnadır.)", color = Color.Gray, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (memoryText.isNotBlank()) {
                                    val keyTag = keyword.ifBlank { "BELLEK" }.uppercase()
                                    val catTag = category.ifBlank { "BİLGİ (CORE)" }.uppercase()
                                    val newEntry = "$keyTag ::: $catTag ::: ${memoryText.trim()}"
                                    val newPinned = if (pinnedMemoryText.isBlank()) newEntry else "$pinnedMemoryText\n$newEntry"
                                    pinnedMemoryText = newPinned
                                    onSaveMemory(bot.copy(pinnedMemory = newPinned))
                                    keyword = ""
                                    memoryText = ""
                                }
                            },
                            enabled = memoryText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Belleği Kaydet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Chips
                val presetCategories = listOf("Tümü", "Bilgi (Core)", "Biyografi", "İlişki", "Dünya/Mekan")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCategories.forEach { cat ->
                        val isSelected = categoryFilter == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2196F3) else Color(0xFF1E1E28))
                                .border(1.dp, if (isSelected) Color(0xFF2196F3) else Color(0xFF2E2E3E), RoundedCornerShape(12.dp))
                                .clickable { categoryFilter = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Hafızada ara...", color = Color.Gray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Saved Memory List Cards
                if (filteredMemories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Henüz enjekte edilmiş bellek yok.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    filteredMemories.forEachIndexed { index, (keyTag, catTag, bodyText) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252535)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = keyTag,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF252535), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = catTag,
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bodyText,
                                    color = Color(0xFFD0D0E0),
                                    fontSize = 12.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val lines = pinnedMemoryText.lines().toMutableList()
                                            val targetLine = "$keyTag ::: $catTag ::: $bodyText"
                                            lines.remove(targetLine)
                                            val updated = lines.joinToString("\n")
                                            pinnedMemoryText = updated
                                            onSaveMemory(bot.copy(pinnedMemory = updated))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryPicker) {
        Dialog(onDismissRequest = { showCategoryPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C34)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Neural Vault",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showCategoryPicker = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val pickerCategories = listOf(
                        "Bilgi (Core)",
                        "Biyografi",
                        "İlişki",
                        "Dünya/Mekan"
                    )

                    pickerCategories.forEachIndexed { index, cat ->
                        val isSelected = category == cat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    category = cat
                                    showCategoryPicker = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF80CBC4),
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                        if (index < pickerCategories.size - 1) {
                            Divider(color = Color(0xFF3F3F4C), thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotQuickProfileSheet(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    onDismiss: () -> Unit,
    onSaveBot: (BotEntity) -> Unit,
    onOpenFullSettings: () -> Unit,
    onResetChat: () -> Unit,
    onExportChat: () -> Unit
) {
    var pinnedMemoryText by remember { mutableStateOf(bot.pinnedMemory) }
    var storyNotesText by remember { mutableStateOf(bot.storyNotes) }
    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = EmochiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (bot.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = bot.avatarUrl,
                                contentDescription = displayName,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, EmochiBorder, CircleShape)
                            )
                        } else {
                            OrbView(hue = 280f, size = 44.dp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = displayName,
                                color = EmochiTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isUniverse) Color(0xFF2A2C4A) else Color(0xFF1E2038))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isUniverse) "Evren Senaryosu" else "Bireysel Karakter",
                                    color = EmochiPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = EmochiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personality / Scenario Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmochiCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmochiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isUniverse) "🌌 Evren Senaryosu" else "🎭 Karakter Kişiliği",
                            color = EmochiPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUniverse) bot.scenario else bot.aiPersonality,
                            color = EmochiTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pinned Memory Quick Editor
                Text("📌 Kalıcı Hafıza (Sabit Notlar)", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "AI'nın unutmasını istemediğiniz detayları buraya yazın.",
                    color = EmochiTextMuted,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = pinnedMemoryText,
                    onValueChange = { pinnedMemoryText = it },
                    placeholder = { Text("Kalıcı notlar ekle...", color = EmochiTextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .padding(top = 4.dp),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSaveBot(bot.copy(pinnedMemory = pinnedMemoryText, storyNotes = storyNotesText))
                        Toast.makeText(context, "Hafıza güncellendi!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiPrimary, contentColor = Color(0xFF1A1B2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hafızayı Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = EmochiBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Buttons
                Text("Hızlı İşlemler", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportChat,
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📋 Kopyala", color = EmochiPrimary, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onResetChat,
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🧹 Sıfırla", color = EmochiError, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenFullSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("⚙️ Ayarlar", color = EmochiTextPrimary, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}
