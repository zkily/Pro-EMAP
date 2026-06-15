package com.example.smart_emap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspection_plans")
data class InspectionPlanEntity(
    @PrimaryKey val id: Int,
    val productionDay: String,
    val productCd: String,
    val productName: String,
    val actualProductionQuantity: Int,
    val defectQty: Int,
    val isCompleted: Boolean,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "erp_products")
data class ErpProductEntity(
    @PrimaryKey val productCode: String,
    val productName: String,
    val isActive: Boolean
)
