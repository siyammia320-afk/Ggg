package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

class FloatingWidgetService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, HasDefaultViewModelProviderFactory {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = ViewModelProvider.AndroidViewModelFactory.getInstance(application)

    private lateinit var windowManager: WindowManager
    private var bubbleView: ComposeView? = null
    private var dialogView: ComposeView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var dialogParams: WindowManager.LayoutParams

    private var isDialogShowing = false

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        const val CHANNEL_ID = "floating_widget_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, FloatingWidgetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingWidgetService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        _isRunning.value = true
        startForegroundNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        setupBubbleView()
        setupDialogView()
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Widget Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs the floating widget over other apps"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Widget Active")
            .setContentText("Tap floating icon on screen to open full app")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                )
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupBubbleView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 300
        }

        bubbleView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingWidgetService)
            setViewTreeViewModelStoreOwner(this@FloatingWidgetService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWidgetService)

            setContent {
                FloatingBubbleContent()
            }
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchDownTime = 0L

        bubbleView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchDownTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    bubbleParams.x = initialX + deltaX
                    bubbleParams.y = initialY + deltaY
                    bubbleView?.let {
                        if (it.parent != null) {
                            try {
                                windowManager.updateViewLayout(it, bubbleParams)
                            } catch (_: Exception) {}
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    val duration = System.currentTimeMillis() - touchDownTime
                    if (diffX < 35 && diffY < 35 && duration < 600) {
                        openDialogOverlay()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupDialogView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        dialogParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        dialogView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingWidgetService)
            setViewTreeViewModelStoreOwner(this@FloatingWidgetService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWidgetService)

            setContent {
                com.example.ui.theme.FBTheme {
                    FloatingDialogOverlayContent(
                        onCloseClick = { closeDialogOverlay() },
                        onStopServiceClick = { stopSelf() }
                    )
                }
            }
        }
    }

    private fun openDialogOverlay() {
        if (isDialogShowing) return
        try {
            if (dialogView == null) {
                setupDialogView()
            }
            if (dialogView != null && dialogView?.parent == null) {
                windowManager.addView(dialogView, dialogParams)
            }
            dialogView?.visibility = View.VISIBLE
            bubbleView?.visibility = View.GONE
            isDialogShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                bubbleView?.visibility = View.VISIBLE
            } catch (_: Exception) {}
            isDialogShowing = false
        }
    }

    private fun closeDialogOverlay() {
        try {
            if (dialogView != null && dialogView?.parent != null) {
                windowManager.removeView(dialogView)
            }
            bubbleView?.visibility = View.VISIBLE
            isDialogShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        _isRunning.value = false
        isDialogShowing = false
        try {
            if (bubbleView != null && bubbleView?.parent != null) {
                windowManager.removeView(bubbleView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (dialogView != null && dialogView?.parent != null) {
                windowManager.removeView(dialogView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bubbleView = null
        dialogView = null

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }
}

@Composable
fun FloatingBubbleContent() {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(Color(0xFF0284C7))
            .border(2.5.dp, Color(0xFF38BDF8), CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ARAFAT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "FB ⚡",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
        }
    }
}

@Composable
fun FloatingDialogOverlayContent(
    onCloseClick: () -> Unit,
    onStopServiceClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                onClick = onCloseClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clickable(
                    onClick = { /* consume clicks inside overlay dialog card */ },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Text(
                            text = "ARAFAT FB CREATOR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // STOP SERVICE button inside dialog header
                        IconButton(
                            onClick = onStopServiceClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Service",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // CLOSE (X) button inside dialog header
                        IconButton(
                            onClick = onCloseClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Dialog",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Body Content (Full App)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    com.example.FullFeatureAppContent()
                }
            }
        }
    }
}
