package com.example.smart_emap.core.mes

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.PatchWeldingBody
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.WeldingManagementRowDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.weldingOfflineDataStore by preferencesDataStore("welding_offline")

data class WeldingPendingCreatePlan(
    val localPlanId: Int,
    val productionDay: String,
    val productCd: String,
    val productName: String,
    val operatorUserId: Int,
)

data class WeldingPendingPatch(
    val planId: Int,
    val body: PatchWeldingBody,
)

data class WeldingOfflineSyncState(
    val nextLocalPlanId: Int = -1,
    val localToServer: Map<Int, Int> = emptyMap(),
    val creates: List<WeldingPendingCreatePlan> = emptyList(),
    val patches: List<WeldingPendingPatch> = emptyList(),
)

data class WeldingOfflineCacheSnapshot(
    val products: List<ErpProductDto> = emptyList(),
    val defectItems: List<ProcessDefectItemDto> = emptyList(),
    val plansByDay: Map<String, List<WeldingManagementRowDto>> = emptyMap(),
)

class WeldingOfflineStore(context: Context) {
    private val dataStore = context.applicationContext.weldingOfflineDataStore
    private val mutex = Mutex()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val syncAdapter = moshi.adapter(WeldingOfflineSyncState::class.java)
    private val cacheAdapter = moshi.adapter(WeldingOfflineCacheSnapshot::class.java)

    private val syncKey = stringPreferencesKey("sync_state_v1")
    private val cacheKey = stringPreferencesKey("cache_snapshot_v1")

    suspend fun loadCache(): WeldingOfflineCacheSnapshot = mutex.withLock { readCache() }

    suspend fun saveProducts(products: List<ErpProductDto>) = mutex.withLock {
        writeCache(readCache().copy(products = products))
    }

    suspend fun saveDefectItems(items: List<ProcessDefectItemDto>) = mutex.withLock {
        writeCache(readCache().copy(defectItems = items))
    }

    suspend fun savePlans(productionDay: String, rows: List<WeldingManagementRowDto>) = mutex.withLock {
        val cache = readCache()
        writeCache(cache.copy(plansByDay = cache.plansByDay + (productionDay to rows)))
    }

    suspend fun pendingCount(): Int = mutex.withLock {
        val sync = readSync()
        sync.creates.size + sync.patches.size
    }

    fun isLocalPlanId(planId: Int): Boolean = planId < 0

    suspend fun resolvePlanId(planId: Int): Int {
        if (planId > 0) return planId
        return mutex.withLock { readSync().localToServer[planId] ?: planId }
    }

    suspend fun allocateLocalPlanId(): Int = mutex.withLock {
        val sync = readSync()
        val id = sync.nextLocalPlanId
        writeSync(sync.copy(nextLocalPlanId = id - 1))
        id
    }

    suspend fun enqueueCreate(entry: WeldingPendingCreatePlan) = mutex.withLock {
        val sync = readSync()
        writeSync(sync.copy(creates = sync.creates + entry))
    }

    suspend fun enqueuePatch(planId: Int, body: PatchWeldingBody) = mutex.withLock {
        val sync = readSync()
        val patches = sync.patches.toMutableList()
        val lastIdx = patches.indexOfLast { it.planId == planId }
        if (lastIdx >= 0) {
            patches[lastIdx] = WeldingPendingPatch(planId, mergeWeldingPatch(patches[lastIdx].body, body))
        } else {
            patches.add(WeldingPendingPatch(planId, body))
        }
        writeSync(sync.copy(patches = patches))
    }

    suspend fun flush(
        createPlan: suspend (WeldingPendingCreatePlan) -> Int,
        patchPlan: suspend (Int, PatchWeldingBody) -> Unit,
    ): FlushOfflineResult = mutex.withLock {
        var sync = readSync()
        var syncedCreates = 0
        var syncedPatches = 0

        val creates = sync.creates.toMutableList()
        while (creates.isNotEmpty()) {
            val entry = creates.first()
            val serverId = createPlan(entry)
            creates.removeAt(0)
            syncedCreates++
            val mapping = sync.localToServer.toMutableMap()
            mapping[entry.localPlanId] = serverId
            sync = sync.copy(localToServer = mapping, creates = creates.toList())
            sync = remapPatchesAfterCreate(sync, entry.localPlanId, serverId)
            writeSync(sync)
        }

        val patches = sync.patches.toMutableList()
        while (patches.isNotEmpty()) {
            val entry = patches.first()
            val resolved = sync.localToServer[entry.planId] ?: entry.planId
            if (resolved < 0) break
            patchPlan(resolved, entry.body)
            patches.removeAt(0)
            syncedPatches++
            sync = sync.copy(patches = patches.toList())
            writeSync(sync)
        }

        FlushOfflineResult(
            syncedCreates = syncedCreates,
            syncedPatches = syncedPatches,
            remaining = sync.creates.size + sync.patches.size,
        )
    }

