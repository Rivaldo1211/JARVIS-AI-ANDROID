package br.com.rivaldo.jarvisai

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

class JarvisScreenReaderService : AccessibilityService(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val handler = Handler(Looper.getMainLooper())
    private var registered = false
    private var lastSpoken = ""
    private val readReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_READ_SCREEN) readCurrentScreen()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        tts = TextToSpeech(this, this)
        if (!registered) {
            val filter = IntentFilter(ACTION_READ_SCREEN)
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(readReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            else {
                @Suppress("DEPRECATION")
                registerReceiver(readReceiver, filter)
            }
            registered = true
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
            tts?.setSpeechRate(0.92f)
            tts?.setPitch(0.88f)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_READ_NEXT, false)) return
        val packageName = event.packageName?.toString().orEmpty()
        if (!isAllowedPackage(packageName)) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) return
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ readCurrentScreen() }, 1300)
    }

    private fun isAllowedPackage(packageName: String): Boolean {
        return packageName == applicationContext.packageName ||
            packageName == "com.android.chrome" ||
            packageName == "com.google.android.googlequicksearchbox" ||
            packageName == "com.google.android.youtube"
    }

    private fun readCurrentScreen() {
        val root = rootInActiveWindow ?: return
        val packageName = root.packageName?.toString().orEmpty()
        if (!isAllowedPackage(packageName)) return
        val pieces = mutableListOf<String>()
        collectText(root, pieces)
        val content = pieces.distinct()
            .filter { it.length > 2 }
            .joinToString(". ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(1100)
        if (content.length < 20 || content == lastSpoken) return
        lastSpoken = content
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_READ_NEXT, false)
            .putString(KEY_LAST_TEXT, content)
            .apply()
        val intro = "Encontrei este conteúdo na tela. "
        tts?.speak(intro + content, TextToSpeech.QUEUE_FLUSH, null, "jarvis-screen-reader")
    }

    private fun collectText(node: AccessibilityNodeInfo?, output: MutableList<String>) {
        if (node == null || output.size >= 100) return
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { output.add(it) }
        for (index in 0 until node.childCount) collectText(node.getChild(index), output)
    }

    override fun onInterrupt() {
        tts?.stop()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (registered) {
            unregisterReceiver(readReceiver)
            registered = false
        }
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        const val ACTION_READ_SCREEN = "br.com.rivaldo.jarvisai.action.READ_SCREEN"
        const val PREFS = "jarvis_screen_reader"
        const val KEY_READ_NEXT = "read_next_screen"
        const val KEY_LAST_TEXT = "last_screen_text"
    }
}
