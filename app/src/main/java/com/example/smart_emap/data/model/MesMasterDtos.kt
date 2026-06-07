package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class MachineListResponse(
    val success: Boolean? = null,
    val data: MachineListData? = null,
    val list: List<MachineDto>? = null,
)

data class MachineListData(
    val list: List<MachineDto>? = null,
    val total: Int? = null,
)

data class MachineDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    val status: String? = null,
)

data class EquipmentEfficiencyProductDto(
    val id: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "efficiency_rate") val efficiencyRate: Double? = null,
    @Json(name = "step_time") val stepTime: Int? = null,
    @Json(name = "lot_size") val lotSize: Int? = null,
)
