package com.example.smart_emap

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
        // 系统启动页与 Compose 品牌页同色过渡；全屏内容由 SplashScreenContent 展示
        installSplashScreen()
        super.onCreate(savedInstanceState)
        KeepAwakeHelper.bindActivity(this)
        KeepAwakeHelper.requestBatteryOptimizationExemptionIfNeeded(this)
        
        // 彻底隐藏状态栏和导航栏，进入沉浸式全屏模式
        applyImmersiveFullscreen()

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(SPLASH_MIN_DISPLAY_MS)
                showSplash = false
            }

            // 确保在页面切换或重新组合时，系统 UI 保持隐藏
            SideEffect {
                applyImmersiveFullscreen()
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

    /** 隐藏状态栏/导航栏，内容铺满平板全屏（仅能通过边缘滑动临时唤出，且会自动再次隐藏） */
    private fun applyImmersiveFullscreen() {
        // 让内容延伸到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 允许内容绘制到刘海/挖孔屏区域（如果有）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            // 隐藏状态栏（时间/电池）和导航栏（底栏）
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // 设置行为：滑动唤出后自动隐藏，不改变应用布局
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private companion object {
        const val SPLASH_MIN_DISPLAY_MS = 1500L
    }
}

