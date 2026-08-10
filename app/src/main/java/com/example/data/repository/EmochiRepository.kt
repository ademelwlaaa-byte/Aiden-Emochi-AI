package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class KeyCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val desc: String = ""
)

data class BackupSnapshot(
    val version: Int = 1,
    val bots: List<BotEntity>,
    val messages: List<MessageEntity>,
    val settings: UserSettingsEntity
)

class EmochiRepository(private val db: AppDatabase) {
    private val botDao = db.botDao()
    private val messageDao = db.messageDao()
    private val settingsDao = db.userSettingsDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val keyCharListAdapter = moshi.adapter<List<KeyCharacter>>(
        Types.newParameterizedType(List::class.java, KeyCharacter::class.java)
    )
    private val backupAdapter = moshi.adapter(BackupSnapshot::class.java)

    private fun getBuildConfigKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
    }

    val allBots: Flow<List<BotEntity>> = botDao.getAllBots()
    val userSettingsFlow: Flow<UserSettingsEntity?> = settingsDao.getUserSettingsFlow()

    fun getBotFlow(id: String): Flow<BotEntity?> = botDao.getBotByIdFlow(id)
    fun getMessagesFlow(botId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForBot(botId)

    suspend fun getBot(id: String): BotEntity? = botDao.getBotById(id)

    suspend fun saveBot(bot: BotEntity) = botDao.insertOrUpdate(bot)

    suspend fun deleteBot(id: String) = botDao.deleteBotById(id)

    suspend fun saveMessage(msg: MessageEntity) = messageDao.insertMessage(msg)

    suspend fun deleteMessage(id: String) = messageDao.deleteMessageById(id)

    suspend fun resetMessagesForBot(botId: String) {
        messageDao.deleteMessagesForBot(botId)
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) = settingsDao.insertOrUpdate(settings)

    suspend fun getMessageListForBot(botId: String): List<MessageEntity> {
        return messageDao.getMessagesForBotList(botId)
    }

    suspend fun initStarterBotsIfEmpty() = withContext(Dispatchers.IO) {
        val existingBots = botDao.getAllBotsList()
        if (existingBots.isEmpty()) {
            val now = System.currentTimeMillis()
            val starterBot1 = BotEntity(
                id = "starter_ayla",
                mode = "personal",
                aiName = "Ayla",
                aiPersonality = "Sıcak, neşeli, empati yeteneği yüksek, konuşkan ve korumacı bir yakın arkadaş. Zeki espriler yapmayı ve senin gününün nasıl geçtiğini dinlemeyi sever.",
                scenario = "Lise/üniversiteden beri en yakın dostun. Akşam saatlerinde kahveni yudumlarken sana mesaj atıyor.",
                universeName = "Kişisel Sohbet",
                keyCharactersJson = "[]",
                userCharName = "Sencer",
                userCharDesc = "Ayla'nın en güvendiği yakın dostu.",
                openingMessage = "*Telefonun ekranı aydınlanır, Ayla'dan yeni bir mesaj gelmiştir.*\n\n\"Selam! Sonunda bugünkü yoğun koşturmacayı bitirip koltuğuma çekilebildim. Sen nasılsın bakalım? Günün nasıl geçti, bana anlatmak istediğin bir şeyler var mı?\"",
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = false,
                isTemplate = true,
                pinnedMemory = "Ayla her zaman içten ve samimidir. Sencer'e çok değer verir.",
                updatedAt = now
            )

            val starterBot2 = BotEntity(
                id = "starter_aetheria",
                mode = "universe",
                aiName = "Aetheria Yönetmeni",
                aiPersonality = "Atmosferik, gizemli ve sürükleyici bir fantezi-siberpunk dünyası anlatıcısı.",
                scenario = "Neon ışıklarıyla aydınlatılmış Aetheria şehrinin yüksek kuleleri ve gölgeli alt sokaklarında tehlikeli bir lonca anlaşması yapılmak üzeredir.",
                universeName = "Aetheria: Neon & Büyü",
                keyCharactersJson = """[{"id":"c1","name":"Valeria","desc":"Büyü teknolojisi uzmanı lonca lideri"},{"id":"c2","name":"Kael","desc":"Sessiz ve tehlikeli paralı asker"}]""",
                userCharName = "Rider",
                userCharDesc = "Loncanın en yetenekli bilgi tüccarı.",
                openingMessage = "*Neon tabelaların yağmurlu asfalt üzerinde mor ve mavi yansımalar oluşturduğu sokağın köşesinde duruyorsun. Yağmurluğunun yakasını kaldırdın. Valeria, arkasındaki iki muhafızla birlikte gölgelerin arasından süzülerek sana doğru yaklaştı.*\n\n\"Tam zamanında geldin Rider,\" dedi Valeria, sesindeki elektronik bozulmayı gizlemeye çalışarak. \"İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?\"",
                writingStyle = "rp",
                intensity = "normal",
                customLength = "default",
                isNsfw = true,
                isPublic = false,
                isTemplate = true,
                pinnedMemory = "Aetheria evreninde yüksek teknoloji ile kadim sihir iç içedir.",
                updatedAt = now - 1000
            )

            botDao.insertOrUpdate(starterBot1)
            botDao.insertOrUpdate(starterBot2)

            val msg1 = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = starterBot1.id,
                role = "assistant",
                text = starterBot1.openingMessage,
                timestamp = now
            )
            val msg2 = MessageEntity(
                id = UUID.randomUUID().toString(),
                botId = starterBot2.id,
                role = "assistant",
                text = starterBot2.openingMessage,
                timestamp = now - 1000
            )
            messageDao.insertMessage(msg1)
            messageDao.insertMessage(msg2)
        } else {
            for (bot in existingBots) {
                if (bot.id.startsWith("starter_") || bot.id.startsWith("preset_") || bot.isTemplate) {
                    if (bot.isPublic || !bot.isTemplate) {
                        botDao.insertOrUpdate(bot.copy(isPublic = false, isTemplate = true))
                    }
                }
            }
        }
    }

    suspend fun getOrCreateSettings(): UserSettingsEntity {
        var settings = settingsDao.getUserSettings()
        if (settings == null) {
            settings = UserSettingsEntity()
            settingsDao.insertOrUpdate(settings)
        }
        return settings
    }

    private suspend fun recordTokenUsage(botId: String? = null, promptTokens: Long, candidateTokens: Long) {
        val current = getOrCreateSettings()
        val updated = current.copy(
            totalPromptTokens = current.totalPromptTokens + promptTokens,
            totalCandidateTokens = current.totalCandidateTokens + candidateTokens
        )
        settingsDao.insertOrUpdate(updated)

        if (!botId.isNullOrBlank()) {
            val bot = botDao.getBotById(botId)
            if (bot != null) {
                val updatedBot = bot.copy(
                    totalPromptTokens = bot.totalPromptTokens + promptTokens,
                    totalCandidateTokens = bot.totalCandidateTokens + candidateTokens
                )
                botDao.insertOrUpdate(updatedBot)
            }
        }
    }

    fun parseKeyCharacters(json: String): List<KeyCharacter> {
        return try {
            if (json.isBlank()) emptyList() else keyCharListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeKeyCharacters(chars: List<KeyCharacter>): String {
        return try {
            keyCharListAdapter.toJson(chars)
        } catch (e: Exception) {
            "[]"
        }
    }

    // --- Prompt & Memory Logic ---

    fun buildSystemPrompt(bot: BotEntity, settings: UserSettingsEntity, includeStyleGuide: Boolean = true): String {
        val pinnedBlock = if (bot.pinnedMemory.isNotBlank()) {
            "\n\n## Kalıcı hafıza (kullanıcının elle yazdığı, ASLA silinmeyen/özetlenmeyen notlar — bunlara mutlaka uy)\n${bot.pinnedMemory}"
        } else ""

        val memoryBlock = if (bot.memoryNotes.isNotBlank()) {
            "\n\n## Uzun vadeli hafıza (geçmiş sohbetlerden özet)\n${bot.memoryNotes}"
        } else ""

        val storyBlock = if (bot.storyNotes.isNotBlank()) {
            "\n\n## Süregelen hikaye durumu\n${bot.storyNotes}"
        } else ""

        // +18 NSFW Policy & Active Filter Directives
        val isNsfwAllowed = settings.enableNsfw && bot.isNsfw
        val filterDirectives = mutableListOf<String>()
        val nsfwPolicy = if (isNsfwAllowed) {
            filterDirectives.add("18+ Yetişkin / Filtresiz RP serbesttir.")
            if (settings.enableFlirty) filterDirectives.add("ÇAPKINKLIK (FLIRTY): Karakter diyaloglarında çapkın, flörtöz ve baştan çıkarıcı bir eda benimser.")
            if (settings.enableHardcore) filterDirectives.add("SERT MOD (HARDCORE): Duygular, fiziksel tepkiler ve tutku sert, filtresiz ve yüksek yoğunlukta ifade edilir.")
            if (settings.enableFetish) filterDirectives.add("FANTEZİ (FETISH): Özel fantezi unsurlarına ve arzulara açık, tabusuz kurgu desteklenir.")
            if (settings.enableDarkRp) filterDirectives.add("KARANLIK (DARK RP): Gerilim, psikolojik derinlik, tehlike ve karanlık atmosfer belirgin hissedilir.")
            if (settings.enableSweet) filterDirectives.add("ROMANTİK (SWEET): Şefkat, tatlı yakınlık, duygusal romantizm ön plandadır.")
            if (settings.enablePrimal) filterDirectives.add("VAHŞİ (PRIMAL): Ham içgüdüler, id ve baskılanmamış arzularla karakter harekete geçer.")
            
            "## İÇERİK POLİTİKASI: +18 / FİLTRESİZ BİÇİM (+18 YETİŞKİN MODU)\n- Bu hikaye +18 / filtresiz rol yapma modundadır. Gerilim, şiddet, karanlık temalar, romantizm, cinsel ima/yakınlık ve ham duygular sansürlenmeden, dürüstçe yazılabilir. Yumuşatma yapma." +
                    if (filterDirectives.isNotEmpty()) "\n\n### AKTİF MODLAR VE İÇERİK FİLTRELERİ:\n- " + filterDirectives.joinToString("\n- ") else ""
        } else {
            "## İÇERİK POLİTİKASI: GÜVENLİ / DENGELİ MOD\n- Aile dostu, genel izleyici kitlesine uygun, grafik şiddet veya cinsel açıklık içermeyen dengeli bir anlatım kullan."
        }

        // Response Length Guidance
        val effLength = if (bot.customLength != "default") bot.customLength else settings.responseLength
        val lengthInstruction = when (effLength) {
            "short" -> "\n## YANIT UZUNLUĞU: KISA & HIZLI (AZ TOKEN)\n- Yanıtı 1-3 kısa paragraf/sahne tut. Hızlı tempolu, öz, doğrudan olaya odaklanan mesaj yaz."
            "long" -> "\n## YANIT UZUNLUĞU: UZUN & DESTANSI (YÜKSEK DETAY)\n- Yanıtı 5-8 ayrıntılı ve uzun paragraf yaz. Derin iç monologlar, zengin mekan tasvirleri, karakter mimikleri ve ayrıntılı aksiyon adımları kullan."
            else -> "\n## YANIT UZUNLUĞU: STANDART ROMAN RP (DENGELİ DETAY)\n- Yanıtı 3-5 zengin paragraf tut. Aşağıdaki örnek yapıya uygun olarak atmosfer, diyaloglar ve hareketleri dengeli harmanla."
        }

        val userCharLabel = bot.userCharName.ifBlank { "kullanıcı" }
        val rpRules = "\n- $userCharLabel adına ASLA konuşma/hareket ettirme. Sadece anlatıcı/canlandırdığın karakterleri işlet, sırayı kullanıcıya bırak.\n- Tekrar etme, sahneyi ileri taşı.\n- Duyusal detaylarla ortamı canlı tut."

        val sampleStructure = """
## HİKAYE VE ANLATIM DÜZENİ (ÖRNEK SAHNE YAPISI)
Metni edebi bir roman sahnesi gibi yapılandır. Aksiyonu, karakter beden dilini, çevresel detayları ve diyalogları tırnak içinde harmanla.

Örnek Yapı:
Peter bir moloz parçasının üzerinde oturuyordu, nefes alabilmesi için maskesi burnunun üstüne kadar çekilmişti. Bir blenderden geçmiş gibi görünüyordu. Elbisesi parçalanmıştı ve çene çizgisi boyunca koyu bir morluk oluşmuştu.
  "İyiyim," dedi ama sesi biraz çatladı. Titrek bir nefes aldı ve beton levhaya yaslandı.   "En azından Bruce'tan daha iyi."
Sokakta Hulk'un durduğu kratere baktı, sonra Aiden'a döndü.
  "Bu... çok yoğundu. Senin için bile," Peter ağrıyan omzunu ovuşturarak itiraf etti.   "Gerçekten onu susturdun. Hiç böyle bir şey görmemiştim."
Durdu, ifadesi ciddileşti.
  "Jean hâlâ orada. Durumu iyi değil. Bruce'a ne yaptıysa ondan da bir şeyler alıp götürmüş." Baxter Binasının girişini işaret etti.
        """.trimIndent()

        val styleGuide = if (includeStyleGuide) {
            if (bot.writingStyle == "rp") {
                "\n\n## Yazım tarzı — RP / Roman Anlatımı\nÜçüncü tekil şahıs anlatım kullan. Sahneyi, ortamı ve karakter tepkilerini anlatıp diyaloglara bağla.\n\n$sampleStructure\n\nKurallar:$rpRules"
            } else {
                "\n\n## Yazım tarzı — Sade sohbet\nDoğal, sıcak, birinci ağızdan mesajlaşma tarzında yaz."
            }
        } else ""

        val langDirective = if (settings.appLanguage == "en") {
            """

            ## MANDATORY LANGUAGE OVERRIDE (USER APP LANGUAGE = ENGLISH):
            - The active user application language setting is ENGLISH ("en").
            - REGARDLESS of the original language of the character backstory, universe scenario, initial message, memory notes, or user input (even if written in Turkish or another language):
              1. ALL YOUR RESPONSES MUST BE 100% IN FLUENT, NATURAL, HIGH-QUALITY ENGLISH.
              2. Translate all scenario actions, dialogue, character thoughts, and narrator descriptions seamlessly into English in real-time.
              3. Never produce Turkish text in your output when the app language is set to English.
            """.trimIndent()
        } else {
            """

            ## MUTLAK DİL VE ZORUNLU ÇEVİRİ KURALI (UYGULAMA DİLİ = TÜRKÇE):
            - Kullanıcının aktif uygulama dili TÜRKÇE ("tr")'dir.
            - Karakter tanımı, senaryo detayları, açılış mesajı, hafıza notları veya kullanıcı girdisi İngilizce ya da başka bir dilde yazılmış olsa dahi:
              1. TÜM YANITLARINI %100 MÜKEMMEL, DOĞAL VE AKICI TÜRKÇE OLARAK ÜRET.
              2. İngilizce yazılmış tüm senaryo eylemlerini, diyalogları, iç düşünceleri ve anlatımı anında Türkçe'ye çevirerek sun.
              3. Dil Türkçe seçiliyken yanıtlarında asla İngilizce veya yabancı dilde metin üretme.
            """.trimIndent()
        }

        val oocDirective = if (bot.enableOoc && settings.enableOoc) {
            "\n\n## PARANTEZ İÇİ YÖNLENDİRME / OOC (OUT OF CHARACTER) YÖNERGESİ:\n- Kullanıcının mesajında parantez içinde \"(...)\" veya \"[...]\" yazdığı ifadeler hikaye dışı (OOC / Meta Yönlendirme) talimatlar ve AI yönlendirmeleridir.\n- Örnek: \"(Ayla bu sırada kapıyı kilitlesin)\" veya \"(Sahneyi akşam vaktine taşıyalım)\" veya \"(Daha soğuk tepki ver)\".\n- Parantez içindeki bu talimatları SİSTEM VE YÖNERGE TALİMATI olarak algıla. Karakter diyalogunda \"neden parantez açtın\" veya \"tamam şöyle yapıyorum\" deme! Doğrudan talimatı sahneye, karaktere ve aksiyona dürüstçe uygula."
        } else ""

        if (bot.mode == "universe") {
            val castList = parseKeyCharacters(bot.keyCharactersJson)
            val castBlock = if (castList.isNotEmpty()) {
                "\n\n## Karakter kadrosu (sahnede isimli/tekrar eden karakter olarak SADECE bunları ve $userCharLabel'i kullan; yeni bir \"ana karakter\" icat ETME)\n" +
                        castList.joinToString("\n") { "- ${it.name}: ${it.desc.ifBlank { "(tanım verilmedi)" }}" }
            } else {
                "\n\nKURAL: Sahnede gerekirse yan karakterler oluşturabilirsin ama abartma — az sayıda kullan."
            }

            return "Sen \"${bot.universeName}\" adlı kurgusal evrende geçen bir hikayenin anlatıcısı ve yönetmenisin. Kullanıcı tek bir karakteri ($userCharLabel) canlandırıyor; sen sahneyi, ortamı ve gerektiğinde diğer karakterleri yönetiyorsun.$pinnedBlock\n\n## Evren ve olay örgüsü\n${bot.scenario}$castBlock\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$storyBlock$memoryBlock$oocDirective$langDirective\n\n## Genel kurallar\n- Evrenin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
        }

        val aiName = bot.aiName.ifBlank { "Karakter" }
        return "Sen \"$aiName\" adında bir karaktersin ve kullanıcıyla kişisel/samimi bir senaryoda etkileşim kuruyorsun.$pinnedBlock\n\n## Kişilik\n${bot.aiPersonality}\n\n## Bağlam\nİlişki / bağlam: ${bot.scenario}\n\n## Kullanıcının canlandırdığı karakter\n$userCharLabel${if (bot.userCharDesc.isNotBlank()) " — ${bot.userCharDesc}" else ""}\n\n$nsfwPolicy$lengthInstruction$styleGuide$storyBlock$memoryBlock$oocDirective$langDirective\n\n## Genel kurallar\n- Karakterinin ve senaryonun dışına çıkma, tutarlılığını koru.\n- Sahneyi kullanıcı yerine bitirme.\n- Önceki sahnelerde kurduğun detayları hatırlıyormuş gibi kullan."
    }

    // --- API Service Execution Engine ---

    private fun sanitizeModelName(model: String): String {
        val clean = model.trim().lowercase()
        return when {
            clean == "gemini-1.5-flash" || clean == "gemini-2.0-flash" -> "gemini-2.5-flash"
            clean == "gemini-1.5-pro" || clean == "gemini-2.0-pro" || clean == "gemini-2.0-flash-thinking" -> "gemini-2.5-pro"
            clean.isEmpty() -> "gemini-2.5-flash"
            else -> model.trim()
        }
    }

    // --- Context Window & Token Management Engine ---

    fun getModelContextLimit(model: String): Int {
        val m = model.trim().lowercase()
        return when {
            m.contains("claude-3-5-sonnet") || m.contains("claude-3-5-haiku") || m.contains("claude-3-opus") -> 200_000
            m.contains("gpt-4o") || m.contains("o1") || m.contains("o3-mini") -> 128_000
            m.contains("gemini") -> 1_000_000
            m.contains("deepseek") -> 64_000
            m.contains("llama-3.3") || m.contains("llama-3.1") -> 128_000
            m.contains("llama") || m.contains("groq") || m.contains("mixtral") -> 32_768
            else -> 32_000
        }
    }

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        return (text.length / 3.5).toInt().coerceAtLeast(1)
    }

    fun estimateTotalTokens(systemPrompt: String, messages: List<MessageEntity>): Int {
        var count = estimateTokenCount(systemPrompt)
        for (msg in messages) {
            count += estimateTokenCount(msg.text) + 4
        }
        return count
    }

    private suspend fun prepareContextAndSummarizeIfNeeded(
        bot: BotEntity,
        messages: List<MessageEntity>,
        modelName: String
    ): Pair<BotEntity, List<MessageEntity>> {
        if (messages.size <= 8) return Pair(bot, messages)

        val modelLimit = getModelContextLimit(modelName)
        val maxBudgetTokens = (modelLimit * 0.75).toInt().coerceAtMost(24_000)

        val currentSysPrompt = buildSystemPrompt(bot, getOrCreateSettings())
        val currentTokens = estimateTotalTokens(currentSysPrompt, messages)

        if (currentTokens > maxBudgetTokens || messages.size > 22) {
            val keepCount = 12
            val olderMessages = messages.dropLast(keepCount)
            val recentMessages = messages.takeLast(keepCount)

            if (olderMessages.isNotEmpty()) {
                val updatedBot = summarizeAndArchiveOlderMessages(bot, olderMessages)
                return Pair(updatedBot, recentMessages)
            }
        }

        return Pair(bot, messages)
    }

    private suspend fun summarizeAndArchiveOlderMessages(
        bot: BotEntity,
        olderMessages: List<MessageEntity>
    ): BotEntity {
        val settings = getOrCreateSettings()
        val selectedModel = sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" })

        val aiName = if (bot.mode == "universe") bot.universeName else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Kullanıcı" }

        val recapText = olderMessages.joinToString("\n") { m ->
            val sender = if (m.role == "user") userLabel else aiName
            "$sender: ${m.text}"
        }

        val prompt = "Aşağıdaki geçmiş sahne mesajlarından önemli gelişmeleri ve olay örgüsünü özetle ve SADECE şu formatta yaz:\n\nDURUM:\n- (yan karakterler, mekanlar, çözülmemiş konular — en fazla 5 madde)\n\nHAFIZA:\n- (duygusal gelişmeler, ilişki değişimleri, verilen sözler — en fazla 5 madde)"

        var updatedBot = bot
        var summarizationSucceeded = false

        // 1. AI Tabanlı Özetleme Denemesi (Aktif Sağlayıcı / Model Üzerinden)
        try {
            val requestMsgs = listOf(MessageEntity(id = "sum_old", botId = bot.id, role = "user", text = "$prompt\n\nGEÇMİŞ SAHNE:\n$recapText", timestamp = 0L))
            val res = executeModelRequest(selectedModel, settings, "Sen yardımcı bir hafıza ve olay özetleyicisin.", requestMsgs, botId = bot.id)
            recordTokenUsage(bot.id, res.second.first, res.second.second)
            val raw = res.first

            if (raw.contains("DURUM:", ignoreCase = true) || raw.contains("HAFIZA:", ignoreCase = true)) {
                val durumMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE))[0]
                    .replace(Regex("DURUM:", RegexOption.IGNORE_CASE), "").trim()

                val hafizaMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE)).getOrNull(1)?.trim() ?: ""

                val newStory = listOf(bot.storyNotes, durumMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                val newMemory = listOf(bot.memoryNotes, hafizaMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = newStory,
                    memoryNotes = newMemory,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            summarizationSucceeded = false
        }

        // 2. Yerel Kırpma / Arşivleme Fallback (AI Bağlantısı veya Key Olmadığında)
        if (!summarizationSucceeded) {
            try {
                val localSummaryHeader = "[Önceki Konuşma Arşivi]"
                val sampleOldMsgs = olderMessages.takeLast(8).joinToString("\n") { m ->
                    val sender = if (m.role == "user") userLabel else aiName
                    val snippet = if (m.text.length > 80) m.text.take(80) + "..." else m.text
                    "- $sender: $snippet"
                }
                val fallbackStorySnippet = "$localSummaryHeader\n$sampleOldMsgs"

                val combinedStory = listOf(bot.storyNotes, fallbackStorySnippet).filter { it.isNotBlank() }
                    .joinToString("\n").lines().takeLast(25).joinToString("\n")

                updatedBot = bot.copy(
                    storyNotes = combinedStory,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
                summarizationSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Eski Mesajları Veritabanından Temizleme
        if (summarizationSucceeded) {
            for (m in olderMessages) {
                messageDao.deleteMessageById(m.id)
            }
        }

        return updatedBot
    }

    private fun formatMessagesForGemini(messages: List<MessageEntity>): List<GeminiContent> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Sohbeti başlat."))
                )
            )
        }

        val result = mutableListOf<GeminiContent>()

        // Ensure first content turn starts with user
        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        // Merge consecutive same-role messages
        for (m in adjustedMsgs) {
            val currentRole = if (m.role == "user") "user" else "model"
            if (result.isNotEmpty() && result.last().role == currentRole) {
                val lastContent = result.removeAt(result.size - 1)
                val prevText = lastContent.parts.firstOrNull()?.text ?: ""
                val mergedText = "$prevText\n\n${m.text}"
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = mergedText))
                    )
                )
            } else {
                result.add(
                    GeminiContent(
                        role = currentRole,
                        parts = listOf(GeminiPart(text = m.text))
                    )
                )
            }
        }

        return result
    }

    private fun formatMessagesForStandardApi(messages: List<MessageEntity>): List<Pair<String, String>> {
        val recentMsgs = messages.takeLast(30)
        if (recentMsgs.isEmpty()) {
            return listOf(Pair("user", "Sohbeti başlat."))
        }

        val adjustedMsgs = mutableListOf<MessageEntity>()
        if (recentMsgs.first().role != "user") {
            adjustedMsgs.add(
                MessageEntity(
                    id = "virtual_init",
                    botId = recentMsgs.first().botId,
                    role = "user",
                    text = "Sohbet/Sahne başlatıldı.",
                    timestamp = 0L
                )
            )
        }
        adjustedMsgs.addAll(recentMsgs)

        val result = mutableListOf<Pair<String, String>>()
        for (m in adjustedMsgs) {
            val role = if (m.role == "user") "user" else "assistant"
            if (result.isNotEmpty() && result.last().first == role) {
                val last = result.removeAt(result.size - 1)
                result.add(Pair(role, "${last.second}\n\n${m.text}"))
            } else {
                result.add(Pair(role, m.text))
            }
        }
        return result
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val sanitizedModel = sanitizeModelName(model)
        val geminiContents = formatMessagesForGemini(messages)

        val safetySettings = listOf(
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE"),
            com.example.data.api.GeminiSafetySetting("HARM_CATEGORY_CIVIC_INTEGRITY", "BLOCK_NONE")
        )

        val request = GeminiRequest(
            contents = geminiContents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.85f),
            safetySettings = safetySettings
        )

        val modelsToTry = listOf(sanitizedModel, "gemini-2.5-flash", "gemini-3.5-flash").distinct()
        var lastException: Exception? = null

        for (currModel in modelsToTry) {
            try {
                val response = RetrofitClient.service.generateContent(
                    model = currModel,
                    apiKey = apiKey,
                    request = request
                )

                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw IllegalStateException("Gemini boş yanıt döndürdü.")

                val promptTokens = response.usageMetadata?.promptTokenCount?.toLong() ?: 0L
                val candTokens = response.usageMetadata?.candidatesTokenCount?.toLong() ?: 0L

                return@withContext Pair(text.trim(), Pair(promptTokens, candTokens))
            } catch (e: retrofit2.HttpException) {
                val errorJson = e.response()?.errorBody()?.string()
                val serverMsg = try {
                    JSONObject(errorJson ?: "").optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }

                val rawText = serverMsg ?: e.message() ?: ""
                val isQuota = e.code() == 429 || rawText.contains("quota", ignoreCase = true) || rawText.contains("RESOURCE_EXHAUSTED", ignoreCase = true)

                val userFacingMsg = if (isQuota) {
                    "Gemini API kullanım kotası aşıldı (429 Rate Limit). Lütfen Ayarlar -> AI Model Ayarları menüsünden kendi API anahtarınızı (Gemini, Groq, Claude veya OpenAI) ekleyin."
                } else {
                    serverMsg ?: "Gemini API Hatası [${e.code()}]: ${e.message()}"
                }

                val exc = IllegalStateException(userFacingMsg)
                lastException = exc

                if (e.code() == 404 || isQuota) {
                    continue
                } else {
                    throw exc
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: IllegalStateException("Gemini API çağrısı başarısız oldu. Lütfen Ayarlar'dan API Key'inizi kontrol edin.")
    }

    private suspend fun callOpenAiCompatibleApi(
        endpointUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val standardMsgs = formatMessagesForStandardApi(messages)
        val jsonMessages = JSONArray()
        jsonMessages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        for (m in standardMsgs) {
            jsonMessages.put(JSONObject().apply {
                put("role", m.first)
                put("content", m.second)
            })
        }

        val bodyObj = JSONObject().apply {
            put("model", model)
            put("messages", jsonMessages)
            put("temperature", 0.85)
        }

        val requestBody = bodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val parsedMsg = try {
                    JSONObject(errBody).optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }
                throw IllegalStateException("API Hata [$model] (${response.code}): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val choices = jsonResp.getJSONArray("choices")
            if (choices.length() == 0) throw IllegalStateException("Yanıt boş döndü.")

            val text = choices.getJSONObject(0).getJSONObject("message").getString("content")
            val usage = jsonResp.optJSONObject("usage")
            val promptTokens = usage?.optLong("prompt_tokens") ?: 0L
            val candidateTokens = usage?.optLong("completion_tokens") ?: 0L

            Pair(text.trim(), Pair(promptTokens, candidateTokens))
        }
    }

    private suspend fun callClaudeApi(
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<MessageEntity>
    ): Pair<String, Pair<Long, Long>> = withContext(Dispatchers.IO) {
        val standardMsgs = formatMessagesForStandardApi(messages)
        val jsonMessages = JSONArray()
        for (m in standardMsgs) {
            jsonMessages.put(JSONObject().apply {
                put("role", m.first)
                put("content", m.second)
            })
        }

        val bodyObj = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("system", systemPrompt)
            put("messages", jsonMessages)
        }

        val requestBody = bodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val parsedMsg = try {
                    JSONObject(errBody).optJSONObject("error")?.optString("message")
                } catch (_: Exception) { null }
                throw IllegalStateException("Claude API Hata (${response.code}): ${parsedMsg ?: errBody.take(200)}")
            }
            val responseStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseStr)
            val contentArray = jsonResp.getJSONArray("content")
            if (contentArray.length() == 0) throw IllegalStateException("Claude yanıtı boş.")

            val text = contentArray.getJSONObject(0).getString("text")
            val usage = jsonResp.optJSONObject("usage")
            val promptTokens = usage?.optLong("input_tokens") ?: 0L
            val candidateTokens = usage?.optLong("output_tokens") ?: 0L

            Pair(text.trim(), Pair(promptTokens, candidateTokens))
        }
    }

    suspend fun generateAiReply(
        bot: BotEntity,
        messages: List<MessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val settings = getOrCreateSettings()
        val selectedModel = sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" })

        // Context Window & Token Budget Management
        val (effectiveBot, effectiveMessages) = prepareContextAndSummarizeIfNeeded(bot, messages, selectedModel)
        val systemPrompt = buildSystemPrompt(effectiveBot, settings)

        var primaryException: Exception? = null
        // Primary Execution
        try {
            val result = executeModelRequest(selectedModel, settings, systemPrompt, effectiveMessages, botId = effectiveBot.id)
            recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
            return@withContext result.first
        } catch (e: Exception) {
            if (!settings.enableAutoFallback) {
                throw e
            }
            primaryException = e
        }

        // Auto Fallback to Gemini Secondary Model
        val fallbackModel = sanitizeModelName(settings.fallbackModel.ifBlank { "gemini-2.5-flash" })
        val customKey = settings.customApiKey.trim()
        val buildConfigKey = getBuildConfigKey()
        val backupKey = settings.backupApiKey.trim()

        val primaryKey = if (customKey.isNotBlank()) customKey else buildConfigKey
        val candidateKeys = listOf(primaryKey, backupKey)
            .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            .distinct()

        if (candidateKeys.isEmpty()) {
            throw primaryException ?: IllegalStateException("API Key bulunamadı.")
        }

        var fallbackErr: Exception? = null
        for (k in candidateKeys) {
            try {
                val result = callGeminiApi(k, fallbackModel, systemPrompt, effectiveMessages)
                recordTokenUsage(effectiveBot.id, result.second.first, result.second.second)
                return@withContext result.first
            } catch (err: Exception) {
                fallbackErr = err
            }
        }
        throw IllegalStateException("${primaryException?.message}\n(Yedek model de yanıt veremedi: ${fallbackErr?.message})")
    }

    private suspend fun executeModelRequest(
        model: String,
        settings: UserSettingsEntity,
        systemPrompt: String,
        messages: List<MessageEntity>,
        botId: String? = null
    ): Pair<String, Pair<Long, Long>> {
        return when {
            // Groq Models
            model.contains("llama") || model.contains("groq") || model.contains("mixtral") -> {
                val apiKey = settings.groqApiKey.ifBlank { settings.customApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("Groq API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Groq Key girin.")
                callOpenAiCompatibleApi("https://api.groq.com/openai/v1/chat/completions", apiKey, model, systemPrompt, messages)
            }
            // Claude Models
            model.contains("claude") -> {
                val apiKey = settings.claudeApiKey
                if (apiKey.isBlank()) throw IllegalStateException("Claude API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Claude Key girin.")
                callClaudeApi(apiKey, model, systemPrompt, messages)
            }
            // OpenAI / DeepSeek Models
            model.contains("gpt") || model.contains("deepseek") -> {
                val isDeepseek = model.contains("deepseek")
                val url = if (isDeepseek) "https://api.deepseek.com/chat/completions" else "https://api.openai.com/v1/chat/completions"
                val apiKey = settings.openaiApiKey.ifBlank { settings.groqApiKey }
                if (apiKey.isBlank()) throw IllegalStateException("OpenAI/DeepSeek API Key eksik. Lütfen Ayarlar -> API Anahtarları menüsünden Key girin.")
                callOpenAiCompatibleApi(url, apiKey, model, systemPrompt, messages)
            }
            // Default Gemini Models
            else -> {
                val customKey = settings.customApiKey.trim()
                val buildConfigKey = getBuildConfigKey()
                val backupKey = settings.backupApiKey.trim()

                val primaryKey = if (customKey.isNotBlank()) customKey else buildConfigKey
                val candidateKeys = listOf(primaryKey, backupKey)
                    .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
                    .distinct()

                if (candidateKeys.isEmpty()) {
                    throw IllegalStateException("Gemini API Key eksik. Lütfen Ayarlar -> AI Model Ayarları menüsünden API Key girin.")
                }

                var lastErr: Exception? = null
                for (k in candidateKeys) {
                    try {
                        return callGeminiApi(k, model, systemPrompt, messages)
                    } catch (err: Exception) {
                        lastErr = err
                    }
                }
                throw lastErr ?: IllegalStateException("Gemini API anahtarları ile bağlantı kurulamadı.")
            }
        }
    }

    suspend fun generateOpeningMessage(bot: BotEntity): String = withContext(Dispatchers.IO) {
        val settings = getOrCreateSettings()

        val systemPrompt = buildSystemPrompt(bot, settings, includeStyleGuide = false) + if (bot.writingStyle == "rp") {
            "\n\nGörev: Bu sahneyi başlatan bir açılış anı yaz. Üçüncü tekil şahıs, roman/RP tarzı, betimleme + diyalog içersin. 3-6 cümle. Sadece sahneyi yaz."
        } else {
            "\n\nGörev: Bu senaryoya uygun kısa bir ilk mesaj yaz. Sadece mesajı yaz."
        }

        val requestMsgs = listOf(MessageEntity(id = "init", botId = bot.id, role = "user", text = "Sahneyi/mesajı başlat.", timestamp = 0L))

        val result = executeModelRequest(sanitizeModelName(settings.selectedModel.ifBlank { "gemini-2.5-flash" }), settings, systemPrompt, requestMsgs, botId = bot.id)
        recordTokenUsage(bot.id, result.second.first, result.second.second)
        return@withContext result.first
    }

    suspend fun updateMemorySummaries(bot: BotEntity, messages: List<MessageEntity>) = withContext(Dispatchers.IO) {
        if (messages.size < 6) return@withContext

        val settings = getOrCreateSettings()
        val apiKey = if (settings.customApiKey.isNotBlank()) settings.customApiKey else getBuildConfigKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext

        val aiName = if (bot.mode == "universe") bot.universeName else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Kullanıcı" }

        val recapText = messages.takeLast(16).joinToString("\n") { m ->
            val sender = if (m.role == "user") userLabel else aiName
            "$sender: ${m.text}"
        }

        val prompt = "Aşağıdaki sahneden iki ayrı liste çıkar, SADECE şu formatta yaz, başka hiçbir şey ekleme:\n\nDURUM:\n- (yan karakterler, mekanlar, çözülmemiş konular — en fazla 6 madde)\n\nHAFIZA:\n- (duygusal gelişmeler, ilişki değişimleri, önemli sözler — en fazla 5 madde)"

        try {
            val requestMsgs = listOf(MessageEntity(id = "sum", botId = bot.id, role = "user", text = "$prompt\n\nSAHNE:\n$recapText", timestamp = 0L))
            val res = callGeminiApi(apiKey, "gemini-2.5-flash", "Sen yardımcı bir özetleyicisin.", requestMsgs)
            recordTokenUsage(bot.id, res.second.first, res.second.second)
            val raw = res.first

            if (raw.contains("DURUM:", ignoreCase = true) || raw.contains("HAFIZA:", ignoreCase = true)) {
                val durumMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE))[0]
                    .replace(Regex("DURUM:", RegexOption.IGNORE_CASE), "").trim()

                val hafizaMatch = raw.split(Regex("HAFIZA:", RegexOption.IGNORE_CASE)).getOrNull(1)?.trim() ?: ""

                val newStory = listOf(bot.storyNotes, durumMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(20).joinToString("\n")

                val newMemory = listOf(bot.memoryNotes, hafizaMatch).filter { it.isNotBlank() }.joinToString("\n")
                    .lines().takeLast(20).joinToString("\n")

                val updatedBot = bot.copy(
                    storyNotes = newStory,
                    memoryNotes = newMemory,
                    updatedAt = System.currentTimeMillis()
                )
                botDao.insertOrUpdate(updatedBot)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Import / Export Backup Snapshot ---

    suspend fun exportDataSnapshot(): String = withContext(Dispatchers.IO) {
        val bots = botDao.getAllBotsList()
        val messages = messageDao.getAllMessagesList()
        val settings = getOrCreateSettings()

        val snapshot = BackupSnapshot(
            version = 1,
            bots = bots,
            messages = messages,
            settings = settings
        )
        backupAdapter.toJson(snapshot)
    }

    suspend fun importDataSnapshot(jsonStr: String) = withContext(Dispatchers.IO) {
        val trimmed = jsonStr.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException("İçe aktarılacak metin boş.")
        }

        // 1. Try standard Moshi backup snapshot
        try {
            val snapshot = backupAdapter.fromJson(trimmed)
            if (snapshot != null) {
                settingsDao.insertOrUpdate(snapshot.settings)
                for (bot in snapshot.bots) {
                    botDao.insertOrUpdate(bot)
                }
                if (snapshot.messages.isNotEmpty()) {
                    messageDao.insertAllMessages(snapshot.messages)
                }
                return@withContext
            }
        } catch (_: Exception) {
            // Fallthrough to custom JSON parser
        }

        // 2. Custom robust parser for partial backups, single bots, lists, or character cards
        if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            if (obj.has("bots") || obj.has("messages") || obj.has("settings")) {
                // Partial backup object
                if (obj.has("settings")) {
                    try {
                        val sObj = obj.getJSONObject("settings")
                        val settings = getOrCreateSettings().copy(
                            customApiKey = sObj.optString("customApiKey", ""),
                            groqApiKey = sObj.optString("groqApiKey", ""),
                            claudeApiKey = sObj.optString("claudeApiKey", ""),
                            openaiApiKey = sObj.optString("openaiApiKey", ""),
                            backupApiKey = sObj.optString("backupApiKey", ""),
                            selectedModel = sObj.optString("selectedModel", "gemini-2.5-flash"),
                            fallbackModel = sObj.optString("fallbackModel", "gemini-2.5-flash")
                        )
                        settingsDao.insertOrUpdate(settings)
                    } catch (_: Exception) {}
                }
                if (obj.has("bots")) {
                    val bArr = obj.getJSONArray("bots")
                    for (i in 0 until bArr.length()) {
                        val bObj = bArr.getJSONObject(i)
                        parseAndSaveBotObject(bObj)
                    }
                }
            } else {
                // Single bot or Character Card
                parseAndSaveBotObject(obj)
            }
        } else if (trimmed.startsWith("[")) {
            val arr = JSONArray(trimmed)
            for (i in 0 until arr.length()) {
                val item = arr.get(i)
                if (item is JSONObject) {
                    parseAndSaveBotObject(item)
                }
            }
        } else {
            throw IllegalArgumentException("Geçersiz JSON formatı. Lütfen geçerli bir yedek veya karakter dosyası yapıştırın.")
        }
    }

    private suspend fun parseAndSaveBotObject(obj: JSONObject) {
        val name = obj.optString("aiName").ifBlank {
            obj.optString("name").ifBlank {
                obj.optString("char_name", "İçe Aktarılan Bot")
            }
        }
        val personality = obj.optString("aiPersonality").ifBlank {
            obj.optString("personality").ifBlank {
                obj.optString("description", "")
            }
        }
        val scenario = obj.optString("scenario").ifBlank {
            obj.optString("world_scenario", "")
        }
        val openingMsg = obj.optString("openingMessage").ifBlank {
            obj.optString("firstMessage").ifBlank {
                obj.optString("first_mes").ifBlank {
                    obj.optString("greeting", "Merhaba!")
                }
            }
        }

        val bot = BotEntity(
            id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
            mode = obj.optString("mode", "personal"),
            aiName = name,
            aiPersonality = personality,
            scenario = scenario,
            universeName = obj.optString("universeName", "Evren"),
            keyCharactersJson = obj.optString("keyCharactersJson", "[]"),
            userCharName = obj.optString("userCharName", "Kullanıcı"),
            userCharDesc = obj.optString("userCharDesc", ""),
            openingMessage = openingMsg,
            writingStyle = obj.optString("writingStyle", "Sohbet"),
            intensity = obj.optString("intensity", "Normal"),
            customLength = obj.optString("customLength", "default"),
            isNsfw = obj.optBoolean("isNsfw", true),
            pinnedMemory = obj.optString("pinnedMemory", ""),
            storyNotes = obj.optString("storyNotes", ""),
            memoryNotes = obj.optString("memoryNotes", ""),
            updatedAt = System.currentTimeMillis()
        )
        botDao.insertOrUpdate(bot)
    }
}
