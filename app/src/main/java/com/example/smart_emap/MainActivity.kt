package com.example.smart_emap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.smart_emap.core.system.KeepAwakeHelper
import com.example.smart_emap.ui.navigation.AppNavHost
import com.example.smart_emap.ui.splash.SplashScreenContent
import com.example.smart_emap.ui.theme.SmartEMAPTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { SmartEmapAppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 系统启动页只做黑色过渡；全屏品牌图由 Compose 展示，不能 setKeepOnScreenCondition 否则会挡住 Compose 层
        installSplashScreen()
        super.onCreate(savedInstanceState)
        KeepAwakeHelper.bindActivity(this)
        KeepAwakeHelper.requestBatteryOptimizationExemptionIfNeeded(this)
        enableEdgeToEdge()
        applyImmersiveFullscreen()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(SPLASH_MIN_DISPLAY_MS)
                showSplash = false
            }

            SmartEMAPTheme {
                if (showSplash) {
                    SplashScreenContent()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppNavHost(appContainer = appContainer)
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveFullscreen()
        }
    }

    /** 隐藏状态栏/导航栏，内容铺满平板全屏（可从边缘滑动临时唤出系统栏） */
    private fun applyImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private companion object {
        const val SPLASH_MIN_DISPLAY_MS = 1500L
    }
}
