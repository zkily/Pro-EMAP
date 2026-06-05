package com.example.smart_emap

import android.content.Context
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.mes.MesClientIdStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.repository.AuthRepository
import com.example.smart_emap.data.repository.DashboardRepository
import com.example.smart_emap.data.repository.InspectionRepository

class SmartEmapAppContainer(context: Context) {
    val sessionStore = SessionStore(context.applicationContext)
    val mesClientIdStore = MesClientIdStore(context.applicationContext)
    val apiClient = ApiClient(sessionStore)
    val authRepository = AuthRepository(sessionStore, apiClient)
    val dashboardRepository = DashboardRepository(apiClient)
    val inspectionRepository = InspectionRepository(apiClient, mesClientIdStore)
}
