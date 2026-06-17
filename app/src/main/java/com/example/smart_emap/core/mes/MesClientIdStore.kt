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

    suspend fun getClientInstanceId(userId: Int? = null): String =
        getOrCreateId(inspectionKey, userId)

    suspend fun getWeldingClientInstanceId(): String = getOrCreateId(weldingKey, null)

    private fun userBackupKey(userId: Int) =
        stringPreferencesKey("inspection_client_instance_u$userId")

    private suspend fun readId(key: androidx.datastore.preferences.core.Preferences.Key<String>): String? =
        context.mesClientDataStore.data.map { it[key]?.trim().orEmpty() }.first().ifEmpty { null }

    private suspend fun writeId(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        value: String,
    ) {
        context.mesClientDataStore.edit { prefs -> prefs[key] = value }
    }

    private suspend fun getOrCreateId(
        key: androidx.datastore.preferences.core.Preferences.Key<String>,
        userId: Int?,
    ): String {
        readId(key)?.let { existing ->
            if (userId != null) writeId(userBackupKey(userId), existing)
            return existing
        }
        if (userId != null) {
            readId(userBackupKey(userId))?.let { backup ->
                writeId(key, backup)
                return backup
            }
        }
        val id = UUID.randomUUID().toString()
        writeId(key, id)
        if (userId != null) writeId(userBackupKey(userId), id)
        return id
    }
}
