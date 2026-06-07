package com.example.smart_emap.data.model

data class ApiEnvelope<T>(
    val success: Boolean? = null,
    val data: T? = null,
    val message: String? = null,
)

data class PagedListDto<T>(
    val list: List<T> = emptyList(),
    val total: Int = 0,
)

data class StockActionResultDto(
    val calculated_count: Int? = null,
    val updated_count: Int? = null,
    val usage_synced: Int? = null,
)

data class SyncMasterResultDto(
    val updated_count: Int? = null,
)

data class DataGenerationBodyDto(
    val start_date: String,
    val end_date: String,
    val overwrite_existing: Boolean? = null,
)

data class DataGenerationResultDto(
    val generated_count: Int? = null,
    val updated_count: Int? = null,
    val skipped_count: Int? = null,
    val duplicate_count: Int? = null,
)
