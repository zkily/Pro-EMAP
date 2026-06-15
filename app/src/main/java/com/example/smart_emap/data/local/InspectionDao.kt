package com.example.smart_emap.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspection_plans WHERE productionDay = :day")
    fun getPlansByDay(day: String): Flow<List<InspectionPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<InspectionPlanEntity>)

    @Query("DELETE FROM inspection_plans WHERE productionDay = :day")
    suspend fun deletePlansByDay(day: String)

    @Query("SELECT * FROM erp_products")
    suspend fun getAllProducts(): List<ErpProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ErpProductEntity>)
}
