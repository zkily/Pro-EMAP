package com.example.smart_emap.data.repository

class CuttingPatchException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
