package com.example.smart_emap

import android.content.Context
import com.example.smart_emap.core.auth.SessionEvents
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.mes.ChamferingOfflineStore
import com.example.smart_emap.core.mes.CuttingOfflineStore
import com.example.smart_emap.core.mes.InspectionOfflineStore
import com.example.smart_emap.core.mes.WeldingOfflineStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.core.network.NetworkMonitor
import com.example.smart_emap.data.local.AppDatabase
import com.example.smart_emap.data.repository.*

class SmartEmapAppContainer(context: Context) {
    /** Room 未使用前先不初始化，避免启动阶段主线程建库导致部分设备闪退 */
    val database by lazy { AppDatabase.getDatabase(context.applicationContext) }
    val inspectionDao by lazy { database.inspectionDao() }

    val sessionStore = SessionStore(context.applicationContext)
    val sessionEvents = SessionEvents()
    val mesClientIdStore = MesClientIdStore(context.applicationContext)
    val apiClient = ApiClient(sessionStore, sessionEvents)

    val authRepository = AuthRepository(sessionStore, apiClient)
    val dashboardRepository = DashboardRepository(apiClient)
    val systemUserRepository = SystemUserRepository(apiClient)
    val inspectionRepository = InspectionRepository(apiClient, mesClientIdStore, systemUserRepository)
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
    val apsSchedulingRepository = ApsSchedulingRepository(apiClient)
    val partRepository = PartRepository(apiClient)
    val planInstructionRepository = PlanInstructionRepository(apiClient)
    val productionSummaryRepository = ProductionSummaryRepository(apiClient)
    val planBaselineRepository = PlanBaselineRepository(apiClient)
    val systemOrganizationRepository = SystemOrganizationRepository(apiClient)
    val systemRoleRepository = SystemRoleRepository(apiClient)
    val networkMonitor = NetworkMonitor(context.applicationContext)
}