    private fun remapPatchesAfterCreate(
        sync: WeldingOfflineSyncState,
        localId: Int,
        serverId: Int,
    ): WeldingOfflineSyncState {
        val patches = sync.patches.map { patch ->
            if (patch.planId == localId) patch.copy(planId = serverId) else patch
        }
        val merged = mutableListOf<WeldingPendingPatch>()
        for (patch in patches) {
            val idx = merged.indexOfLast { it.planId == patch.planId }
            if (idx >= 0) {
                merged[idx] = WeldingPendingPatch(patch.planId, mergeWeldingPatch(merged[idx].body, patch.body))
            } else {
                merged.add(patch)
            }
        }
        return sync.copy(patches = merged)
    }

    private suspend fun readSync(): WeldingOfflineSyncState {
        val json = dataStore.data.map { it[syncKey] }.first()
        if (json.isNullOrBlank()) return WeldingOfflineSyncState()
        return runCatching { syncAdapter.fromJson(json) }.getOrNull() ?: WeldingOfflineSyncState()
    }

    private suspend fun writeSync(state: WeldingOfflineSyncState) {
        dataStore.edit { prefs -> prefs[syncKey] = syncAdapter.toJson(state) }
    }

    private suspend fun readCache(): WeldingOfflineCacheSnapshot {
        val json = dataStore.data.map { it[cacheKey] }.first()
        if (json.isNullOrBlank()) return WeldingOfflineCacheSnapshot()
        return runCatching { cacheAdapter.fromJson(json) }.getOrNull() ?: WeldingOfflineCacheSnapshot()
    }

    private suspend fun writeCache(cache: WeldingOfflineCacheSnapshot) {
        dataStore.edit { prefs -> prefs[cacheKey] = cacheAdapter.toJson(cache) }
    }
}

fun mergeWeldingPatch(previous: PatchWeldingBody, incoming: PatchWeldingBody): PatchWeldingBody {
    val defects = previous.mesDefectByItem.orEmpty().toMutableMap()
    incoming.mesDefectByItem?.forEach { (k, v) -> defects[k] = v }
    return PatchWeldingBody(
        productionDay = incoming.productionDay ?: previous.productionDay,
        productionSequence = incoming.productionSequence ?: previous.productionSequence,
        actualProductionQuantity = incoming.actualProductionQuantity ?: previous.actualProductionQuantity,
        productionCompletedCheck = incoming.productionCompletedCheck ?: previous.productionCompletedCheck,
        defectQty = incoming.defectQty ?: previous.defectQty,
        mesProductionStartedAt = incoming.mesProductionStartedAt ?: previous.mesProductionStartedAt,
        mesProductionEndedAt = incoming.mesProductionEndedAt ?: previous.mesProductionEndedAt,
        mesNetProductionSec = incoming.mesNetProductionSec ?: previous.mesNetProductionSec,
        mesPausedAccumSec = incoming.mesPausedAccumSec ?: previous.mesPausedAccumSec,
        mesProductionIsPaused = incoming.mesProductionIsPaused ?: previous.mesProductionIsPaused,
        mesOperatorUserId = incoming.mesOperatorUserId ?: previous.mesOperatorUserId,
        mesDefectByItem = defects.ifEmpty { null },
        mesClientInstanceId = incoming.mesClientInstanceId ?: previous.mesClientInstanceId,
        mesClaimClientLock = incoming.mesClaimClientLock ?: previous.mesClaimClientLock,
        mesForceRelease = incoming.mesForceRelease ?: previous.mesForceRelease,
        remarks = incoming.remarks ?: previous.remarks,
    )
}
