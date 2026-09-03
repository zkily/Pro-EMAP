package com.example.smart_emap.ui.shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.roundToInt

data class HeaderWeatherInfo(
    val temperature: String,
    val emoji: String,
)

/**
 * ヘッダー天気。
 * 1) 社内 API プロキシ `/api/system/header-weather`（LAN 向け）
 * 2) 失敗時は Open-Meteo 直叩き（フォールバック）
 */
object HeaderWeatherFetcher {
    private const val NAGOYA_LAT = 35.1815
    private const val NAGOYA_LON = 136.9066
    const val REFRESH_MS = 15 * 60 * 1000L

    private val client: OkHttpClient by lazy { buildClient() }

    private val fallback = HeaderWeatherInfo(temperature = "--", emoji = "🌤️")

    suspend fun fetch(apiBaseUrl: String? = null): HeaderWeatherInfo = withContext(Dispatchers.IO) {
        val base = apiBaseUrl?.trim().orEmpty()
        if (base.isNotEmpty()) {
            fetchFromBackend(base)?.let { return@withContext it }
        }
        fetchFromOpenMeteo() ?: fallback
    }

    private fun fetchFromBackend(apiBaseUrl: String): HeaderWeatherInfo? {
        return runCatching {
            val root = if (apiBaseUrl.endsWith("/")) apiBaseUrl else "$apiBaseUrl/"
            val url = "${root}api/system/header-weather"
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .header("User-Agent", "SmartEMAP-Android/1.0 (header-weather)")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("backend weather http ${response.code}")
                val body = response.body?.string() ?: error("backend weather empty")
                val json = JSONObject(body)
                val temp = json.optString("temperature").trim()
                val emoji = json.optString("emoji").trim()
                if (temp.isEmpty() || temp == "--") error("backend weather invalid")
                HeaderWeatherInfo(
                    temperature = temp,
                    emoji = emoji.ifEmpty { "🌤️" },
                )
            }
        }.getOrNull()
    }

    private fun fetchFromOpenMeteo(): HeaderWeatherInfo? {
        repeat(2) { attempt ->
            val result = runCatching {
                val url =
                    "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$NAGOYA_LAT&longitude=$NAGOYA_LON" +
                        "&current=temperature_2m,weather_code&timezone=Asia%2FTokyo"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .header("User-Agent", "SmartEMAP-Android/1.0 (header-weather)")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("weather http ${response.code}")
                    val body = response.body?.string() ?: error("weather empty body")
                    val current = JSONObject(body).getJSONObject("current")
                    val tempC = current.getDouble("temperature_2m")
                    val code = current.optInt("weather_code", 0)
                    HeaderWeatherInfo(
                        temperature = "${tempC.roundToInt()}°C",
                        emoji = wmoWeatherCodeToEmoji(code),
                    )
                }
            }
            if (result.isSuccess) return result.getOrNull()
            if (attempt == 0) Thread.sleep(400)
        }
        return null
    }

    private fun wmoWeatherCodeToEmoji(code: Int): String = when {
        code == 0 -> "☀️"
        code in 1..3 -> "⛅"
        code in 4..48 -> "🌫️"
        code in 49..57 -> "🌦️"
        code in 58..67 -> "🌧️"
        code in 68..77 -> "❄️"
        code in 78..82 -> "🌧️"
        code in 83..86 -> "❄️"
        code in 87..99 -> "⛈️"
        else -> "🌤️"
    }

    /** 開発用自己署名 HTTPS（社内 API）に合わせ、ApiClient と同様に信頼する */
    private fun buildClient(): OkHttpClient {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
}
