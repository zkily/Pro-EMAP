package com.example.smart_emap.core.mes

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.mesClientDataStore by preferencesDataStore("mes_client")

class MesClientIdStore(private val context: Context) {
    private val inspectionKey = stringPreferencesKey("inspection_client_instance_v1")
    private val weldingKey = stringPreferencesKey("welding_client_instance_v1")

    suspend fun getClientInstanceId(): String = getOrCreateId(inspectionKey)

    suspend fun getWeldingClientInstanceId(): String = getOrCreateId(weldingKey)

    private suspend fun getOrCreateId(key: androidx.datastore.preferences.core.Preferences.Key<String>): String {
        val existing = context.mesClientDataStore.data.map { it[key] }.first()?.trim()
        if (!existing.isNullOrEmpty()) return existing
        val id = UUID.randomUUID().toString()
        context.mesClientDataStore.edit { prefs -> prefs[key] = id }
        return id
    }
}
