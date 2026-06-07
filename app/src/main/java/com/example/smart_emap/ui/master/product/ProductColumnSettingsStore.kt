package com.example.smart_emap.ui.master.product

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.productColumnSettingsDataStore by preferencesDataStore("product_master_columns")

class ProductColumnSettingsStore(
    context: Context,
) {
    private val dataStore = context.applicationContext.productColumnSettingsDataStore
    private val columnsKey = stringPreferencesKey("visible_columns")

    suspend fun load(): Map<String, Boolean> {
        val raw = dataStore.data.map { it[columnsKey] }.first()
        return mergeProductVisibleColumns(deserialize(raw))
    }

    suspend fun save(columns: Map<String, Boolean>) {
        dataStore.edit { prefs ->
            prefs[columnsKey] = serialize(columns)
        }
    }

    private fun serialize(map: Map<String, Boolean>): String =
        map.entries.joinToString("|") { "${it.key}=${it.value}" }

    private fun deserialize(raw: String?): Map<String, Boolean> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split("|").mapNotNull { part ->
            val kv = part.split("=", limit = 2)
            if (kv.size == 2) kv[0] to (kv[1] == "true") else null
        }.toMap()
    }
}
