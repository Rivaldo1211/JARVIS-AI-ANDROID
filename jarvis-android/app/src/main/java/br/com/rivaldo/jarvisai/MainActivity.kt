package br.com.rivaldo.jarvisai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLEncoder
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var status by mutableStateOf("Inicializando sistemas...")
    private var transcript by mutableStateOf("Toque no botão e diga: Jarvis, abra o YouTube.")
    private var answer by mutableStateOf("Olá. Sou o JARVIS. Estou pronto para receber comandos.")
    private var listening by mutableStateOf(false)
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        status = if (SpeechRecognizer.isRecognitionAvailable(this)) {
            "Sistema de voz disponível"
        } else {
            "Reconhecimento de voz indisponível neste aparelho"
        }

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) startListening()
                else {
                    status = "Permissão de microfone negada"
                    answer = "Preciso da permissão do microfone para ouvir seus comandos."
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF050B16)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF101F38), Color(0xFF050B16), Color.Black)
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("J.A.R.V.I.S.", color = Color(0xFF64D8FF), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text("JUST A RATHER VERY INTELLIGENT SYSTEM", color = Color(0xFF8CA9C8), fontSize = 9.sp)
                        Spacer(Modifier.height(42.dp))
                        Surface(
                            modifier = Modifier.padding(8.dp),
                            shape = CircleShape,
                            color = Color(0xFF102B49)
                        ) {
                            Text(
                                if (listening) "•••" else "J",
                                modifier = Modifier.padding(horizontal = 48.dp, vertical = 38.dp),
                                color = Color(0xFF64D8FF),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(status, color = Color(0xFF64D8FF), fontSize = 13.sp)
                        Spacer(Modifier.height(20.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF101B2B),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text("VOCÊ DISSE", color = Color(0xFF64D8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(transcript, color = Color.White, modifier = Modifier.padding(top = 6.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("JARVIS", color = Color(0xFF64D8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(answer, color = Color(0xFFE4EDF8), modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = {
                                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087EAD)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (listening) "OUVINDO..." else "FALAR COM JARVIS", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Versão inicial • Comandos locais por voz", color = Color(0xFF71849C), fontSize = 11.sp)
                    }
                }
            }
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
            answer = "Olá. Sou o JARVIS. Como posso ajudar?"
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status = "Serviço de reconhecimento de voz não encontrado"
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { listening = true; status = "Estou ouvindo..." }
                override fun onBeginningOfSpeech() { status = "Pode falar" }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { listening = false; status = "Processando comando..." }
                override fun onError(error: Int) {
                    listening = false
                    status = "Não consegui reconhecer. Tente novamente."
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
        val command = raw.lowercase(Locale("pt", "BR")).trim()
        when {
            command.contains("youtube") -> openApp("com.google.android.youtube", "YouTube")
            command.contains("whatsapp") -> openApp("com.whatsapp", "WhatsApp")
            command.contains("chrome") || command.contains("navegador") -> openApp("com.android.chrome", "Chrome")
            command.startsWith("pesquise") || command.startsWith("pesquisar") || command.startsWith("procure") -> {
                val query = command
                    .removePrefix("pesquise").removePrefix("pesquisar").removePrefix("procure")
                    .replaceFirst(Regex("^\\s+(por|sobre)\\s+"), "").trim()
                if (query.isBlank()) {
                    speak("O que você gostaria que eu pesquisasse?")
                } else {
                    speak("Vou pesquisar $query")
                    val url = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                }
            }
            command.startsWith("abra ") || command.startsWith("abrir ") -> {
                val target = command.removePrefix("abra ").removePrefix("abrir ").trim()
                when {
                    target.contains("configura") -> openSettings()
                    target.contains("youtube") -> openApp("com.google.android.youtube", "YouTube")
                    target.contains("whatsapp") -> openApp("com.whatsapp", "WhatsApp")
                    target.contains("chrome") || target.contains("internet") -> openApp("com.android.chrome", "Chrome")
                    else -> speak("Ainda não tenho o atalho para $target. Vou adicionar mais aplicativos nas próximas versões.")
                }
            }
            command.contains("que horas") -> speak("Você pode conferir o horário na barra superior do seu celular.")
            command.contains("olá") || command.contains("oi") || command.contains("jarvis") -> speak("Olá. Estou à disposição.")
            else -> askJarvis(raw)
        }
    }

    private fun askJarvis(message: String) {
        val baseUrl = BuildConfig.JARVIS_API_URL.trim().trimEnd('/')
        if (baseUrl.isBlank()) {
            speak("A inteligência artificial está preparada, mas falta publicar o servidor e configurar o endereço da API.")
            return
        }

        status = "Consultando o Gemini..."
        answer = "Estou pensando..."
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val historyJson = JSONArray()
                conversationHistory.forEach { item ->
                    historyJson.put(
                        JSONObject()
                            .put("role", item.first)
                            .put("content", item.second)
                    )
                }

                val requestBody = JSONObject()
                    .put("message", message)
                    .put("history", historyJson)
                    .toString()

                connection = (URL("$baseUrl/v1/chat").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 45000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }

                connection.outputStream.use { output ->
                    output.write(requestBody.toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseStream = if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val responseText = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (statusCode !in 200..299) {
                    throw IllegalStateException("Servidor respondeu HTTP $statusCode")
                }

                val reply = JSONObject(responseText).optString("reply").trim()
                if (reply.isBlank()) throw IllegalStateException("Resposta vazia")

                conversationHistory.add("user" to message)
                conversationHistory.add("assistant" to reply)
                while (conversationHistory.size > 12) {
                    conversationHistory.removeAt(0)
                }

                runOnUiThread { speak(reply) }
            } catch (_: Exception) {
                runOnUiThread {
                    speak("Não consegui acessar o servidor de inteligência artificial. Confira sua conexão e tente novamente.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
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
        status = "Pronto"
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
