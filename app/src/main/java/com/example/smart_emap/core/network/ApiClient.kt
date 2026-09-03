package com.example.smart_emap.core.network

import com.example.smart_emap.BuildConfig
import com.example.smart_emap.core.auth.SessionEvents
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.data.api.AuthApiService
import com.example.smart_emap.data.api.ApsApiService
import com.example.smart_emap.data.api.ChamferingApiService
import com.example.smart_emap.data.api.CuttingApiService
import com.example.smart_emap.data.api.CuttingInstructionApiService
import com.example.smart_emap.data.api.DatabaseApiService
import com.example.smart_emap.data.api.CuttingPlanningApiService
import com.example.smart_emap.data.api.PlanBaselineApiService
import com.example.smart_emap.data.api.PlanDataApiService
import com.example.smart_emap.data.api.ProductionRequirementsApiService
import com.example.smart_emap.data.api.ProductionSummaryApiService
import com.example.smart_emap.data.api.InventoryApiService
import com.example.smart_emap.data.api.StockTransactionLogApiService
import com.example.smart_emap.data.api.DashboardApiService
import com.example.smart_emap.data.api.ErpOptionsApiService
import com.example.smart_emap.data.api.InspectionApiService
import com.example.smart_emap.data.api.MaterialApiService
import com.example.smart_emap.data.api.MasterApiService
import com.example.smart_emap.data.api.PartApiService
import com.example.smart_emap.data.api.PlanInstructionApiService
import com.example.smart_emap.data.api.OrderBatchApiService
import com.example.smart_emap.data.api.OrderDailyApiService
import com.example.smart_emap.data.api.OrderMonthlyApiService
import com.example.smart_emap.data.api.OutsourcingApiService
import com.example.smart_emap.data.api.ProcessDefectApiService
import com.example.smart_emap.data.api.ProductionActualLogsApiService
import com.example.smart_emap.data.api.ShippingApiService
import com.example.smart_emap.data.api.ShortcutsApiService
import com.example.smart_emap.data.api.SystemApiService
import com.example.smart_emap.data.api.SystemUsersApiService
import com.example.smart_emap.data.api.TodosApiService
import com.example.smart_emap.data.api.WeldingApiService
import com.example.smart_emap.data.model.FlexibleIntAdapterFactory
import com.example.smart_emap.data.model.MesDefectByItemAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

