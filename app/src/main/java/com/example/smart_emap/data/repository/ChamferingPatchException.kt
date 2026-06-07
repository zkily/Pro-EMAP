package com.example.smart_emap.data.repository

class ChamferingPatchException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
