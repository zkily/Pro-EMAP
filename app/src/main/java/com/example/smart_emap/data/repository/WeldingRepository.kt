package com.example.smart_emap.data.repository

import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.CreateWeldingBody
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.MachineDto
import com.example.smart_emap.data.model.PatchWeldingBody
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.WeldingManagementRowDto
import com.example.smart_emap.data.model.WeldingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.FlexibleIntAdapterFactory
import com.example.smart_emap.data.model.MesDefectByItemAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

const val WELDING_DEFECT_DETECTION_PROCESS_CD = "KT07"

/** Web weldingMesEquipment.WELDING_MES_MACHINE_NAME_RE と同一（溶接 + 2 文字） */
private val WELDING_MES_MACHINE_NAME_RE = Regex("^溶接.{2}$")

class WeldingRepository(
    private val apiClient: ApiClient,
    private val mesClientIdStore: MesClientIdStore,
) {
    private val moshi = Moshi.Builder()
        .add(MesDefectByItemAdapterFactory)
        .add(FlexibleIntAdapterFactory)
        .add(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(com.example.smart_emap.data.model.ApiMessageResponse::class.java)

    suspend fun getClientInstanceId(): String = mesClientIdStore.getWeldingClientInstanceId()

    /** 溶接設備一覧（名称が「溶接」+2 文字） */
    suspend fun loadWeldingMesMachines(): List<MachineDto> = loadWeldingMesMachinesInternal()

    /** 選択設備に紐づく製品（Web fetchWeldingMesProducts） */
    suspend fun loadProductsForMachine(machineId: Int): List<ErpProductDto> {
        val rows = apiClient.apsApi().getEquipmentEfficiencyProducts(machineId)
        val seen = linkedSetOf<String>()
        val products = mutableListOf<ErpProductDto>()
        for (row in rows) {
            val code = row.productCd?.trim().orEmpty()
            if (code.isEmpty() || !seen.add(code)) continue
            val name = row.productName?.trim().orEmpty().ifEmpty { code }
            products.add(
                ErpProductDto(
                    id = row.id,
                    productCode = code,
                    productName = name,
                    isActive = true,
                    unitPerBox = 0,
                ),
            )
        }
        return products.sortedBy { it.productName }
    }

    /**
     * 全溶接設備の製品をマージ（後方互換・モニタ等）
     */
    suspend fun loadProducts(): List<ErpProductDto> {
        val machines = loadWeldingMesMachinesInternal()
        if (machines.isEmpty()) return emptyList()

        val seen = linkedSetOf<String>()
        val products = mutableListOf<ErpProductDto>()
        for (machine in machines) {
            val machineId = machine.id ?: continue
            for (p in loadProductsForMachine(machineId)) {
                if (seen.add(p.productCode)) products.add(p)
            }
        }
        return products.sortedBy { it.productName }
    }

    private suspend fun loadWeldingMesMachinesInternal(): List<MachineDto> {
        val res = apiClient.masterApi().listMachines(keyword = "溶接", pageSize = 500)
        val list = res.items().map { MachineDto(id = it.id, machineCd = it.machineCd, machineName = it.machineName, status = it.status) }
        return list
            .mapNotNull { row ->
                val id = row.id ?: return@mapNotNull null
                if (id <= 0) return@mapNotNull null
                if (pickWeldingMesMachineLabel(row) == null) return@mapNotNull null
                row
            }
            .sortedBy { pickWeldingMesMachineLabel(it).orEmpty() }
    }

    fun pickWeldingMesMachineLabel(row: MachineDto): String? {
        val name = row.machineName?.trim().orEmpty()
        if (WELDING_MES_MACHINE_NAME_RE.matches(name)) return name
        val cd = row.machineCd?.trim().orEmpty()
        if (WELDING_MES_MACHINE_NAME_RE.matches(cd)) return cd
        return null
    }

    suspend fun loadDefectItems(): List<ProcessDefectItemDto> {
        val res = apiClient.processDefectApi().getOptions(WELDING_DEFECT_DETECTION_PROCESS_CD)
        return res.data.orEmpty().filter { !it.defectCd.isNullOrBlank() }
    }

    suspend fun loadProductivityAnalysis(
        startDate: String,
        endDate: String,
        operatorUserId: Int? = null,
        productCd: String? = null,
        includeIncomplete: Boolean = false,
    ): Result<WeldingProductivityAnalysisDataDto> = runCatching {
        val res = apiClient.weldingApi().productivityAnalysis(
            startDate = startDate,
            endDate = endDate,
            mesOperatorUserId = operatorUserId,
            productCd = productCd?.trim()?.ifBlank { null },
            includeIncomplete = if (includeIncomplete) true else null,
        )
        if (res.success == false || res.data == null) {
            throw IllegalStateException(res.message ?: "分析データの取得に失敗しました")
        }
        res.data
    }

    suspend fun loadPlans(
        productionDay: String,
        weldingMachine: String? = null,
    ): List<WeldingManagementRowDto> {
        val res = apiClient.weldingApi().list(
            productionDay = productionDay,
            weldingMachine = weldingMachine?.trim()?.ifBlank { null },
            limit = 2000,
        )
        return res.data.orEmpty().filter { it.id != null }
    }

    /** 溶接モニタ：設備別 list をマージして当日行を取得 */
    suspend fun loadMonitorPlans(productionDay: String): List<WeldingManagementRowDto> {
        val merged = linkedMapOf<Int, WeldingManagementRowDto>()
        loadPlans(productionDay).forEach { row -> row.id?.let { merged[it] = row } }
        for (machine in loadWeldingMesMachinesInternal()) {
            val label = pickWeldingMesMachineLabel(machine) ?: continue
            loadPlans(productionDay, label).forEach { row -> row.id?.let { merged[it] = row } }
        }
        return merged.values.toList()
    }

    suspend fun createPlan(
        productionDay: String,
        productCd: String,
        productName: String,
        operatorUserId: Int,
        weldingMachine: String? = null,
    ): Int {
        val res = apiClient.weldingApi().create(
            CreateWeldingBody(
                productionDay = productionDay,
                productCd = productCd,
                productName = productName,
                weldingMachine = weldingMachine?.trim()?.ifBlank { null },
                mesOperatorUserId = operatorUserId,
            ),
        )
        val id = res.data?.id ?: throw IllegalStateException(res.message ?: "作成に失敗しました")
        return id
    }

    suspend fun patchPlan(id: Int, body: PatchWeldingBody) {
        try {
            val clientId = mesClientIdStore.getWeldingClientInstanceId()
            val res = apiClient.weldingApi().patch(
                id,
                body.copy(mesClientInstanceId = body.mesClientInstanceId ?: clientId),
            )
            if (res.success == false) {
                throw WeldingPatchException(400, res.message ?: res.detail ?: "保存に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    private fun mapPatchHttpError(e: HttpException): WeldingPatchException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "保存に失敗しました (${e.code()})"
            return WeldingPatchException(e.code(), message)
        }
        return WeldingPatchException(e.code(), "保存に失敗しました (${e.code()})")
    }
}
