package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.CuttingManagementRowDto
import com.example.smart_emap.data.model.CuttingPlanningMachineDto
import com.example.smart_emap.data.model.PatchCuttingBody
import com.example.smart_emap.data.model.ReorderCuttingBody
import com.example.smart_emap.data.model.SplitCuttingToNextDayBody
import com.example.smart_emap.data.model.UserListItemDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

class CuttingRepository(
    private val apiClient: ApiClient,
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val errorAdapter = moshi.adapter(com.example.smart_emap.data.model.ApiMessageResponse::class.java)

    suspend fun loadMachines(): List<CuttingPlanningMachineDto> {
        return apiClient.cuttingPlanningApi().machines()
            .filter { m ->
                val name = m.machineName.orEmpty()
                val cd = m.machineCd.orEmpty()
                !name.contains("外注") && !cd.contains("外注")
            }
            .filter { it.id != null && it.id > 0 }
    }

    suspend fun loadPlans(productionDay: String, cuttingMachine: String): List<CuttingManagementRowDto> {
        val res = apiClient.cuttingApi().list(
            productionDay = productionDay,
            cuttingMachine = cuttingMachine,
            limit = 2000,
        )
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "切断指示の取得に失敗しました")
        }
        return res.data.orEmpty().filter { it.id != null }
    }

    suspend fun loadOperators(): List<UserListItemDto> {
        val res = apiClient.systemUsersApi().listUsers(page = 1, pageSize = 500, status = "active")
        return res.items.orEmpty().filter { it.id != null }
    }

    suspend fun patchPlan(id: Int, body: PatchCuttingBody) {
        try {
            val res = apiClient.cuttingApi().patch(id, body)
            if (res.success == false) {
                throw CuttingPatchException(400, res.message ?: res.detail ?: "保存に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    suspend fun reorderPlans(cuttingMachine: String, orderedIds: List<Int>) {
        try {
            val res = apiClient.cuttingApi().reorder(
                ReorderCuttingBody(cuttingMachine = cuttingMachine, orderedIds = orderedIds),
            )
            if (res.success == false) {
                throw CuttingPatchException(400, res.message ?: res.detail ?: "生産順の更新に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    suspend fun splitToNextDay(id: Int, todayQuantity: Int, nextDay: String?) {
        try {
            val res = apiClient.cuttingApi().splitToNextDay(
                id,
                SplitCuttingToNextDayBody(todayQuantity = todayQuantity, nextDay = nextDay),
            )
            if (res.success == false) {
                throw CuttingPatchException(400, res.message ?: res.detail ?: "順延に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    private fun mapPatchHttpError(e: HttpException): CuttingPatchException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "保存に失敗しました (${e.code()})"
            return CuttingPatchException(e.code(), message)
        }
        return CuttingPatchException(e.code(), "保存に失敗しました (${e.code()})")
    }
}
