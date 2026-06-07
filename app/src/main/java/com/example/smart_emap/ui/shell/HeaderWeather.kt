package com.example.smart_emap.ui.shell

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class HeaderWeatherInfo(
    val temperature: String,
    val emoji: String,
)

/** 与 Web HeaderBar.vue 一致：名古屋 Open-Meteo 天气 */
object HeaderWeatherFetcher {
    private const val NAGOYA_LAT = 35.1815
    private const val NAGOYA_LON = 136.9066
    const val REFRESH_MS = 15 * 60 * 1000L

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val fallback = HeaderWeatherInfo(temperature = "--", emoji = "🌤️")

    suspend fun fetch(): HeaderWeatherInfo = withContext(Dispatchers.IO) {
        runCatching {
            val url =
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$NAGOYA_LAT&longitude=$NAGOYA_LON" +
                    "&current=temperature_2m,weather_code&timezone=Asia%2FTokyo"
            val request = Request.Builder().url(url).get().build()
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
        }.getOrElse { fallback }
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
}
