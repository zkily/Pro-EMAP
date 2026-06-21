package com.example.smart_emap.ui.mes.welding

import com.example.smart_emap.data.model.WeldingManagementRowDto

/** Web weldingDataSource.ts と同等 */
enum class WeldingDataSourceKind { Mes, Excel, Csv }

fun resolveWeldingDataSource(row: WeldingManagementRowDto): WeldingDataSourceKind {
    val ds = row.dataSource?.trim()?.lowercase().orEmpty()
    when (ds) {
        "mes" -> return WeldingDataSourceKind.Mes
        "excel" -> return WeldingDataSourceKind.Excel
        "csv" -> return WeldingDataSourceKind.Csv
    }
    val remarks = row.remarks?.trim().orEmpty()
    if (remarks.startsWith("EXCEL_SYNC:")) return WeldingDataSourceKind.Excel
    if (remarks.startsWith("CSV_IMPORT:")) return WeldingDataSourceKind.Csv
    if (!row.externalSyncKey.isNullOrBlank()) return WeldingDataSourceKind.Excel
    return WeldingDataSourceKind.Mes
}
