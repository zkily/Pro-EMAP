package com.example.smart_emap.core.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

enum class PrintPageLayout {
    A4_PORTRAIT_SINGLE,
    /** A4 縦・カラー・長辺とじ両面（推奨生産日リスト用） */
    A4_PORTRAIT_DUPLEX_COLOR,
    A4_LANDSCAPE_SINGLE,
    A5_LANDSCAPE_SINGLE,
    A3_LANDSCAPE_SINGLE,
}

/** 加载 HTML 并打开系统印刷对话框（等同 Web 端 `openPrintWindow` + `window.print()`）。 */
object HtmlPrintHelper {

    private const val MARGIN_8MM_MILS = 315
    private const val MARGIN_10MM_MILS = 394
    private val activeWebViews = mutableSetOf<WebView>()

    fun PrintPageLayout.toAttributes(): PrintAttributes = when (this) {
        PrintPageLayout.A4_PORTRAIT_SINGLE -> PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setMinMargins(
                PrintAttributes.Margins(MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS),
            )
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE)
            .build()
        PrintPageLayout.A4_PORTRAIT_DUPLEX_COLOR -> PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setMinMargins(
                PrintAttributes.Margins(MARGIN_10MM_MILS, MARGIN_10MM_MILS, MARGIN_10MM_MILS, MARGIN_10MM_MILS),
            )
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setDuplexMode(PrintAttributes.DUPLEX_MODE_LONG_EDGE)
            .build()
        PrintPageLayout.A4_LANDSCAPE_SINGLE -> PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
            .setMinMargins(
                PrintAttributes.Margins(MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS),
            )
            .setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE)
            .build()
        PrintPageLayout.A5_LANDSCAPE_SINGLE -> PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A5.asLandscape())
            .setMinMargins(
                PrintAttributes.Margins(0, MARGIN_10MM_MILS, 0, MARGIN_10MM_MILS),
            )
            .setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE)
            .build()
        PrintPageLayout.A3_LANDSCAPE_SINGLE -> PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A3.asLandscape())
            .setMinMargins(
                PrintAttributes.Margins(MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS, MARGIN_8MM_MILS),
            )
            .setDuplexMode(PrintAttributes.DUPLEX_MODE_NONE)
            .build()
    }

    fun printHtml(
        context: Context,
        html: String,
        jobName: String,
        layout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    ): Boolean {
        val activity = context.findActivity() ?: return false
        val webView = WebView(activity).apply {
            settings.defaultTextEncodingName = "UTF-8"
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = activity.getSystemService(PrintManager::class.java) ?: return
                    val adapter = view.createPrintDocumentAdapter(jobName)
                    printManager.print(
                        jobName,
                        adapter,
                        layout.toAttributes(),
                    )
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            activeWebViews.remove(view)
                            view.destroy()
                        },
                        120_000,
                    )
                }
            }
            loadDataWithBaseURL(null, html, "text/html; charset=UTF-8", "UTF-8", null)
        }
        activeWebViews.add(webView)
        return true
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
