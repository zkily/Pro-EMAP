package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.BatchActualTransaction
import com.example.smart_emap.data.model.ProductByProcessDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.model.StockTransactionLogBody
import com.example.smart_emap.data.model.StockTransactionLogRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BatchInitialStockRow(
    val productCd: String,
    val productName: String,
    val editQuantity: Int?,
    val existingId: Int? = null,
    val existingQuantity: Int? = null,
)

data class BatchActualRow(
    val productCd: String = "",
    val productName: String = "",
    val date: String = "",
    val cuttingActual: Int? = null,
    val chamferingActual: Int? = null,
    val moldingActual: Int? = null,
)

object DataMgmtBatchLogic {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    fun currentMonthYm(): String {
        val now = LocalDate.now()
        return "%04d-%02d".format(now.year, now.monthValue)
    }

    fun filterProductsForInitial(products: List<ProductByProcessDto>): List<ProductByProcessDto> =
        products.filter { p ->
            val cd = p.productCd.trim()
            if (cd.isBlank() || !cd.endsWith("1")) return@filter false
            val name = p.productName.orEmpty()
            if (name.contains("加工") || name.contains("アーチ")) return@filter false
            true
        }

    fun buildInitialStockRows(
        products: List<ProductByProcessDto>,
        existingLogs: List<StockTransactionLogRowDto>,
    ): List<BatchInitialStockRow> {
        val existingMap = existingLogs.associateBy { it.targetCd.orEmpty().trim() }
        return filterProductsForInitial(products)
            .map { p ->
                val cd = p.productCd.trim()
                val ex = existingMap[cd]
                BatchInitialStockRow(
                    productCd = cd,
                    productName = p.productName.orEmpty().trim(),
                    editQuantity = ex?.quantity,
                    existingId = ex?.id,
                    existingQuantity = ex?.quantity,
                )
            }
            .sortedWith(compareBy({ it.productName }, { it.productCd }))
    }

    fun stockMetaForProcess(processCd: String): Pair<String, String> = when (processCd) {
        "KT13" -> "製品" to "製品倉庫"
        "KT15" -> "製品" to "外注倉庫"
        else -> "仕掛品" to "工程中間在庫"
    }

    fun buildInitialStockUpdates(
        month: String,
        processCd: String,
        rows: List<BatchInitialStockRow>,
    ): Pair<List<StockTransactionLogBody>, List<Pair<Int, StockTransactionLogBody>>> {
        val transactionTime = "$month-01 00:00:00"
        val (stockType, locationCd) = stockMetaForProcess(processCd)
        val inserts = mutableListOf<StockTransactionLogBody>()
        val updates = mutableListOf<Pair<Int, StockTransactionLogBody>>()
        rows.forEach { row ->
            val newQty = row.editQuantity ?: return@forEach
            val existingQty = row.existingQuantity ?: 0
            if (newQty == existingQty) return@forEach
            val body = StockTransactionLogBody(
                transactionTime = transactionTime,
                transactionType = "初期",
                targetCd = row.productCd,
                quantity = newQty,
                stockType = stockType,
                locationCd = locationCd,
                processCd = processCd,
            )
            if (row.existingId != null) {
                updates += row.existingId to body
            } else if (newQty > 0) {
                inserts += body
            }
        }
        return inserts to updates
    }

    fun emptyBatchActualRows(date: String, count: Int = 2): List<BatchActualRow> =
        List(count) { BatchActualRow(date = date) }

    fun buildBatchActualTransactions(
        date: String,
        rows: List<BatchActualRow>,
    ): List<BatchActualTransaction> {
        val transactionTime = "$date 00:00:00"
        val result = mutableListOf<BatchActualTransaction>()
        rows.filter { it.productCd.isNotBlank() }.forEach { row ->
            row.cuttingActual?.takeIf { it != 0 }?.let {
                result += BatchActualTransaction(row.productCd, "KT01", it, transactionTime)
            }
            row.chamferingActual?.takeIf { it != 0 }?.let {
                result += BatchActualTransaction(row.productCd, "KT02", it, transactionTime)
            }
            row.moldingActual?.takeIf { it != 0 }?.let {
                result += BatchActualTransaction(row.productCd, "KT04", it, transactionTime)
            }
        }
        return result
    }

    fun fallbackProductsFromSummary(
        products: List<ProductionSummaryProductOptionDto>,
    ): List<ProductByProcessDto> = products.map {
        ProductByProcessDto(productCd = it.productCd, productName = it.productName)
    }
}
