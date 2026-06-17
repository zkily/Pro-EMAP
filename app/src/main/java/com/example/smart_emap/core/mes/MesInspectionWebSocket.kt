package com.example.smart_emap.core.mes

import android.util.Log
import com.example.smart_emap.core.auth.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** 検査 MES 実績の WebSocket 推送（mes_inspection_state） */
class MesInspectionWebSocket(
    private val sessionStore: SessionStore,
    private val defaultApiBaseUrl: String,
    private val scope: CoroutineScope,
    private val onInspectionState: (productionDay: String, inspectionId: Int) -> Unit,
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    private var webSocket: WebSocket? = null
    @Volatile
    private var active = false

    fun start() {
        if (active) return
        active = true
        scope.launch(Dispatchers.IO) { connect() }
    }

    fun stop() {
        active = false
        webSocket?.close(1000, null)
        webSocket = null
    }

    private suspend fun connect() {
        if (!active) return
        val token = sessionStore.getToken()?.trim().orEmpty()
        if (token.isEmpty()) return
        val base = sessionStore.getApiBaseUrl(defaultApiBaseUrl).trimEnd('/')
        val wsBase = base.replace("https://", "wss://").replace("http://", "ws://")
        val url = "$wsBase/ws?token=${URLEncoder.encode(token, Charsets.UTF_8.name())}"
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    runCatching {
                        val json = JSONObject(text)
                        if (json.optString("type") != "mes_inspection_state") return
                        val day = json.optString("production_day").trim()
                        val id = json.optInt("inspection_id")
                        if (day.isNotEmpty() && id > 0) onInspectionState(day, id)
                    }.onFailure { Log.w(TAG, "parse message failed", it) }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(TAG, "connection failed", t)
                    scheduleReconnect()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (active && code != 1000) scheduleReconnect()
                }
            },
        )
    }

    private fun scheduleReconnect() {
        if (!active) return
        scope.launch(Dispatchers.IO) {
            delay(RECONNECT_MS)
            if (active) connect()
        }
    }

    companion object {
        private const val TAG = "MesInspectionWS"
        private const val RECONNECT_MS = 3_000L
    }
}
