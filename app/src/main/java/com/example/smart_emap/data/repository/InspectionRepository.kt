package com.example.smart_emap.data.repository

import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.CreateInspectionBody
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.ErpProductsEnvelope
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.model.PatchInspectionBody
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

const val INSPECTION_DEFECT_DETECTION_PROCESS_CD = "KT09"

private val INSPECTION_PRODUCT_NAME_EXCLUDES = listOf("加工", "アーチ")

class InspectionRepository(
    private val apiClient: ApiClient,
    private val mesClientIdStore: MesClientIdStore,
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(com.example.smart_emap.data.model.ApiMessageResponse::class.java)

    private val productsListAdapter = moshi.adapter<List<ErpProductDto>>(
        Types.newParameterizedType(List::class.java, ErpProductDto::class.java),
    )

    private val productsEnvelopeAdapter = moshi.adapter(ErpProductsEnvelope::class.java)

    suspend fun getClientInstanceId(): String = mesClientIdStore.getClientInstanceId()

    suspend fun loadProducts(): List<ErpProductDto> {
        val body = apiClient.erpOptionsApi().getProductsRaw().string()
        val list = parseProductsJson(body)
        return list
            .map { p ->
                ErpProductDto(
                    id = p.id,
                    productCode = p.normalizedCode(),
                    productName = p.normalizedName(),
                    isActive = p.isActive,
                )
            }
            .filter { p ->
                if (p.isActive == false) return@filter false
                val code = p.productCode
                if (code.isEmpty() || !code.endsWith("1")) return@filter false
                INSPECTION_PRODUCT_NAME_EXCLUDES.none { p.productName.contains(it) }
            }
            .sortedBy { it.productName }
    }

    /** Web getProducts と同様：配列または { data: [...] } の両方に対応 */
    private fun parseProductsJson(body: String): List<ErpProductDto> {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed == "null") return emptyList()
        if (trimmed.startsWith("[")) {
            return productsListAdapter.fromJson(trimmed).orEmpty()
        }
        return productsEnvelopeAdapter.fromJson(trimmed)?.data.orEmpty()
    }

    suspend fun loadDefectItems(): List<ProcessDefectItemDto> {
        val res = apiClient.processDefectApi().getOptions(INSPECTION_DEFECT_DETECTION_PROCESS_CD)
        return res.data.orEmpty()
    }

    suspend fun loadPlans(productionDay: String): List<InspectionManagementRowDto> {
        val res = apiClient.inspectionApi().list(productionDay = productionDay, limit = 2000)
        return res.data.orEmpty().filter { it.id != null }
    }

    suspend fun createPlan(
        productionDay: String,
        productCd: String,
        productName: String,
        inspectorUserId: Int,
    ): Int {
        val res = apiClient.inspectionApi().create(
            CreateInspectionBody(
                productionDay = productionDay,
                productCd = productCd,
                productName = productName,
                mesInspectorUserId = inspectorUserId,
            ),
        )
        val id = res.data?.id ?: throw IllegalStateException(res.message ?: "作成に失敗しました")
        return id
    }

    suspend fun patchPlan(id: Int, body: PatchInspectionBody) {
        try {
            val clientId = mesClientIdStore.getClientInstanceId()
            val res = apiClient.inspectionApi().patch(
                id,
                body.copy(mesClientInstanceId = body.mesClientInstanceId ?: clientId),
            )
            if (res.success == false) {
                throw InspectionPatchException(400, res.message ?: res.detail ?: "保存に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    private fun mapPatchHttpError(e: HttpException): InspectionPatchException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "保存に失敗しました (${e.code()})"
            return InspectionPatchException(e.code(), message)
        }
        return InspectionPatchException(e.code(), "保存に失敗しました (${e.code()})")
    }

    suspend fun loadProductivityAnalysis(
        startDate: String,
        endDate: String,
        inspectorUserId: Int? = null,
        productCd: String? = null,
        includeIncomplete: Boolean = false,
    ): Result<InspectionProductivityAnalysisDataDto> = runCatching {
        val res = apiClient.inspectionApi().productivityAnalysis(
            startDate = startDate,
            endDate = endDate,
            mesInspectorUserId = inspectorUserId,
            productCd = productCd?.trim()?.ifBlank { null },
            includeIncomplete = if (includeIncomplete) true else null,
        )
        if (res.success == false || res.data == null) {
            throw IllegalStateException(res.message ?: "分析データの取得に失敗しました")
        }
        res.data
    }

    suspend fun loadUtilizationAnalysis(
        startDate: String,
        endDate: String,
        inspectorUserId: Int? = null,
        includeIncomplete: Boolean = false,
        extraWorkdays: List<String> = emptyList(),
        extraHolidays: List<String> = emptyList(),
    ): Result<InspectionUtilizationAnalysisDataDto> = runCatching {
        val res = apiClient.inspectionApi().utilizationAnalysis(
            startDate = startDate,
            endDate = endDate,
            mesInspectorUserId = inspectorUserId,
            includeIncomplete = if (includeIncomplete) true else null,
            extraWorkdays = extraWorkdays.joinToString(",").ifBlank { null },
            extraHolidays = extraHolidays.joinToString(",").ifBlank { null },
            useCompanyCalendar = true,
        )
        if (res.success == false || res.data == null) {
            throw IllegalStateException(res.message ?: "分析データの取得に失敗しました")
        }
        res.data
    }
}
