package com.example.smart_emap.ui.erp.purchase.outsourcing.material

data class SuppliedMaterialStockItem(
    val materialCode: String,
    val materialName: String,
    val spec: String,
    val issuedQty: Int,
    val usedQty: Int,
    val stockQty: Int,
    val unit: String,
    val minStock: Int,
)

data class SuppliedMaterialSupplierStock(
    val id: Int,
    val name: String,
    val totalItems: Int,
    val lowStockItems: Int,
    val totalIssuedWeight: Int,
    val currentStockWeight: Int,
    val materials: List<SuppliedMaterialStockItem>,
)

data class SuppliedMaterialHistoryItem(
    val date: String,
    val type: String,
    val orderNo: String,
    val quantity: Int,
    val stockAfter: Int,
    val operator: String,
    val remarks: String,
)

data class UsageManagementItem(
    val id: Int,
    val usageNo: String,
    val usageDate: String,
    val orderNo: String,
    val supplier: String,
    val supplierId: Int,
    val materialCode: String,
    val materialName: String,
    val usageQty: Int,
    val unit: String,
    val usageWeight: Int,
    val productQty: Int,
    val yieldRate: Double,
    val reporter: String,
    val remarks: String = "",
)

data class MaterialIssueItem(
    val id: Int,
    val issueNo: String,
    val issueDate: String,
    val orderNo: String,
    val supplier: String,
    val supplierId: Int,
    val materialCode: String,
    val materialName: String,
    val spec: String,
    val quantity: Int,
    val unit: String,
    val unitWeight: Double,
    val totalWeight: Int,
    val status: String,
    val operator: String,
    val remarks: String = "",
)

data class OutsourcingMaterialOption(
    val value: Int,
    val label: String,
)

data class OutsourcingOrderOption(
    val orderNo: String,
    val productCode: String,
)

data class OutsourcingMaterialSelectOption(
    val code: String,
    val name: String,
    val spec: String = "",
    val unit: String,
    val unitWeight: Double,
    val stockQty: Int,
)

object OutsourcingMaterialMockData {
    val supplierOptions = listOf(
        OutsourcingMaterialOption(1, "山田メッキ工業"),
        OutsourcingMaterialOption(2, "高橋溶接工業"),
        OutsourcingMaterialOption(3, "佐藤表面処理"),
        OutsourcingMaterialOption(4, "渡辺精密溶接"),
    )

    val orderOptions = listOf(
        OutsourcingOrderOption("PO-2025-001", "PT-001"),
        OutsourcingOrderOption("PO-2025-002", "PT-002"),
        OutsourcingOrderOption("WO-2025-001", "WD-001"),
    )

    val supplierStocks = listOf(
        SuppliedMaterialSupplierStock(
            id = 1,
            name = "山田メッキ工業",
            totalItems = 3,
            lowStockItems = 1,
            totalIssuedWeight = 1250,
            currentStockWeight = 320,
            materials = listOf(
                SuppliedMaterialStockItem("M-001", "SUS304丸棒", "φ10x1000", 500, 420, 80, "本", 100),
                SuppliedMaterialStockItem("M-002", "SUS304板材", "t2.0x1000x2000", 100, 65, 35, "枚", 20),
                SuppliedMaterialStockItem("M-003", "SUS316角パイプ", "30x30x2.0", 200, 180, 20, "本", 50),
            ),
        ),
        SuppliedMaterialSupplierStock(
            id = 2,
            name = "高橋溶接工業",
            totalItems = 2,
            lowStockItems = 0,
            totalIssuedWeight = 5800,
            currentStockWeight = 1520,
            materials = listOf(
                SuppliedMaterialStockItem("M-004", "SS400板材", "t3.2x1219x2438", 150, 100, 50, "枚", 30),
                SuppliedMaterialStockItem("M-005", "SPHC-P", "t1.6x1219x2438", 80, 40, 40, "枚", 20),
            ),
        ),
        SuppliedMaterialSupplierStock(
            id = 3,
            name = "佐藤表面処理",
            totalItems = 2,
            lowStockItems = 1,
            totalIssuedWeight = 890,
            currentStockWeight = 180,
            materials = listOf(
                SuppliedMaterialStockItem("M-006", "A5052アルミ板", "t3.0x1000x2000", 60, 55, 5, "枚", 15),
                SuppliedMaterialStockItem("M-007", "C1100銅板", "t1.0x365x1200", 30, 10, 20, "枚", 10),
            ),
        ),
    )

    val usageList = listOf(
        UsageManagementItem(1, "UR-2025-001", "2025-12-03", "PO-2025-001", "山田メッキ工業", 1, "M-001", "SUS304丸棒", 50, "本", 31, 480, 96.0, "外注担当A"),
        UsageManagementItem(2, "UR-2025-002", "2025-12-03", "WO-2025-001", "高橋溶接工業", 2, "M-004", "SS400板材", 20, "枚", 1504, 190, 95.0, "外注担当B"),
        UsageManagementItem(3, "UR-2025-003", "2025-12-02", "PO-2025-002", "佐藤表面処理", 3, "M-006", "A5052アルミ板", 10, "枚", 87, 92, 92.0, "外注担当C"),
    )

    val availableUsageMaterials = listOf(
        OutsourcingMaterialSelectOption("M-001", "SUS304丸棒", unit = "本", unitWeight = 0.62, stockQty = 80),
        OutsourcingMaterialSelectOption("M-004", "SS400板材", unit = "枚", unitWeight = 75.2, stockQty = 50),
    )

    val issueList = listOf(
        MaterialIssueItem(1, "MI-2025-001", "2025-12-03", "PO-2025-001", "山田メッキ工業", 1, "M-001", "SUS304丸棒", "φ10x1000", 100, "本", 0.62, 62, "issued", "田中"),
        MaterialIssueItem(2, "MI-2025-002", "2025-12-03", "WO-2025-001", "高橋溶接工業", 2, "M-002", "SS400板材", "t3.2x1219x2438", 50, "枚", 75.2, 3760, "issued", "鈴木"),
        MaterialIssueItem(3, "MI-2025-003", "2025-12-03", "", "佐藤表面処理", 3, "M-003", "SPHC-P", "t1.6x1219x2438", 30, "枚", 37.6, 1128, "preparing", "山田", remarks = "午後出庫予定"),
    )

    val materialOptions = listOf(
        OutsourcingMaterialSelectOption("M-001", "SUS304丸棒", "φ10x1000", "本", 0.62, 500),
        OutsourcingMaterialSelectOption("M-002", "SS400板材", "t3.2x1219x2438", "枚", 75.2, 200),
        OutsourcingMaterialSelectOption("M-003", "SPHC-P", "t1.6x1219x2438", "枚", 37.6, 150),
    )

    fun sampleHistory(): List<SuppliedMaterialHistoryItem> = listOf(
        SuppliedMaterialHistoryItem("2025-12-03", "issue", "MI-2025-001", 100, 80, "田中", ""),
        SuppliedMaterialHistoryItem("2025-12-02", "usage", "PO-2025-001", 50, -20, "-", "納品完了"),
        SuppliedMaterialHistoryItem("2025-12-01", "issue", "MI-2025-000", 200, 30, "鈴木", ""),
        SuppliedMaterialHistoryItem("2025-11-28", "usage", "PO-2025-000", 170, -170, "-", ""),
    )
}
