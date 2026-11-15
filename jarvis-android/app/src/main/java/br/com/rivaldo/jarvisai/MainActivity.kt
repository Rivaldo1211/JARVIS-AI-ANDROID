package br.com.rivaldo.jarvisai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLEncoder
import org.json.JSONObject
import java.util.Locale

private val VoidBlack = Color(0xFF03070D)
private val Panel = Color(0xFF091522)
private val Cyan = Color(0xFF45E7FF)
private val Ice = Color(0xFFBCEFFF)
private val Muted = Color(0xFF6F8DA3)
private val Gold = Color(0xFFFFC66D)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var status by mutableStateOf("INICIALIZANDO NÚCLEO")
    private var transcript by mutableStateOf("Aguardando comando de voz...")
    private var answer by mutableStateOf("Sistemas em espera. Como posso ajudar?")
    private var listening by mutableStateOf(false)
    // Memória persistente local: não envia perfil nem conversas para servidores.
    private val memoryStore by lazy { getSharedPreferences("jarvis_personal_memory", MODE_PRIVATE) }

    private fun loadFacts(): JSONObject =
        try { JSONObject(memoryStore.getString("facts", "{}") ?: "{}") } catch (_: Exception) { JSONObject() }

    private fun saveFact(key: String, value: String) {
        val facts = loadFacts()
        facts.put(key, value)
        memoryStore.edit().putString("facts", facts.toString()).apply()
    }

    private fun profileSummary(): String {
        val facts = loadFacts()
        if (facts.length() == 0) return "Ainda estou construindo seu perfil. Você pode me contar seu nome, preferências, interesses e objetivos."
        val labels = linkedMapOf(
            "nome" to "Nome", "gosta" to "Interesses", "prefere" to "Preferência",
            "trabalho" to "Trabalho", "objetivo" to "Objetivo", "cidade" to "Localidade",
            "estilo" to "Estilo de conversa"
        )
        return labels.entries.filter { facts.has(it.key) }
            .joinToString(". ") { "${it.value}: ${facts.optString(it.key)}" }
            .ifBlank { "Ainda não tenho informações suficientes para resumir seu perfil." }
    }

    private fun learnFromMessage(message: String): String? {
        val patterns = listOf(
            "nome" to Regex("""(?:meu nome é|me chamo|pode me chamar de)\\s+(.{2,60})""", RegexOption.IGNORE_CASE),
            "gosta" to Regex("""(?:eu gosto de|adoro|tenho interesse em)\\s+(.{2,100})""", RegexOption.IGNORE_CASE),
            "prefere" to Regex("""(?:eu prefiro|prefiro)\\s+(.{2,100})""", RegexOption.IGNORE_CASE),
            "trabalho" to Regex("""(?:eu trabalho com|trabalho na área de|minha profissão é)\\s+(.{2,100})""", RegexOption.IGNORE_CASE),
            "objetivo" to Regex("""(?:meu objetivo é|quero alcançar|estou buscando)\\s+(.{2,120})""", RegexOption.IGNORE_CASE),
            "cidade" to Regex("""(?:moro em|sou de)\\s+(.{2,80})""", RegexOption.IGNORE_CASE),
            "estilo" to Regex("""(?:gosto de respostas|prefiro respostas)\\s+(.{2,100})""", RegexOption.IGNORE_CASE)
        )
        for ((key, regex) in patterns) {
            val match = regex.find(message) ?: continue
            val value = match.groupValues[1].trim().trimEnd('.', '!', '?')
            if (value.isNotBlank()) {
                saveFact(key, value)
                val label = when (key) {
                    "nome" -> "seu nome"
                    "gosta" -> "seus interesses"
                    "prefere" -> "sua preferência"
                    "trabalho" -> "sua área de trabalho"
                    "objetivo" -> "seu objetivo"
                    "cidade" -> "sua localidade"
                    else -> "seu estilo de conversa"
                }
                return "Entendido. Vou guardar essa informação sobre $label neste aparelho."
            }
        }
        return null
    }

    private fun localIntelligence(message: String) {
        val normalized = message.lowercase(Locale("pt", "BR")).trim()
        val facts = loadFacts()
        val learned = learnFromMessage(message)
        if (learned != null) { speak(learned); return }

        when {
            normalized.contains("o que você sabe sobre mim") ||
                normalized.contains("meu perfil") ||
                normalized.contains("o que sabe de mim") ->
                speak(profileSummary())
            normalized.contains("qual meu nome") || normalized.contains("como me chamo") -> {
                val name = facts.optString("nome")
                speak(if (name.isNotBlank()) "Seu nome é $name." else "Ainda não sei seu nome. Você pode dizer: meu nome é...")
            }
            normalized.contains("esqueça tudo") || normalized.contains("apague minha memória") -> {
                memoryStore.edit().clear().apply()
                speak("Minha memória pessoal local foi apagada.")
            }
            normalized.contains("esqueça meu nome") -> {
                facts.remove("nome")
                memoryStore.edit().putString("facts", facts.toString()).apply()
                speak("Apaguei essa informação da minha memória.")
            }
            normalized.startsWith("abra ") || normalized.startsWith("abrir ") -> {
                val target = normalized.removePrefix("abra ").removePrefix("abrir ").trim()
                when {
                    target.contains("configura") -> openSettings()
                    target.contains("youtube") -> openApp("com.google.android.youtube", "YouTube")
                    target.contains("whatsapp") -> openApp("com.whatsapp", "WhatsApp")
                    target.contains("chrome") || target.contains("internet") -> openApp("com.android.chrome", "Chrome")
                    else -> speak("Ainda não tenho o atalho para $target.")
                }
            }
            normalized.contains("que horas") ->
                speak("Você pode conferir o horário na barra superior do seu celular.")
            normalized.contains("obrigado") || normalized.contains("obrigada") ->
                speak("À disposição. Estou aprendendo suas preferências para tornar nossas próximas conversas mais naturais.")
            normalized == "oi" || normalized == "olá" || normalized == "bom dia" ||
                normalized == "boa tarde" || normalized == "boa noite" ||
                normalized == "jarvis" || normalized == "oi jarvis" || normalized == "olá jarvis" -> {
                val name = facts.optString("nome")
                speak(if (name.isNotBlank()) "Olá, $name. É bom continuar nossa conversa." else "Olá. Ainda estou conhecendo você. Como prefere que eu te chame?")
            }
            normalized.contains("youtube") -> openApp("com.google.android.youtube", "YouTube")
            normalized.contains("whatsapp") -> openApp("com.whatsapp", "WhatsApp")
            normalized.contains("chrome") || normalized.contains("navegador") -> openApp("com.android.chrome", "Chrome")
            normalized.startsWith("pesquise") || normalized.startsWith("pesquisar") || normalized.startsWith("procure") -> {
                val query = normalized.replaceFirst(Regex("^(pesquise|pesquisar|procure)\\\\s*(por|sobre)?\\\\s*"), "").trim()
                if (query.isBlank()) speak("O que gostaria que eu pesquisasse?") else {
                    speak("Vou pesquisar $query")
                    val url = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                }
            }
            else -> {
                val name = facts.optString("nome").takeIf { it.isNotBlank() }?.let { "$it, " }.orEmpty()
                val interest = facts.optString("gosta")
                val context = if (interest.isNotBlank()) " Sei que você tem interesse em $interest." else ""
                speak("${name}Ainda não tenho um mecanismo de raciocínio generativo instalado neste aparelho. Posso aprender fatos e preferências que você me contar, lembrar seu perfil e executar comandos locais.$context Para melhorar, conte algo sobre você ou diga 'o que você sabe sobre mim'.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        status = if (SpeechRecognizer.isRecognitionAvailable(this)) {
            "SISTEMAS ONLINE"
        } else {
            "MÓDULO DE VOZ INDISPONÍVEL"
        }

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) startListening()
                else {
                    status = "ACESSO AO MICROFONE NEGADO"
                    answer = "Preciso da permissão do microfone para ouvir seus comandos."
                }
            }

            val transition = rememberInfiniteTransition(label = "jarvis-hud")
            val rotation by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 14000, easing = LinearEasing)
                ),
                label = "hud-rotation"
            )
            val pulse by transition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "hud-pulse"
            )

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VoidBlack) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0B2030), VoidBlack, Color(0xFF020409))
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 22.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "STARK INDUSTRIES  /  AI",
                                        color = Muted,
                                        fontSize = 9.sp,
                                        letterSpacing = 2.2.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "J.A.R.V.I.S.",
                                        color = Ice,
                                        fontSize = 30.sp,
                                        letterSpacing = 4.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (listening) Gold else Cyan)
                                        )
                                        Spacer(Modifier.width(7.dp))
                                        Text(
                                            if (listening) "CAPTANDO" else "ONLINE",
                                            color = if (listening) Gold else Cyan,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text("CORE  /  V0.4", color = Muted, fontSize = 9.sp)
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0x3345E7FF), RoundedCornerShape(26.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0B2435), Color(0xFF06111D))
                                        ),
                                        RoundedCornerShape(26.dp)
                                    )
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "NEURAL INTERFACE",
                                        color = Muted,
                                        fontSize = 9.sp,
                                        letterSpacing = 3.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier.size(258.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(Modifier.fillMaxSize()) {
                                            val center = this.center
                                            val baseRadius = size.minDimension * 0.39f
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    listOf(Color(0x5545E7FF), Color.Transparent),
                                                    center = center,
                                                    radius = baseRadius * 1.55f
                                                ),
                                                radius = baseRadius * 1.55f,
                                                center = center
                                            )
                                            drawCircle(
                                                color = Color(0x6645E7FF),
                                                radius = baseRadius,
                                                center = center,
                                                style = Stroke(width = 1.5.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color(0x5545E7FF),
                                                radius = baseRadius * 0.82f,
                                                center = center,
                                                style = Stroke(width = 1.dp.toPx())
                                            )
                                            drawArc(
                                                color = Cyan,
                                                startAngle = rotation,
                                                sweepAngle = 82f,
                                                useCenter = false,
                                                topLeft = androidx.compose.ui.geometry.Offset(
                                                    center.x - baseRadius * 1.12f,
                                                    center.y - baseRadius * 1.12f
                                                ),
                                                size = androidx.compose.ui.geometry.Size(
                                                    baseRadius * 2.24f,
                                                    baseRadius * 2.24f
                                                ),
                                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                color = Gold.copy(alpha = 0.85f),
                                                startAngle = -rotation * 0.65f,
                                                sweepAngle = 38f,
                                                useCenter = false,
                                                topLeft = androidx.compose.ui.geometry.Offset(
                                                    center.x - baseRadius * 0.92f,
                                                    center.y - baseRadius * 0.92f
                                                ),
                                                size = androidx.compose.ui.geometry.Size(
                                                    baseRadius * 1.84f,
                                                    baseRadius * 1.84f
                                                ),
                                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            for (i in 0 until 48) {
                                                val angle = Math.toRadians(i * 7.5)
                                                val inner = baseRadius * 1.18f
                                                val outer = inner + if (i % 4 == 0) 8.dp.toPx() else 3.dp.toPx()
                                                val x1 = center.x + kotlin.math.cos(angle).toFloat() * inner
                                                val y1 = center.y + kotlin.math.sin(angle).toFloat() * inner
                                                val x2 = center.x + kotlin.math.cos(angle).toFloat() * outer
                                                val y2 = center.y + kotlin.math.sin(angle).toFloat() * outer
                                                drawLine(
                                                    color = if (i % 4 == 0) Cyan.copy(alpha = 0.8f) else Muted.copy(alpha = 0.5f),
                                                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                                                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                                                    strokeWidth = if (i % 4 == 0) 2.dp.toPx() else 1.dp.toPx()
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size((116 * pulse).dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(Color(0xFF17445A), Color(0xFF071522))
                                                    )
                                                )
                                                .border(1.dp, Cyan.copy(alpha = 0.65f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "J",
                                                color = Ice,
                                                fontSize = 66.sp,
                                                fontWeight = FontWeight.ExtraLight,
                                                fontFamily = FontFamily.SansSerif
                                            )
                                        }
                                    }
                                    Text(
                                        if (listening) "ANALISANDO SINAL DE ÁUDIO" else "AGUARDANDO INSTRUÇÕES",
                                        color = if (listening) Gold else Cyan,
                                        fontSize = 10.sp,
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(status, color = Muted, fontSize = 10.sp)
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            HudMessageCard(
                                label = "ENTRADA DE VOZ",
                                content = transcript,
                                accent = Muted
                            )
                            Spacer(Modifier.height(10.dp))
                            HudMessageCard(
                                label = "RESPOSTA DO JARVIS",
                                content = answer,
                                accent = Cyan
                            )

                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(58.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0B7897),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    if (listening) "ESCUTANDO..." else "INICIAR COMUNICAÇÃO",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "VOICE INTERFACE  •  SECURE CONNECTION  •  PERSONAL AI",
                                color = Color(0xFF456176),
                                fontSize = 8.sp,
                                letterSpacing = 1.1.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            configureJarvisVoice()
            answer = "Sistemas online. Estou à sua disposição."
        } else {
            status = "SÍNTESE DE VOZ INDISPONÍVEL"
        }
    }

    private fun configureJarvisVoice() {
        val engine = tts ?: return
        val locale = Locale("pt", "BR")
        val languageResult = engine.setLanguage(locale)
        if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            status = "VOZ PORTUGUÊS BRASIL NÃO DISPONÍVEL"
        }

        val brazilianVoices = engine.voices.orEmpty().filter {
            it.locale.language == "pt" && it.locale.country.equals("BR", ignoreCase = true)
        }
        val maleNamedVoice = brazilianVoices.firstOrNull { voice ->
            val voiceName = voice.name.lowercase(Locale.ROOT)
            voiceName.contains("male") ||
                voiceName.contains("mascul") ||
                voiceName.contains("homem")
        }
        val bestAvailableVoice: Voice? = maleNamedVoice
            ?: brazilianVoices.maxByOrNull { it.quality }

        if (bestAvailableVoice != null) {
            engine.voice = bestAvailableVoice
        }

        // Timbre mais grave e ritmo calmo. O gênero real da voz depende das vozes instaladas no aparelho.
        engine.setPitch(0.82f)
        engine.setSpeechRate(0.92f)
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status = "SERVIÇO DE RECONHECIMENTO NÃO ENCONTRADO"
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "CAPTANDO COMANDO" }
                override fun onBeginningOfSpeech() { status = "PODE FALAR" }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { listening = false; status = "PROCESSANDO" }
                override fun onError(error: Int) {
                    listening = false
                    status = "NÃO FOI POSSÍVEL RECONHECER"
                }
                override fun onResults(results: Bundle?) {
                    listening = false
                    val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    transcript = phrase
                    if (phrase.isNotBlank()) handleCommand(phrase) else speak("Não consegui entender. Pode repetir?")
                }
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga seu comando para o JARVIS")
            }
            recognizer.startListening(intent)
        }
    }

    private fun handleCommand(raw: String) {
        localIntelligence(raw)
    }

    private fun openApp(packageName: String, label: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            speak("Abrindo $label.")
            startActivity(launchIntent)
        } else {
            speak("Não encontrei $label instalado neste aparelho.")
        }
    }

    private fun openSettings() {
        speak("Abrindo configurações.")
        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
    }

    private fun speak(message: String) {
        answer = message
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis-response")
        status = "SISTEMAS ONLINE"
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
private fun HudMessageCard(label: String, content: String, accent: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(16.dp)),
        color = Panel,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                label,
                color = accent,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                content,
                color = Color(0xFFD9EAF2),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}
