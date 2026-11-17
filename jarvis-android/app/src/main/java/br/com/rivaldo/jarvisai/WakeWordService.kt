package br.com.rivaldo.jarvisai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.openwakeword.OpenWakeWord

class WakeWordService : Service() {
    private var wakeDetector: OpenWakeWord? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopDetector()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(
            "JARVIS está em escuta. Diga: Hey Jarvis.",
            NOTIFICATION_ID
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (wakeDetector == null) {
            try {
                wakeDetector = OpenWakeWord.Builder(this)
                    .setModel(OpenWakeWord.BuiltInModel.HEY_JARVIS)
                    .setThreshold(0.5f)
                    .setDebounceMs(2500)
                    .build()

                wakeDetector?.start { score ->
                    Log.i(TAG, "Wake word detectada. Score=$score")
                    onWakeWordDetected()
                }
            } catch (error: Exception) {
                Log.e(TAG, "Não foi possível iniciar o openWakeWord", error)
                showStatusNotification(
                    "Falha ao iniciar a ativação por voz: ${error.message ?: "erro inesperado"}"
                )
                stopDetector()
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun onWakeWordDetected() {
        stopDetector()
        val notification = buildNotification(
            "JARVIS ouviu Hey Jarvis. Toque para falar com o assistente.",
            WAKE_NOTIFICATION_ID,
            openAssistant = true
        )
        getSystemService(NotificationManager::class.java).notify(WAKE_NOTIFICATION_ID, notification)
        sendBroadcast(Intent(ACTION_WAKE_DETECTED).setPackage(packageName))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showStatusNotification(message: String) {
        val notification = buildNotification(message, WAKE_NOTIFICATION_ID, openAssistant = true)
        getSystemService(NotificationManager::class.java).notify(WAKE_NOTIFICATION_ID, notification)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(
        message: String,
        notificationId: Int,
        openAssistant: Boolean = false
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            if (openAssistant) putExtra(MainActivity.EXTRA_START_LISTENING, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("JARVIS AI")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(!openAssistant)
            .setAutoCancel(openAssistant)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(if (openAssistant) Notification.PRIORITY_HIGH else Notification.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ativação por voz do JARVIS",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Indica quando a detecção da palavra de ativação está ligada."
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun stopDetector() {
        try { wakeDetector?.stop() } catch (error: Exception) {
            Log.w(TAG, "Erro ao parar detector", error)
        }
        try { wakeDetector?.release() } catch (error: Exception) {
            Log.w(TAG, "Erro ao liberar detector", error)
        }
        wakeDetector = null
    }

    override fun onDestroy() {
        stopDetector()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "JarvisWakeWord"
        const val ACTION_START = "br.com.rivaldo.jarvisai.action.START_WAKE"
        const val ACTION_STOP = "br.com.rivaldo.jarvisai.action.STOP_WAKE"
        const val ACTION_WAKE_DETECTED = "br.com.rivaldo.jarvisai.action.WAKE_DETECTED"
        const val CHANNEL_ID = "jarvis_wake_word"
        const val NOTIFICATION_ID = 7101
        const val WAKE_NOTIFICATION_ID = 7102
    }
}
