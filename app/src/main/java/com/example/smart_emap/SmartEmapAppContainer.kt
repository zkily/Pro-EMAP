package com.example.smart_emap

import android.content.Context
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.mes.ChamferingOfflineStore
import com.example.smart_emap.core.mes.CuttingOfflineStore
import com.example.smart_emap.core.mes.InspectionOfflineStore
import com.example.smart_emap.core.mes.WeldingOfflineStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.core.network.NetworkMonitor
import com.example.smart_emap.data.repository.AuthRepository
import com.example.smart_emap.data.repository.ChamferingRepository
import com.example.smart_emap.data.repository.CuttingInstructionRepository
import com.example.smart_emap.data.repository.CuttingRepository
import com.example.smart_emap.data.repository.DashboardRepository
import com.example.smart_emap.data.repository.InspectionRepository
import com.example.smart_emap.data.repository.MaterialRepository
import com.example.smart_emap.data.repository.OrderDailyRepository
import com.example.smart_emap.data.repository.OrderMonthlyRepository
import com.example.smart_emap.data.repository.MasterRepository
import com.example.smart_emap.data.repository.PartRepository
import com.example.smart_emap.data.repository.WeldingRepository

class SmartEmapAppContainer(context: Context) {
    val sessionStore = SessionStore(context.applicationContext)
    val mesClientIdStore = MesClientIdStore(context.applicationContext)
    val apiClient = ApiClient(sessionStore)
    val authRepository = AuthRepository(sessionStore, apiClient)
    val dashboardRepository = DashboardRepository(apiClient)
    val inspectionRepository = InspectionRepository(apiClient, mesClientIdStore)
    val inspectionOfflineStore = InspectionOfflineStore(context.applicationContext)
    val weldingRepository = WeldingRepository(apiClient, mesClientIdStore)
    val weldingOfflineStore = WeldingOfflineStore(context.applicationContext)
    val cuttingRepository = CuttingRepository(apiClient)
    val cuttingInstructionRepository = CuttingInstructionRepository(apiClient)
    val cuttingOfflineStore = CuttingOfflineStore(context.applicationContext)
    val chamferingRepository = ChamferingRepository(apiClient)
    val chamferingOfflineStore = ChamferingOfflineStore(context.applicationContext)
    val orderMonthlyRepository = OrderMonthlyRepository(apiClient)
    val orderDailyRepository = OrderDailyRepository(apiClient)
    val materialRepository = MaterialRepository(apiClient)
    val masterRepository = MasterRepository(apiClient)
    val partRepository = PartRepository(apiClient)
    val networkMonitor = NetworkMonitor(context.applicationContext)
}