class ApiClient(
    private val sessionStore: SessionStore,
    private val sessionEvents: SessionEvents,
) {
    val moshi = Moshi.Builder()
        .add(MesDefectByItemAdapterFactory)
        .add(FlexibleIntAdapterFactory)
        .add(object : JsonAdapter.Factory {
            override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
                return moshi.nextAdapter<Any>(this, type, annotations).lenient()
            }
        })
        .add(KotlinJsonAdapterFactory())
        .build()

    private val commonOkHttpClient: OkHttpClient by lazy {
        createOkHttpClient(longTimeout = false)
    }

    private val longTimeoutOkHttpClient: OkHttpClient by lazy {
        createOkHttpClient(longTimeout = true)
    }

    private var cachedBaseUrl: String? = null
    private var cachedRetrofit: Retrofit? = null
    private var cachedLongRetrofit: Retrofit? = null

    suspend fun retrofit(): Retrofit {
        val baseUrl = sessionStore.getApiBaseUrl(ApiDefaults.displayBaseUrl)
        if (cachedRetrofit != null && cachedBaseUrl == baseUrl) {
            return cachedRetrofit!!
        }
        cachedBaseUrl = baseUrl
        cachedRetrofit = createRetrofit(baseUrl, commonOkHttpClient)
        cachedLongRetrofit = createRetrofit(baseUrl, longTimeoutOkHttpClient)
        return cachedRetrofit!!
    }

    suspend fun <T> createService(serviceClass: Class<T>, longTimeout: Boolean = false): T {
        val retrofit = if (longTimeout) {
            val baseUrl = sessionStore.getApiBaseUrl(ApiDefaults.displayBaseUrl)
            if (cachedLongRetrofit == null || cachedBaseUrl != baseUrl) {
                retrofit()
            }
            cachedLongRetrofit!!
        } else {
            retrofit()
        }
        return retrofit.create(serviceClass)
    }

    suspend fun authApi(): AuthApiService = createService(AuthApiService::class.java)

    /** 登录时使用文本框传入的地址，避免与已缓存 Retrofit 或本地旧地址不一致。 */
    fun authApiForBaseUrl(baseUrl: String): AuthApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(commonOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApiService::class.java)
    }

    suspend fun dashboardApi(): DashboardApiService = createService(DashboardApiService::class.java)

    suspend fun inspectionApi(): InspectionApiService = createService(InspectionApiService::class.java)

    suspend fun inspectionApiLong(): InspectionApiService =
        createService(InspectionApiService::class.java, longTimeout = true)

    suspend fun cuttingApi(): CuttingApiService = createService(CuttingApiService::class.java)

    suspend fun chamferingApi(): ChamferingApiService = createService(ChamferingApiService::class.java)

    suspend fun cuttingInstructionApi(): CuttingInstructionApiService =
        createService(CuttingInstructionApiService::class.java)

    suspend fun databaseApi(): DatabaseApiService = createService(DatabaseApiService::class.java)

    suspend fun databaseApiLong(): DatabaseApiService =
        createService(DatabaseApiService::class.java, longTimeout = true)

    suspend fun cuttingPlanningApi(): CuttingPlanningApiService =
        createService(CuttingPlanningApiService::class.java)

    suspend fun systemApi(): SystemApiService = createService(SystemApiService::class.java)

    suspend fun systemUsersApi(): SystemUsersApiService = systemApi()

    suspend fun weldingApi(): WeldingApiService = createService(WeldingApiService::class.java)

    suspend fun masterApi(): MasterApiService = createService(MasterApiService::class.java)

    suspend fun masterApiLong(): MasterApiService =
        createService(MasterApiService::class.java, longTimeout = true)

    suspend fun apsApi(): ApsApiService = createService(ApsApiService::class.java)

    suspend fun apsApiLong(): ApsApiService = createService(ApsApiService::class.java, longTimeout = true)

    suspend fun erpOptionsApi(): ErpOptionsApiService = createService(ErpOptionsApiService::class.java)

    suspend fun processDefectApi(): ProcessDefectApiService =
        createService(ProcessDefectApiService::class.java)

    suspend fun orderMonthlyApi(): OrderMonthlyApiService = createService(OrderMonthlyApiService::class.java)

    suspend fun orderBatchApi(): OrderBatchApiService =
        createService(OrderBatchApiService::class.java, longTimeout = true)

    suspend fun orderDailyApi(): OrderDailyApiService = createService(OrderDailyApiService::class.java)

    suspend fun outsourcingApi(): OutsourcingApiService = createService(OutsourcingApiService::class.java)

    suspend fun materialApi(): MaterialApiService = createService(MaterialApiService::class.java)

    suspend fun materialApiLong(): MaterialApiService =
        createService(MaterialApiService::class.java, longTimeout = true)

    suspend fun partApi(): PartApiService = createService(PartApiService::class.java)

    suspend fun partApiLong(): PartApiService = createService(PartApiService::class.java, longTimeout = true)

    suspend fun planInstructionApi(): PlanInstructionApiService =
        createService(PlanInstructionApiService::class.java)

    suspend fun productionRequirementsApi(): ProductionRequirementsApiService =
        createService(ProductionRequirementsApiService::class.java, longTimeout = true)

    suspend fun productionActualLogsApi(): ProductionActualLogsApiService =
        createService(ProductionActualLogsApiService::class.java, longTimeout = true)

    suspend fun productionSummaryApi(): ProductionSummaryApiService =
        createService(ProductionSummaryApiService::class.java)

    suspend fun productionSummaryApiLong(): ProductionSummaryApiService =
        createService(ProductionSummaryApiService::class.java, longTimeout = true)

    suspend fun stockTransactionLogApi(): StockTransactionLogApiService =
        createService(StockTransactionLogApiService::class.java)

    suspend fun stockTransactionLogApiLong(): StockTransactionLogApiService =
        createService(StockTransactionLogApiService::class.java, longTimeout = true)

    suspend fun inventoryApi(): InventoryApiService = createService(InventoryApiService::class.java)

    suspend fun inventoryApiLong(): InventoryApiService =
        createService(InventoryApiService::class.java, longTimeout = true)

    suspend fun planBaselineApi(): PlanBaselineApiService = createService(PlanBaselineApiService::class.java)

    suspend fun planDataApi(): PlanDataApiService = createService(PlanDataApiService::class.java)

    suspend fun shortcutsApi(): ShortcutsApiService = createService(ShortcutsApiService::class.java)

    suspend fun todosApi(): TodosApiService = createService(TodosApiService::class.java)

    suspend fun shippingApi(): ShippingApiService = createService(ShippingApiService::class.java)

    suspend fun shippingApiLong(): ShippingApiService =
        createService(ShippingApiService::class.java, longTimeout = true)

    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private fun createOkHttpClient(longTimeout: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val timeout = if (longTimeout) 120L else 60L

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(NgrokInterceptor)
            .addInterceptor(AuthInterceptor(sessionStore))
            .addInterceptor(UnauthorizedInterceptor(sessionEvents))
            .addInterceptor(logging)
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))

        if (BuildConfig.DEBUG) {
            clientBuilder.protocols(listOf(Protocol.HTTP_1_1))
            val trustManager = trustAllX509TrustManager()
            val sslSocketFactory = createSslSocketFactory(trustManager)
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
            clientBuilder.hostnameVerifier { _, _ -> true }
        }

        return clientBuilder.build()
    }

    fun invalidate() {
        cachedBaseUrl = null
        cachedRetrofit = null
        cachedLongRetrofit = null
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
