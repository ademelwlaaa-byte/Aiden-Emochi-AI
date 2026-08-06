package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
    var enableAutoFallback by remember { mutableStateOf(settings.enableAutoFallback) }
    var enableTts by remember { mutableStateOf(settings.enableTts) }

    var showKeys by remember { mutableStateOf(false) }

    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val modelsMap = mapOf(
        "gemini-2.0-flash" to "⚡ Gemini 2.0 Flash (En Hızlı & Yetenekli)",
        "gemini-1.5-pro" to "🧠 Gemini 1.5 Pro (Yüksek Akıl Yürütme)",
        "gemini-1.5-flash" to "⚡ Gemini 1.5 Flash (Standart Hızlı)",
        "gemini-3.5-flash" to "⚡ Gemini 3.5 Flash (Deneysel Flash)",
        "gemini-3.1-pro-preview" to "🧠 Gemini 3.1 Pro (Deneysel Pro)",
        "llama-3.3-70b-versatile" to "🚀 Groq Llama 3.3 70B (Süper Hızlı Groq)",
        "deepseek-r1-distill-llama-70b" to "🧩 Groq DeepSeek R1 (Akıllı Kurgu)",
        "claude-3-5-sonnet-20241022" to "📖 Claude 3.5 Sonnet (Efsane Roman / Yüksek Token)",
        "claude-3-5-haiku-20241022" to "⚡ Claude 3.5 Haiku (Hızlı & Akıcı RP)",
        "gpt-4o-mini" to "💡 OpenAI GPT-4o Mini (Hızlı & Ekonomik)",
        "deepseek-chat" to "🧩 DeepSeek V3 (Bütçe Dostu Zeka)"
    )

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
                    OutlinedTextField(
                        value = modelsMap[selectedModel] ?: selectedModel,
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
                        modifier = Modifier.background(EmochiCard)
                    ) {
                        modelsMap.forEach { (modelKey, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = EmochiTextPrimary, fontSize = 12.sp) },
                                onClick = {
                                    selectedModel = modelKey
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fallback Model
                Text(text = "Yedek Model (Hata / Kota Aşımında Geçilir)", color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = fallbackDropdownExpanded,
                    onExpandedChange = { fallbackDropdownExpanded = !fallbackDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = modelsMap[fallbackModel] ?: fallbackModel,
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
                        modelsMap.forEach { (modelKey, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = EmochiTextPrimary, fontSize = 12.sp) },
                                onClick = {
                                    fallbackModel = modelKey
                                    fallbackDropdownExpanded = false
                                }
                            )
                        }
                    }
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
                        Text("Yanıtlara sesli okuma butonu ekle.", color = EmochiTextSecondary, fontSize = 11.sp)
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

                Spacer(modifier = Modifier.height(18.dp))

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
                                enableAutoFallback = enableAutoFallback,
                                enableTts = enableTts
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
                Text(text = "Yedekleme & Geri Yükleme (JSON)", color = EmochiTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Tüm botlarınızı ve sohbet geçmişinizi JSON olarak dışa aktarabilir veya yedekten geri yükleyebilirsiniz.",
                    color = EmochiTextSecondary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isBusy = true
                                try {
                                    val json = onExportData()
                                    exportJson = json
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Yedek panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                                    statusMessage = "Yedek verisi panoya kopyalandı."
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
                        Text("Dışa Aktar", color = EmochiPrimary, fontSize = 12.5.sp)
                    }
                }

                if (exportJson.isNotBlank()) {
                    OutlinedTextField(
                        value = exportJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(top = 8.dp),
                        colors = customTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Geri Yükle", color = EmochiTextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = importJson,
                    onValueChange = { importJson = it },
                    placeholder = { Text("Yedek JSON metnini buraya yapıştırın...", fontSize = 12.sp, color = EmochiTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 4.dp),
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
                                statusMessage = "Tüm botlar ve sohbetler başarıyla aktarıldı."
                                importJson = ""
                            } catch (e: Exception) {
                                statusMessage = "İçe aktarma hatası: ${e.message}"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmochiCard),
                    enabled = importJson.isNotBlank() && !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("İçe Aktar", color = EmochiPrimary, fontSize = 12.5.sp)
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
    var pinnedMemory by remember { mutableStateOf(bot.pinnedMemory) }
    var storyNotes by remember { mutableStateOf(bot.storyNotes) }
    var memoryNotes by remember { mutableStateOf(bot.memoryNotes) }

    var charList by remember { mutableStateOf(keyCharacters.toMutableList()) }

    var confirmResetChat by remember { mutableStateOf(false) }
    var confirmDeleteBot by remember { mutableStateOf(false) }

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
