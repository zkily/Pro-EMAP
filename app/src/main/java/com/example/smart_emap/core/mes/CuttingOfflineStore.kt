package com.example.smart_emap.core.mes

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smart_emap.data.model.CuttingManagementRowDto
import com.example.smart_emap.data.model.PatchCuttingBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.cuttingOfflineDataStore by preferencesDataStore("cutting_offline")

data class CuttingPendingPatch(
    val scopeKey: String,
    val planId: Int,
    val body: PatchCuttingBody,
)

data class CuttingOfflineSyncState(
    val patches: List<CuttingPendingPatch> = emptyList(),
)

data class CuttingScopePersist(
    val savedAt: Long = 0,
    val sessions: Map<String, PersistedCuttingPlanSession> = emptyMap(),
    val cachedPlans: List<CuttingManagementRowDto> = emptyList(),
)

data class CuttingOfflineStoreState(
    val v: Int = 2,
    val productionDay: String = "",
    val selectedMachineId: Int? = null,
    val hideCompleted: Boolean = true,
    val scopes: Map<String, CuttingScopePersist> = emptyMap(),
    val sync: CuttingOfflineSyncState = CuttingOfflineSyncState(),
)

data class CuttingPagePersistSnapshot(
    val productionDay: String,
    val selectedMachineId: Int?,
    val hideCompleted: Boolean,
    val sessions: Map<String, PersistedCuttingPlanSession>,
)

data class CuttingFilterPrefs(
    val selectedMachineId: Int? = null,
    val hideCompleted: Boolean = true,
)

class CuttingOfflineStore(context: Context) {
    private val dataStore = context.applicationContext.cuttingOfflineDataStore
    private val mutex = Mutex()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val storeAdapter = moshi.adapter(CuttingOfflineStoreState::class.java)
    private val patchAdapter = moshi.adapter(PatchCuttingBody::class.java)
    private val sessionsMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        PersistedCuttingPlanSession::class.java,
    )
    private val sessionsAdapter = moshi.adapter<Map<String, PersistedCuttingPlanSession>>(sessionsMapType)
    private val key = stringPreferencesKey("store_v2")

    fun makeScopeKey(productionDay: String, machineId: Int?): String =
        "${productionDay.trim()}::${machineId ?: "none"}"

    suspend fun loadSnapshot(productionDay: String, machineId: Int?): CuttingPagePersistSnapshot? =
        mutex.withLock {
            val store = readStore()
            val scope = store.scopes[makeScopeKey(productionDay, machineId)] ?: return@withLock null
            CuttingPagePersistSnapshot(
                productionDay = store.productionDay,
                selectedMachineId = store.selectedMachineId,
                hideCompleted = store.hideCompleted,
                sessions = scope.sessions,
            )
        }

    suspend fun saveSnapshot(snapshot: CuttingPagePersistSnapshot) = mutex.withLock {
        val store = readStore()
        val scopeKey = makeScopeKey(snapshot.productionDay, snapshot.selectedMachineId)
        val existing = store.scopes[scopeKey]
        val updated = store.copy(
            productionDay = snapshot.productionDay,
            selectedMachineId = snapshot.selectedMachineId,
            hideCompleted = snapshot.hideCompleted,
            scopes = store.scopes + (
                scopeKey to CuttingScopePersist(
                    savedAt = System.currentTimeMillis(),
                    sessions = snapshot.sessions,
                    cachedPlans = existing?.cachedPlans.orEmpty(),
                )
                ),
        )
        writeStore(updated)
    }

    suspend fun saveCachedPlans(
        productionDay: String,
        machineId: Int?,
        rows: List<CuttingManagementRowDto>,
    ) = mutex.withLock {
        val store = readStore()
        val scopeKey = makeScopeKey(productionDay, machineId)
        val existing = store.scopes[scopeKey] ?: CuttingScopePersist()
        writeStore(
            store.copy(
                scopes = store.scopes + (
                    scopeKey to existing.copy(
                        savedAt = System.currentTimeMillis(),
                        cachedPlans = rows,
                    )
                    ),
            ),
        )
    }

    suspend fun loadCachedPlans(productionDay: String, machineId: Int?): List<CuttingManagementRowDto>? =
        mutex.withLock {
            val plans = readStore().scopes[makeScopeKey(productionDay, machineId)]?.cachedPlans.orEmpty()
            plans.takeIf { it.isNotEmpty() }
        }

    suspend fun saveFilter(productionDay: String, machineId: Int?, hideCompleted: Boolean) = mutex.withLock {
        val store = readStore()
        writeStore(
            store.copy(
                productionDay = productionDay,
                selectedMachineId = machineId,
                hideCompleted = hideCompleted,
            ),
        )
    }

    /** 生産日は毎回当日を使うため、端末から復元するのは設備・フィルタのみ */
    suspend fun loadFilterPrefs(): CuttingFilterPrefs = mutex.withLock {
        val store = readStore()
        CuttingFilterPrefs(
            selectedMachineId = store.selectedMachineId,
            hideCompleted = store.hideCompleted,
        )
    }

    suspend fun pendingCount(): Int = mutex.withLock { readStore().sync.patches.size }

    suspend fun enqueuePatch(scopeKey: String, planId: Int, body: PatchCuttingBody) = mutex.withLock {
        val store = readStore()
        val patches = store.sync.patches.toMutableList()
        val idx = patches.indexOfLast { it.scopeKey == scopeKey && it.planId == planId }
        if (idx >= 0) {
            patches[idx] = CuttingPendingPatch(scopeKey, planId, mergeCuttingPatch(patches[idx].body, body))
        } else {
            patches.add(CuttingPendingPatch(scopeKey, planId, body))
        }
        writeStore(store.copy(sync = store.sync.copy(patches = patches)))
    }

    suspend fun flush(
        patchPlan: suspend (Int, PatchCuttingBody) -> Unit,
    ): FlushOfflineResult = mutex.withLock {
        val store = readStore()
        var synced = 0
        val remaining = store.sync.patches.toMutableList()
        while (remaining.isNotEmpty()) {
            val entry = remaining.first()
            try {
                patchPlan(entry.planId, entry.body)
                remaining.removeAt(0)
                synced++
            } catch (_: Exception) {
                break
            }
        }
        writeStore(store.copy(sync = store.sync.copy(patches = remaining)))
        FlushOfflineResult(syncedPatches = synced, remaining = remaining.size)
    }

    private suspend fun readStore(): CuttingOfflineStoreState {
        val json = dataStore.data.map { it[key] }.first()
        if (json.isNullOrBlank()) return CuttingOfflineStoreState()
        return runCatching { storeAdapter.fromJson(json) }.getOrNull() ?: CuttingOfflineStoreState()
    }

    private suspend fun writeStore(state: CuttingOfflineStoreState) {
        dataStore.edit { prefs -> prefs[key] = storeAdapter.toJson(state) }
    }
}

