package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ChamferingMachineDto
import com.example.smart_emap.data.model.ChamferingManagementRowDto
import com.example.smart_emap.data.model.PatchChamferingBody
import com.example.smart_emap.data.model.ReorderChamferingBody
import com.example.smart_emap.data.model.SplitChamferingToNextDayBody
import com.example.smart_emap.data.model.UserListItemDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

class ChamferingRepository(
    private val apiClient: ApiClient,
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val errorAdapter = moshi.adapter(com.example.smart_emap.data.model.ApiMessageResponse::class.java)

    suspend fun loadMachines(): List<ChamferingMachineDto> {
        val res = apiClient.masterApi().listMachines(keyword = "面取", pageSize = 500)
        val list = res.items()
        return list
            .filter { m ->
                val name = m.machineName.orEmpty()
                name.contains("面取") && !name.contains("外注") && !m.machineCd.orEmpty().contains("外注")
            }
            .map { m ->
                ChamferingMachineDto(id = m.id, machineCd = m.machineCd, machineName = m.machineName)
            }
            .filter { it.id != null && it.id > 0 }
    }

    suspend fun loadPlans(productionDay: String, chamferingMachine: String): List<ChamferingManagementRowDto> {
        val res = apiClient.chamferingApi().list(
            productionDay = productionDay,
            chamferingMachine = chamferingMachine,
            limit = 2000,
        )
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "面取指示の取得に失敗しました")
        }
        return res.data.orEmpty().filter { it.id != null }
    }

    suspend fun loadOperators(): List<UserListItemDto> {
        val res = apiClient.systemUsersApi().listUsers(page = 1, pageSize = 500, status = "active")
        return res.items.orEmpty().filter { it.id != null }
    }

    suspend fun patchPlan(id: Int, body: PatchChamferingBody) {
        try {
            val res = apiClient.chamferingApi().patch(id, body)
            if (res.success == false) {
                throw ChamferingPatchException(400, res.message ?: res.detail ?: "保存に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    suspend fun reorderPlans(chamferingMachine: String, productionDay: String, orderedIds: List<Int>) {
        try {
            val res = apiClient.chamferingApi().reorder(
                ReorderChamferingBody(
                    chamferingMachine = chamferingMachine,
                    productionDay = productionDay,
                    orderedIds = orderedIds,
                ),
            )
            if (res.success == false) {
                throw ChamferingPatchException(400, res.message ?: res.detail ?: "生産順の更新に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    suspend fun splitToNextDay(id: Int, todayQuantity: Int, nextDay: String?) {
        try {
            val res = apiClient.chamferingApi().splitToNextDay(
                id,
                SplitChamferingToNextDayBody(todayQuantity = todayQuantity, nextDay = nextDay),
            )
            if (res.success == false) {
                throw ChamferingPatchException(400, res.message ?: res.detail ?: "順延に失敗しました")
            }
        } catch (e: HttpException) {
            throw mapPatchHttpError(e)
        }
    }

    private fun mapPatchHttpError(e: HttpException): ChamferingPatchException {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.detail?.trim()
                ?: parsed?.message?.trim()
                ?: "保存に失敗しました (${e.code()})"
            return ChamferingPatchException(e.code(), message)
        }
        return ChamferingPatchException(e.code(), "保存に失敗しました (${e.code()})")
    }
}
