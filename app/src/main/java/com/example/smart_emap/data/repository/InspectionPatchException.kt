package com.example.smart_emap.data.repository

/** PATCH /api/plan/inspection-management/{id} の HTTP エラー（409 端末ロック等） */
class InspectionPatchException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