fun mergeCuttingPatch(previous: PatchCuttingBody, incoming: PatchCuttingBody): PatchCuttingBody =
    PatchCuttingBody(
        productionDay = incoming.productionDay ?: previous.productionDay,
        cuttingMachine = incoming.cuttingMachine ?: previous.cuttingMachine,
        productionSequence = incoming.productionSequence ?: previous.productionSequence,
        actualProductionQuantity = incoming.actualProductionQuantity ?: previous.actualProductionQuantity,
        productionCompletedCheck = incoming.productionCompletedCheck ?: previous.productionCompletedCheck,
        defectQty = incoming.defectQty ?: previous.defectQty,
        mesProductionStartedAt = incoming.mesProductionStartedAt ?: previous.mesProductionStartedAt,
        mesProductionEndedAt = incoming.mesProductionEndedAt ?: previous.mesProductionEndedAt,
        mesNetProductionSec = incoming.mesNetProductionSec ?: previous.mesNetProductionSec,
        mesPausedAccumSec = incoming.mesPausedAccumSec ?: previous.mesPausedAccumSec,
        mesProductionIsPaused = incoming.mesProductionIsPaused ?: previous.mesProductionIsPaused,
        mesSetupTimeMin = incoming.mesSetupTimeMin ?: previous.mesSetupTimeMin,
        mesSawBladeExchangeMin = incoming.mesSawBladeExchangeMin ?: previous.mesSawBladeExchangeMin,
        mesRepairMin = incoming.mesRepairMin ?: previous.mesRepairMin,
        mesOperatorUserId = incoming.mesOperatorUserId ?: previous.mesOperatorUserId,
        mesScannedCode = incoming.mesScannedCode ?: previous.mesScannedCode,
        remarks = incoming.remarks ?: previous.remarks,
    )
