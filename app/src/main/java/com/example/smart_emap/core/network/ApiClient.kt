package com.example.smart_emap.core.network

import com.example.smart_emap.BuildConfig
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.data.api.AuthApiService
import com.example.smart_emap.data.api.DashboardApiService
import com.example.smart_emap.data.api.ErpOptionsApiService
import com.example.smart_emap.data.api.InspectionApiService
import com.example.smart_emap.data.api.ProcessDefectApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

class ApiClient(
    private val sessionStore: SessionStore,
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var cachedBaseUrl: String? = null
    private var cachedRetrofit: Retrofit? = null

    suspend fun retrofit(): Retrofit {
        val baseUrl = sessionStore.getApiBaseUrl(ApiDefaults.displayBaseUrl)
        if (cachedRetrofit != null && cachedBaseUrl == baseUrl) {
            return cachedRetrofit!!
        }
        cachedBaseUrl = baseUrl
        cachedRetrofit = createRetrofit(baseUrl)
        return cachedRetrofit!!
    }

    suspend fun authApi(): AuthApiService = retrofit().create(AuthApiService::class.java)

    suspend fun dashboardApi(): DashboardApiService = retrofit().create(DashboardApiService::class.java)

    suspend fun inspectionApi(): InspectionApiService = retrofit().create(InspectionApiService::class.java)

    suspend fun erpOptionsApi(): ErpOptionsApiService = retrofit().create(ErpOptionsApiService::class.java)

    suspend fun processDefectApi(): ProcessDefectApiService = retrofit().create(ProcessDefectApiService::class.java)

    fun invalidate() {
        cachedBaseUrl = null
        cachedRetrofit = null
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))

        // 仅开发环境放开证书校验，避免自签名/私有 CA 造成的 SSLHandshakeException
        // 生产环境请改用正确受信任证书，或通过 network_security_config 配置 trust-anchors。
        if (BuildConfig.DEBUG) {
            // HTTP/1.1：避免经 Vite/自签名 HTTPS 代理时出现 BAD_DECRYPT
            clientBuilder.protocols(listOf(Protocol.HTTP_1_1))
            val trustManager = trustAllX509TrustManager()
            val sslSocketFactory = createSslSocketFactory(trustManager)
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
            clientBuilder.hostnameVerifier { _, _ -> true }
        }

        val client = clientBuilder
            .addInterceptor(AuthInterceptor(sessionStore))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private fun trustAllX509TrustManager(): X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private fun createSslSocketFactory(trustManager: X509TrustManager) = run {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        sslContext.socketFactory
    }
}
