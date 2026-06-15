package com.example.smart_emap.data.repository

import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.CreateInspectionBody
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.model.PatchInspectionBody
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.ProductProcessBomRowDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.mes.inspectionregistration.InspectionManualRegistrationLogic
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityLogic
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

const val INSPECTION_DEFECT_DETECTION_PROCESS_CD = "KT09"

private val INSPECTION_PRODUCT_NAME_EXCLUDES = listOf("加工", "アーチ")

class InspectionRepository(
    private val apiClient: ApiClient,
    private val mesClientIdStore: MesClientIdStore,
    private val systemUserRepository: SystemUserRepository? = null,
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(com.example.smart_emap.data.model.ApiMessageResponse::class.java)

    suspend fun getClientInstanceId(): String = mesClientIdStore.getClientInstanceId()

    suspend fun loadProducts(): List<ErpProductDto> {
        val list = apiClient.masterApi().listProducts(pageSize = 9999, status = "active").items()
        return list
            .map { p ->
                ErpProductDto(
                    id = p.id,
                    productCode = p.productCd?.trim().orEmpty(),
                    productName = p.productName?.trim().orEmpty(),
                    isActive = (p.status ?: "").equals("active", ignoreCase = true),
                    unitPerBox = (p.unitPerBox ?: 0).coerceAtLeast(0),
                )
            }
            .filter { p ->
                if (p.isActive != true) return@filter false
                val code = p.productCode
                if (code.isEmpty() || !code.endsWith("1")) return@filter false
                INSPECTION_PRODUCT_NAME_EXCLUDES.none { p.productName.contains(it) }
            }
            .sortedBy { it.productName }
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
        manualRegistrationNote: String? = null,
        manualRegistration: Boolean = false,
    ): Int {
        val res = apiClient.inspectionApi().create(
            CreateInspectionBody(
                productionDay = productionDay,
                productCd = productCd,
                productName = productName,
                mesInspectorUserId = inspectorUserId,
                manualRegistrationNote = manualRegistrationNote,
                manualRegistration = if (manualRegistration) true else null,
            ),
        )
        val id = res.data?.id ?: throw IllegalStateException(res.message ?: "作成に失敗しました")
        return id
    }

    suspend fun deletePlan(id: Int) {
        val res = apiClient.inspectionApi().delete(id)
        if (res.success == false) {
            throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")
        }
    }

    suspend fun loadShiageSectionInspectors(): List<UserListItemDto> {
        val repo = systemUserRepository ?: return emptyList()
        val sectionId = repo.getOrganizations().getOrNull()
            ?.firstOrNull { it.type == "section" && it.name == InspectionManualRegistrationLogic.SHIAGE_SECTION_NAME }
            ?.id
        val users = repo.getUsers(
            status = "active",
            sectionId = sectionId,
            page = 1,
            pageSize = 500,
        ).getOrNull()?.items.orEmpty()
        return users.filter { user ->
            user.id != null &&
                (user.section?.trim() == InspectionManualRegistrationLogic.SHIAGE_SECTION_NAME)
        }.sortedBy { it.displayLabel() }
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

    suspend fun loadWeldingProductCdSet(): Set<String> {
        return try {
            fetchWeldingProductCdSetLightweight()
        } catch (e: HttpException) {
            if (e.code() == 404) {
                InspectionProductivityLogic.buildWeldingProductCdSet(fetchAllProductProcessBomRows())
            } else {
                throw e
            }
        }
    }

    private suspend fun fetchWeldingProductCdSetLightweight(): Set<String> {
        val cds = withIoRetry {
            apiClient.masterApiLong().listProductProcessBomWeldingProducts().productCds()
        }
        return InspectionProductivityLogic.buildWeldingProductCdSetFromProductCds(cds)
    }

    private suspend fun fetchAllProductProcessBomRows(): List<ProductProcessBomRowDto> {
        val all = mutableListOf<ProductProcessBomRowDto>()
        var page = 1
        val limit = 100
        val maxPages = 30
        while (page <= maxPages) {
            val list = withIoRetry {
                apiClient.masterApiLong().listProductProcessBom(page = page, limit = limit).items()
            }
            if (list.isEmpty()) break
            all.addAll(list)
            if (list.size < limit) break
            page += 1
        }
        return all
    }

    private suspend fun <T> withIoRetry(times: Int = 3, block: suspend () -> T): T {
        var last: IOException? = null
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                last = e
                if (attempt < times - 1) {
                    delay(400L * (attempt + 1))
                }
            }
        }
        throw last ?: IOException("ネットワークエラー")
    }

    suspend fun loadProductivityAnalysis(
        startDate: String,
        endDate: String,
        inspectorUserId: Int? = null,
        productCd: String? = null,
        includeIncomplete: Boolean = false,
    ): Result<InspectionProductivityAnalysisDataDto> = runCatching {
        val res = try {
            fetchProductivityAnalysisWithRetry(
                startDate = startDate,
                endDate = endDate,
                inspectorUserId = inspectorUserId,
                productCd = productCd,
                includeIncomplete = includeIncomplete,
            )
        } catch (e: HttpException) {
            throw mapProductivityHttpError(e)
        } catch (e: JsonDataException) {
            throw mapProductivityJsonError(e)
        } catch (e: JsonEncodingException) {
            throw mapProductivityJsonError(e)
        } catch (e: IOException) {
            throw IllegalStateException(e.message ?: "ネットワークエラー")
        }
        if (res.success == false || res.data == null) {
            throw IllegalStateException(res.message ?: "分析データの取得に失敗しました")
        }
        res.data
    }

    private suspend fun fetchProductivityAnalysisWithRetry(
        startDate: String,
        endDate: String,
        inspectorUserId: Int?,
        productCd: String?,
        includeIncomplete: Boolean,
    ): com.example.smart_emap.data.model.InspectionProductivityAnalysisResponse {
        var lastJson: Exception? = null
        repeat(2) { attempt ->
            try {
                return withIoRetry {
                    apiClient.inspectionApiLong().productivityAnalysis(
                        startDate = startDate,
                        endDate = endDate,
                        mesInspectorUserId = inspectorUserId,
                        productCd = productCd?.trim()?.ifBlank { null },
                        includeIncomplete = if (includeIncomplete) true else null,
                        limit = 5000,
                    )
                }
            } catch (e: JsonDataException) {
                lastJson = e
                if (attempt == 0) delay(600)
            } catch (e: JsonEncodingException) {
                lastJson = e
                if (attempt == 0) delay(600)
            }
        }
        throw lastJson ?: JsonDataException("分析データの解析に失敗しました")
    }

    private fun mapProductivityJsonError(e: Exception): IllegalStateException {
        val detail = e.message?.trim().orEmpty()
        val hint = if (detail.contains("Expected", ignoreCase = true)) {
            " 响应体可能在传输中被截断，请重启后端后重试。"
        } else {
            ""
        }
        return IllegalStateException("分析データの解析に失敗しました: $detail$hint")
    }

    private fun mapProductivityHttpError(e: HttpException): IllegalStateException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "分析データの取得に失敗しました (${e.code()})"
            return IllegalStateException(message)
        }
        return IllegalStateException("分析データの取得に失敗しました (${e.code()})")
    }

    suspend fun loadUtilizationAnalysis(
        startDate: String,
        endDate: String,
        inspectorUserId: Int? = null,
        includeIncomplete: Boolean = false,
        extraWorkdays: List<String> = emptyList(),
        extraHolidays: List<String> = emptyList(),
    ): Result<InspectionUtilizationAnalysisDataDto> = runCatching {
        val res = try {
            apiClient.inspectionApiLong().utilizationAnalysis(
                startDate = startDate,
                endDate = endDate,
                mesInspectorUserId = inspectorUserId,
                includeIncomplete = if (includeIncomplete) true else null,
                extraWorkdays = extraWorkdays.joinToString(",").ifBlank { null },
                extraHolidays = extraHolidays.joinToString(",").ifBlank { null },
                useCompanyCalendar = true,
                limit = 5000,
            )
        } catch (e: HttpException) {
            throw mapUtilizationHttpError(e)
        } catch (e: JsonDataException) {
            throw IllegalStateException("分析データの解析に失敗しました: ${e.message}")
        } catch (e: JsonEncodingException) {
            throw IllegalStateException("分析データの解析に失敗しました: ${e.message}")
        } catch (e: IOException) {
            throw IllegalStateException(e.message ?: "ネットワークエラー")
        }
        if (res.success == false || res.data == null) {
            throw IllegalStateException(res.message ?: "分析データの取得に失敗しました")
        }
        res.data
    }

    private fun mapUtilizationHttpError(e: HttpException): IllegalStateException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "分析データの取得に失敗しました (${e.code()})"
            return IllegalStateException(message)
        }
        return IllegalStateException("分析データの取得に失敗しました (${e.code()})")
    }
}
