package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

sealed class UiState {
    object Loading : UiState()
    object Menu : UiState()
    object SetupWizard : UiState()
    data class Chat(val botId: String) : UiState()
}

class EmochiViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = EmochiRepository(db)

    private val _uiState = MutableStateFlow<UiState>(UiState.Menu)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val botList: StateFlow<List<BotEntity>> = repository.allBots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _activeBotId = MutableStateFlow<String?>(null)
    val activeBotId: StateFlow<String?> = _activeBotId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBot: StateFlow<BotEntity?> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getBotFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _activeBotId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        viewModelScope.launch {
            repository.getOrCreateSettings()
            repository.initStarterBotsIfEmpty()
        }
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale("tr", "TR"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speakText(text: String) {
        val cleanText = text.replace(Regex("\\*.*?\\*"), "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "EmochiTTS")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    fun openMenu() {
        _errorMessage.value = null
        _activeBotId.value = null
        _uiState.value = UiState.Menu
    }

    fun startWizard() {
        _errorMessage.value = null
        _uiState.value = UiState.SetupWizard
    }

    fun openBot(botId: String) {
        _errorMessage.value = null
        _activeBotId.value = botId
        _uiState.value = UiState.Chat(botId)

        viewModelScope.launch {
            val existingMsgs = repository.getMessageListForBot(botId)
            if (existingMsgs.isEmpty()) {
                val bot = repository.getBot(botId)
                if (bot != null) {
                    val openingText = bot.openingMessage.ifBlank {
                        if (bot.mode == "universe") {
                            "*Sahne başlar. Çevre sakin ve atmosferik bir hava bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
                        } else {
                            "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
                        }
                    }
                    val openingMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = bot.id,
                        role = "assistant",
                        text = openingText,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveMessage(openingMsg)
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createBotFromWizard(bot: BotEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                repository.saveBot(bot)

                val openingText = bot.openingMessage.ifBlank {
                    if (bot.mode == "universe") {
                        "*Sahne başlar. Çevre sakin ve atmosferik bir hava bürünmüştür.*\n\n\"Hikayemize nereden başlamak istersin?\""
                    } else {
                        "Merhaba! Seni seve seve dinliyorum, ne hakkında konuşmak istersin?"
                    }
                }

                val openingMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot.id,
                    role = "assistant",
                    text = openingText,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(openingMsg)

                _activeBotId.value = bot.id
                _uiState.value = UiState.Chat(bot.id)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "Bot oluşturulamadı: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        val botId = _activeBotId.value ?: return
        val currentBot = activeBot.value ?: return
        if (text.isBlank() || _isSending.value) return

        viewModelScope.launch {
            try {
                _isSending.value = true
                _errorMessage.value = null

                val userMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "user",
                    text = text.trim(),
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(userMsg)

                val currentMsgs = repository.getMessageListForBot(botId)

                val replyText = repository.generateAiReply(currentBot, currentMsgs)

                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(aiMsg)

                repository.saveBot(currentBot.copy(updatedAt = System.currentTimeMillis()))

                val finalMsgs = currentMsgs + aiMsg
                if (finalMsgs.size % 6 == 0) {
                    repository.updateMemorySummaries(currentBot, finalMsgs)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Yanıt oluşturulamadı."
            } finally {
                _isSending.value = false
            }
        }
    }

    fun regenerateLastResponse() {
        val botId = _activeBotId.value ?: return
        val currentBot = activeBot.value ?: return
        if (_isSending.value) return

        viewModelScope.launch {
            try {
                _isSending.value = true
                _errorMessage.value = null

                val msgs = repository.getMessageListForBot(botId)
                if (msgs.isEmpty()) return@launch

                val lastAssistantIndex = msgs.indexOfLast { it.role == "assistant" }
                if (lastAssistantIndex == -1) return@launch

                val targetMsg = msgs[lastAssistantIndex]
                repository.deleteMessage(targetMsg.id)

                val remainingMsgs = msgs.subList(0, lastAssistantIndex)

                val replyText = repository.generateAiReply(currentBot, remainingMsgs)

                val newAiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    botId = botId,
                    role = "assistant",
                    text = replyText,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(newAiMsg)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Yeniden oluşturulamadı."
            } finally {
                _isSending.value = false
            }
        }
    }

    fun editMessage(msgId: String, newText: String) {
        val botId = _activeBotId.value ?: return
        val currentBot = activeBot.value ?: return

        viewModelScope.launch {
            try {
                val msgs = repository.getMessageListForBot(botId)
                val idx = msgs.indexOfFirst { it.id == msgId }
                if (idx == -1) return@launch

                val isUserMsg = msgs[idx].role == "user"
                for (i in (idx + 1) until msgs.size) {
                    repository.deleteMessage(msgs[i].id)
                }

                val editedMsg = msgs[idx].copy(text = newText, timestamp = System.currentTimeMillis())
                repository.saveMessage(editedMsg)

                if (isUserMsg) {
                    _isSending.value = true
                    _errorMessage.value = null

                    val truncatedList = msgs.subList(0, idx) + editedMsg
                    val replyText = repository.generateAiReply(currentBot, truncatedList)

                    val newAiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = botId,
                        role = "assistant",
                        text = replyText,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.saveMessage(newAiMsg)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Mesaj düzenlenemedi."
            } finally {
                _isSending.value = false
            }
        }
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch {
            repository.deleteMessage(msgId)
        }
    }

    fun resetChat(botId: String) {
        viewModelScope.launch {
            val currentBot = repository.getBot(botId) ?: return@launch
            repository.resetMessagesForBot(botId)

            val opening = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = botId,
                role = "assistant",
                text = currentBot.openingMessage,
                timestamp = System.currentTimeMillis()
            )
            repository.saveMessage(opening)
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            repository.deleteBot(botId)
            if (_activeBotId.value == botId) {
                openMenu()
            }
        }
    }

    fun updateBotProfile(updatedBot: BotEntity) {
        viewModelScope.launch {
            repository.saveBot(updatedBot)
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateUserSettings(settings)
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDataSnapshot()
    }

    suspend fun importBackupJson(jsonStr: String) {
        repository.importDataSnapshot(jsonStr)
    }

    suspend fun generateOpeningForWizard(botDraft: BotEntity): String {
        return repository.generateOpeningMessage(botDraft)
    }
}
