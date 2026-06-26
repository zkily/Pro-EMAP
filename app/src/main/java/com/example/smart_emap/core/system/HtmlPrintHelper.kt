package com.example.smart_emap.core.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebSettings
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
    private const val DEFAULT_CONTENT_BASE_URL = "https://smart-emap.local/"
    private const val IMAGE_READY_MAX_RETRIES = 40
    private const val IMAGE_READY_DELAY_MS = 100L
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

    /**
     * @param contentBaseUrl HTML 内相对路径资源（如图表 PNG）的基准 URL。
     *   例: `file:///data/.../cache/welding_print/`（末尾须带 `/`）
     */
    fun printHtml(
        context: Context,
        html: String,
        jobName: String,
        layout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
        contentBaseUrl: String = DEFAULT_CONTENT_BASE_URL,
    ): Boolean {
        val activity = context.findActivity() ?: return false
        val webView = WebView(activity).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                blockNetworkImage = false
                loadsImagesAutomatically = true
                defaultTextEncodingName = "UTF-8"
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    waitForImagesThenPrint(view, activity, jobName, layout, attempt = 0)
                }
            }
            loadDataWithBaseURL(contentBaseUrl, html, "text/html; charset=UTF-8", "UTF-8", null)
        }
        activeWebViews.add(webView)
        return true
    }

    private fun waitForImagesThenPrint(
        webView: WebView,
        activity: Activity,
        jobName: String,
        layout: PrintPageLayout,
        attempt: Int,
    ) {
        webView.evaluateJavascript(IMAGE_READY_SCRIPT) { raw ->
            val ready = raw?.trim('"') == "ready"
            if (!ready && attempt < IMAGE_READY_MAX_RETRIES) {
                Handler(Looper.getMainLooper()).postDelayed(
                    { waitForImagesThenPrint(webView, activity, jobName, layout, attempt + 1) },
                    IMAGE_READY_DELAY_MS,
                )
                return@evaluateJavascript
            }
            val printManager = activity.getSystemService(PrintManager::class.java) ?: return@evaluateJavascript
            val adapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, adapter, layout.toAttributes())
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    activeWebViews.remove(webView)
                    webView.destroy()
                },
                120_000,
            )
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private const val IMAGE_READY_SCRIPT = """
        (function() {
          var imgs = document.images;
          if (!imgs || !imgs.length) return 'ready';
          for (var i = 0; i < imgs.length; i++) {
            var img = imgs[i];
            if (!img.complete || img.naturalWidth === 0) return 'pending';
          }
          return 'ready';
        })()
    """
}
