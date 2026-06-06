package com.example.smart_emap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.smart_emap.core.system.KeepAwakeHelper
import com.example.smart_emap.ui.navigation.AppNavHost
import com.example.smart_emap.ui.theme.SmartEMAPTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { SmartEmapAppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KeepAwakeHelper.bindActivity(this)
        KeepAwakeHelper.requestBatteryOptimizationExemptionIfNeeded(this)
        enableEdgeToEdge()
        applyImmersiveFullscreen()
        setContent {
            SmartEMAPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost(appContainer = appContainer)
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
}
