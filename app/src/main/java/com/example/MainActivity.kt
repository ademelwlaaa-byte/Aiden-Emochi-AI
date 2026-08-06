package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.EmochiViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.screens.BotListScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateBotWizardScreen
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: EmochiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EmochiBackground
                ) {
                    EmochiAppMain(viewModel)
                }
            }
        }
    }
}

@Composable
fun EmochiAppMain(viewModel: EmochiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val botList by viewModel.botList.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val activeBot by viewModel.activeBot.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmochiPrimary)
            }
        }

        is UiState.Menu -> {
            BotListScreen(
                botList = botList,
                userSettings = userSettings,
                onOpenBot = { viewModel.openBot(it) },
                onNewBot = { viewModel.startWizard() },
                onDeleteBot = { viewModel.deleteBot(it) },
                onSaveSettings = { viewModel.updateSettings(it) },
                onExportData = { viewModel.exportBackupJson() },
                onImportData = { viewModel.importBackupJson(it) }
            )
        }

        is UiState.SetupWizard -> {
            CreateBotWizardScreen(
                onBack = { viewModel.openMenu() },
                onFinish = { newBot ->
                    viewModel.createBotFromWizard(newBot) {
                        // Opened automatically
                    }
                },
                onGenerateOpening = { draft ->
                    viewModel.generateOpeningForWizard(draft)
                }
            )
        }

        is UiState.Chat -> {
            val currentBot = activeBot
            if (currentBot == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmochiPrimary)
                }
            } else {
                val keyChars = remember(currentBot.keyCharactersJson) {
                    viewModel.repository.parseKeyCharacters(currentBot.keyCharactersJson)
                }

                ChatScreen(
                    bot = currentBot,
                    messages = activeMessages,
                    userSettings = userSettings,
                    isSending = isSending,
                    errorMessage = errorMessage,
                    keyCharacters = keyChars,
                    onBack = { viewModel.openMenu() },
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onRegenerate = { viewModel.regenerateLastResponse() },
                    onEditMessage = { msgId, newText -> viewModel.editMessage(msgId, newText) },
                    onDeleteMessage = { msgId -> viewModel.deleteMessage(msgId) },
                    onSaveBotProfile = { updatedBot, updatedChars ->
                        val json = viewModel.repository.serializeKeyCharacters(updatedChars)
                        viewModel.updateBotProfile(updatedBot.copy(keyCharactersJson = json))
                    },
                    onResetChat = { viewModel.resetChat(currentBot.id) },
                    onDeleteBot = { viewModel.deleteBot(currentBot.id) },
                    onSpeakText = { text -> viewModel.speakText(text) }
                )
            }
        }
    }
}
