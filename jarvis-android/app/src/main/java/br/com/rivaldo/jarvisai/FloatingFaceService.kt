package br.com.rivaldo.jarvisai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.BlurMaskFilter
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.sin

class FloatingFaceService : Service() {
    private var windowManager: WindowManager? = null
    private var faceView: JarvisFaceView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (Build.VERSION.SDK_INT >= 23 && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        startAsForeground()
        if (faceView == null) showFace()
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, true).apply()
        return START_STICKY
    }

    private fun startAsForeground() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Rosto flutuante JARVIS", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val openIntent = PendingIntent.getActivity(
            this, 10,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 11,
            Intent(this, FloatingFaceService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val notification = builder
            .setSmallIcon(R.drawable.ic_jarvis_face)
            .setContentTitle("JARVIS está presente")
            .setContentText("O rosto flutuante está ativo. Toque na notificação para abrir.")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Desativar rosto", stopIntent)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showFace() {
        val size = (88 * resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (18 * resources.displayMetrics.density).toInt()
            y = (220 * resources.displayMetrics.density).toInt()
        }
        val view = JarvisFaceView(this)
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.touchStartX = event.rawX
                    view.touchStartY = event.rawY
                    view.startWindowX = params.x
                    view.startWindowY = params.y
                    view.downAt = SystemClock.elapsedRealtime()
                    view.dragged = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - view.touchStartX).toInt()
                    val dy = (event.rawY - view.touchStartY).toInt()
                    if (kotlin.math.abs(dx) > 5 || kotlin.math.abs(dy) > 5) view.dragged = true
                    if (view.dragged) {
                        params.x = view.startWindowX + dx
                        params.y = view.startWindowY + dy
                        try { windowManager?.updateViewLayout(view, params) } catch (_: Exception) { }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val held = SystemClock.elapsedRealtime() - view.downAt
                    if (!view.dragged && held >= 700) {
                        stopSelf()
                    } else if (!view.dragged) {
                        val launch = Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(launch)
                    }
                    true
                }
                else -> true
            }
        }
        faceView = view
        layoutParams = params
        try {
            windowManager?.addView(view, params)
        } catch (_: Exception) {
            faceView = null
            stopSelf()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Mantém o rosto enquanto o usuário não o desativar explicitamente.
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        try {
            faceView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) { }
        faceView = null
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private class JarvisFaceView(context: android.content.Context) : View(context) {
        private val density = resources.displayMetrics.density
        private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        private val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(5, 19, 32)
            style = Paint.Style.FILL
        }
        private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(69, 231, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f * density
        }
        private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(69, 231, 255)
            style = Paint.Style.FILL
        }
        private val pupil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        private val mouth = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(69, 231, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f * density
            strokeCap = Paint.Cap.ROUND
        }
        private val animator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2800L
            repeatCount = android.animation.ValueAnimator.INFINITE
            addUpdateListener { invalidate() }
        }
        var touchStartX = 0f
        var touchStartY = 0f
        var startWindowX = 0
        var startWindowY = 0
        var downAt = 0L
        var dragged = false

        init {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
            animator.start()
            contentDescription = "Rosto flutuante JARVIS. Toque para abrir; mantenha pressionado para fechar."
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val t = (animator.animatedValue as? Float) ?: 0f
            val blinkPhase = (t * 2.0f) % 1f
            val blink = if (blinkPhase in 0.43f..0.49f) 0.12f else 1f
            val look = sin(t * Math.PI * 2.0).toFloat() * 2.5f * density
            val r = width * 0.43f

            glow.color = Color.argb(65, 35, 210, 255)
            glow.style = Paint.Style.FILL
            glow.maskFilter = BlurMaskFilter(9f * density, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(cx, cy, r * 0.94f, glow)
            glow.maskFilter = null
            canvas.drawCircle(cx, cy, r, face)
            canvas.drawCircle(cx, cy, r, outline)

            val head = RectF(cx - r * 0.66f, cy - r * 0.68f, cx + r * 0.66f, cy + r * 0.68f)
            canvas.drawRoundRect(head, r * 0.35f, r * 0.35f, outline)

            val eyeY = cy - r * 0.08f
            val eyeW = r * 0.34f
            val eyeH = r * 0.19f * blink
            val leftEye = RectF(cx - r * 0.52f, eyeY - eyeH, cx - r * 0.52f + eyeW, eyeY + eyeH)
            val rightEye = RectF(cx + r * 0.18f, eyeY - eyeH, cx + r * 0.18f + eyeW, eyeY + eyeH)
            canvas.drawOval(leftEye, eyePaint)
            canvas.drawOval(rightEye, eyePaint)
            if (blink > 0.5f) {
                canvas.drawCircle(leftEye.centerX() + look, eyeY, r * 0.075f, pupil)
                canvas.drawCircle(rightEye.centerX() + look, eyeY, r * 0.075f, pupil)
            }
            canvas.drawArc(
                RectF(cx - r * 0.28f, cy + r * 0.22f, cx + r * 0.28f, cy + r * 0.55f),
                15f, 150f, false, mouth
            )
        }

        override fun onDetachedFromWindow() {
            animator.cancel()
            super.onDetachedFromWindow()
        }
    }

    companion object {
        const val ACTION_START = "br.com.rivaldo.jarvisai.FLOATING_FACE_START"
        const val ACTION_STOP = "br.com.rivaldo.jarvisai.FLOATING_FACE_STOP"
        const val PREFS = "jarvis_floating_face"
        const val KEY_ENABLED = "floating_face_enabled"
        private const val CHANNEL_ID = "jarvis_floating_face"
        private const val NOTIFICATION_ID = 9041
    }
}
