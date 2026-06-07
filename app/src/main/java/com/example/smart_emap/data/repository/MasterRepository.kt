package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterBatchDeleteBodyDto
import com.example.smart_emap.data.model.MasterCarrierBodyDto
import com.example.smart_emap.data.model.MasterCustomerBodyDto
import com.example.smart_emap.data.model.MasterDestinationBodyDto
import com.example.smart_emap.data.model.MasterDestinationHolidayDto
import com.example.smart_emap.data.model.MasterDestinationWorkdayDto
import com.example.smart_emap.data.model.MasterInspectionBodyDto
import com.example.smart_emap.data.model.MasterMachineBodyDto
import com.example.smart_emap.data.model.MasterMaterialBodyDto
import com.example.smart_emap.data.model.MasterPartBodyDto
import com.example.smart_emap.data.model.MasterProcessBodyDto
import com.example.smart_emap.data.model.MasterProcessRouteBodyDto
import com.example.smart_emap.data.model.MasterProcessingFeeBodyDto
import com.example.smart_emap.data.model.MasterProductBodyDto
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductCsvExportItemDto
import com.example.smart_emap.data.model.ProductCsvExportResultDto
import com.example.smart_emap.data.model.ProductMasterStatsDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto
import com.example.smart_emap.data.model.MasterProductRouteStepDto
import com.example.smart_emap.data.model.MasterRollerBodyDto
import com.example.smart_emap.data.model.MasterRouteStepBodyDto
import com.example.smart_emap.data.model.MasterSupplierBodyDto
import com.example.smart_emap.ui.master.MasterPageKind
import com.example.smart_emap.ui.master.MasterTableRow
import com.example.smart_emap.ui.master.product.PRODUCT_MASTER_PAGE_SIZE

class MasterRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadDestinationOptions(): List<DestinationOptionDto> = runCatching {
        apiClient.masterApi().destinationOptions()
    }.getOrElse { emptyList() }

    data class ProductMasterFilters(
        val keyword: String = "",
        val category: String = "",
        val kind: String = "",
        val materialCd: String = "",
        val page: Int = 1,
        val pageSize: Int = PRODUCT_MASTER_PAGE_SIZE,
    )

    suspend fun loadProductMasterList(filters: ProductMasterFilters): Pair<List<MasterProductDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listProducts(
            keyword = filters.keyword.takeIf { it.isNotBlank() },
            category = filters.category.takeIf { it.isNotBlank() },
            kind = filters.kind.takeIf { it.isNotBlank() },
            materialCd = filters.materialCd.takeIf { it.isNotBlank() },
            page = filters.page,
            pageSize = filters.pageSize,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<MasterProductDto>() to 0 }

    suspend fun loadProductMasterStats(): ProductMasterStatsDto = runCatching {
        val all = apiClient.masterApi().listProducts(pageSize = 9999).items()
        var mass = 0
        var proto = 0
        var supply = 0
        var other = 0
        all.forEach { p ->
            when (p.productType) {
                "量産品" -> mass++
                "試作品" -> proto++
                "補給品" -> supply++
                else -> other++
            }
        }
        ProductMasterStatsDto(total = all.size, massProduction = mass, prototype = proto, supply = supply, other = other)
    }.getOrElse { ProductMasterStatsDto() }

    suspend fun loadMaterialOptionsForProduct(): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listMaterials(pageSize = 9999).items()
            .map { (it.materialCd.orEmpty()) to (it.materialName.orEmpty()) }
            .filter { it.first.isNotBlank() }
    }.getOrElse { emptyList() }

    suspend fun loadRouteOptionsForProduct(): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listProcessRoutes(pageSize = 500).items()
            .map { (it.routeCd.orEmpty()) to (it.routeName.orEmpty()) }
            .filter { it.first.isNotBlank() }
    }.getOrElse { emptyList() }

    suspend fun loadNextProductCd(fallbackProducts: List<MasterProductDto> = emptyList()): String = runCatching {
        val maxCd = apiClient.masterApi().getMaxProductCd()
        formatNextProductCd(maxCd)
    }.getOrElse {
        nextProductCdFromList(fallbackProducts)
    }

    fun nextProductCdFromList(products: List<MasterProductDto>): String {
        val maxFromList = products
            .mapNotNull { it.productCd?.trim()?.toIntOrNull() }
            .maxOrNull() ?: 90000
        return formatNextProductCd(maxFromList)
    }

    private fun formatNextProductCd(maxCd: Int): String {
        val base = if (maxCd > 0) maxCd else 90000
        return (base + 10).toString().padStart(5, '0')
    }

    private fun buildProductBody(fields: Map<String, String>): MasterProductBodyDto {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        return MasterProductBodyDto(
            productCd = fields["product_cd"].orEmpty().trim(),
            productName = fields["product_name"].orEmpty().trim(),
            productType = blankToNull(fields["product_type"]),
            partNumber = blankToNull(fields["part_number"]),
            productAlias = blankToNull(fields["product_alias"]),
            category = blankToNull(fields["category"]),
            kind = blankToNull(fields["kind"]),
            priority = fields["priority"]?.toIntOrNull() ?: 2,
            status = blankToNull(fields["status"]) ?: "active",
            unitPrice = fields["unit_price"]?.toDoubleOrNull(),
            processCount = fields["process_count"]?.toIntOrNull() ?: 1,
            isMultistage = fields["is_multistage"] != "false",
            leadTime = fields["lead_time"]?.toIntOrNull(),
            safetyDays = fields["safety_days"]?.toIntOrNull(),
            lotSize = fields["lot_size"]?.toIntOrNull() ?: 1,
            routeCd = blankToNull(fields["route_cd"]),
            boxType = blankToNull(fields["box_type"]),
            unitPerBox = fields["unit_per_box"]?.toIntOrNull(),
            dimensions = blankToNull(fields["dimensions"]),
            weight = fields["weight"]?.toDoubleOrNull(),
            destinationCd = blankToNull(fields["destination_cd"]),
            vehicleModel = blankToNull(fields["vehicle_model"]),
            locationCd = blankToNull(fields["location_cd"]),
            startUseDate = blankToNull(fields["start_use_date"]),
            materialCd = blankToNull(fields["material_cd"]),
            cutLength = fields["cut_length"]?.toDoubleOrNull(),
            chamferLength = fields["chamfer_length"]?.toDoubleOrNull(),
            developedLength = fields["developed_length"]?.toDoubleOrNull(),
            scrapLength = fields["scrap_length"]?.toDoubleOrNull(),
            takeCount = fields["take_count"]?.toIntOrNull(),
            note = blankToNull(fields["note"]),
        )
    }

    suspend fun saveProduct(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        val body = buildProductBody(fields)
        val api = apiClient.masterApi()
        if (id == null) api.createProduct(body) else api.updateProduct(id, body)
        true
    }.getOrElse { false }

    suspend fun deleteProduct(id: Int) {
        apiClient.masterApi().deleteProduct(id)
    }

    suspend fun loadAllProducts(filters: ProductMasterFilters = ProductMasterFilters()): List<MasterProductDto> = runCatching {
        apiClient.masterApi().listProducts(
            keyword = filters.keyword.takeIf { it.isNotBlank() },
            category = filters.category.takeIf { it.isNotBlank() },
            kind = filters.kind.takeIf { it.isNotBlank() },
            materialCd = filters.materialCd.takeIf { it.isNotBlank() },
            page = 1,
            pageSize = 10000,
        ).items()
    }.getOrElse { emptyList() }

    data class ScrapLengthRecalcResult(
        val updated: Int = 0,
        val skipped: Int = 0,
        val total: Int = 0,
    )

    suspend fun recalculateProductScrapLength(): ScrapLengthRecalcResult = runCatching {
        val resp = apiClient.masterApi().recalculateProductScrapLength()
        val updated = (resp["updated"] as? Number)?.toInt() ?: 0
        val skipped = (resp["skipped"] as? Number)?.toInt() ?: 0
        val total = (resp["total"] as? Number)?.toInt() ?: (updated + skipped)
        ScrapLengthRecalcResult(updated = updated, skipped = skipped, total = total)
    }.getOrElse { ScrapLengthRecalcResult() }

    suspend fun exportProductMasterCsv(filters: ProductMasterFilters): ProductCsvExportResultDto {
        val resp = apiClient.masterApi().listProducts(
            keyword = filters.keyword.takeIf { it.isNotBlank() },
            category = filters.category.takeIf { it.isNotBlank() },
            kind = filters.kind.takeIf { it.isNotBlank() },
            materialCd = filters.materialCd.takeIf { it.isNotBlank() },
            pageSize = 10000,
        )
        val products = resp.items()
        if (products.isEmpty()) {
            throw IllegalStateException("出力する製品がありません")
        }
        val body = products.map { product ->
            ProductCsvExportItemDto(
                productCd = product.productCd,
                productName = product.productName,
                unitPerBox = product.unitPerBox,
            )
        }
        return apiClient.masterApi().exportProductsCsv(body)
    }

    suspend fun loadList(kind: MasterPageKind, keyword: String, statusFilter: String = ""): Pair<List<MasterTableRow>, Int> =
        runCatching {
            val api = apiClient.masterApi()
            when (kind) {
                MasterPageKind.Product -> {
                    val resp = api.listProducts(keyword = keyword.takeIf { it.isNotBlank() }, pageSize = 5000)
                    resp.items().map { MasterTableRow(it.id, listOf(it.productCd, it.productName, it.productType, it.category, it.status, it.destinationCd)) } to resp.totalCount()
                }
                MasterPageKind.Material -> {
                    val resp = api.listMaterials(keyword = keyword.takeIf { it.isNotBlank() })
                    resp.items().map { MasterTableRow(it.id, listOf(it.materialCd, it.materialName, it.materialType, it.supplierName, it.unitPrice?.toString(), it.status?.toString())) } to resp.totalCount()
                }
                MasterPageKind.MaterialInspection -> {
                    val resp = api.listInspectionMasters(keyword = keyword.takeIf { it.isNotBlank() })
                    val list = resp.data?.list.orEmpty()
                    list.map { MasterTableRow(it.id, listOf(it.inspectionCd, it.inspectionStandard)) } to (resp.data?.total ?: list.size)
                }
                MasterPageKind.Part -> {
                    val status = statusFilter.toIntOrNull()
                    val resp = api.listParts(keyword = keyword.takeIf { it.isNotBlank() }, status = status)
                    resp.items().map { MasterTableRow(it.id, listOf(it.partCd, it.partName, it.kind, it.settlementType, it.standardPriceJpy?.toString(), it.status?.toString())) } to resp.totalCount()
                }
                MasterPageKind.Supplier -> {
                    val resp = api.listSuppliers(keyword = keyword.takeIf { it.isNotBlank() })
                    resp.items().map { MasterTableRow(it.id, listOf(it.supplierCd, it.supplierName, it.contactPerson, it.phone, it.email)) } to resp.totalCount()
                }
                MasterPageKind.Process -> {
                    val resp = api.listProcesses(keyword = keyword.takeIf { it.isNotBlank() })
                    resp.items().map { MasterTableRow(it.id, listOf(it.processCd, it.processName, it.shortName, it.category, if (it.isOutsource == true) "外注" else "社内", it.defaultCycleSec?.toString())) } to resp.totalCount()
                }
                MasterPageKind.ProcessRoute -> {
                    val resp = api.listProcessRoutes(keyword = keyword.takeIf { it.isNotBlank() })
                    resp.items().map { MasterTableRow(it.id, listOf(it.routeCd, it.routeName, it.description, if (it.isActive == true) "有効" else "無効")) } to resp.totalCount()
                }
                MasterPageKind.ProcessingFee -> {
                    val resp = api.listProcessingFees(
                        processCd = statusFilter.takeIf { it.isNotBlank() },
                        keyword = keyword.takeIf { it.isNotBlank() },
                    )
                    resp.items().map { MasterTableRow(it.id, listOf(it.processCd, it.processName, it.methodCd, it.methodName, it.unitPrice?.toString(), it.status)) } to resp.totalCount()
                }
                MasterPageKind.Customer -> {
                    val status = statusFilter.toIntOrNull()
                    val resp = api.listCustomers(keyword = keyword.takeIf { it.isNotBlank() }, status = status)
                    resp.items().map { MasterTableRow(it.id, listOf(it.customerCd, it.customerName, it.customerType, it.phone, it.status?.toString())) } to resp.totalCount()
                }
                MasterPageKind.Carrier -> {
                    val status = statusFilter.toIntOrNull()
                    val resp = api.listCarriers(keyword = keyword.takeIf { it.isNotBlank() }, status = status)
                    resp.items().map { MasterTableRow(it.id, listOf(it.carrierCd, it.carrierName, it.phone, it.status?.toString())) } to resp.totalCount()
                }
                MasterPageKind.Machine -> {
                    val resp = api.listMachines(
                        keyword = keyword.takeIf { it.isNotBlank() },
                        machineType = statusFilter.takeIf { it.isNotBlank() },
                        pageSize = 5000,
                    )
                    resp.items().map { MasterTableRow(it.id, listOf(it.machineCd, it.machineName, it.machineType, it.status)) } to resp.totalCount()
                }
                MasterPageKind.Roller -> {
                    val resp = api.listRollers(keyword = keyword.takeIf { it.isNotBlank() })
                    resp.items().map { MasterTableRow(it.id, listOf(it.rollerCd, it.rollerName, it.machineCd, it.category)) } to resp.totalCount()
                }
                MasterPageKind.Destination -> {
                    val status = statusFilter.toIntOrNull()
                    val resp = api.listDestinations(keyword = keyword.takeIf { it.isNotBlank() }, status = status)
                    resp.items().map { MasterTableRow(it.id, listOf(it.destinationCd, it.destinationName, it.customerCd, it.carrierCd, it.status?.toString())) } to resp.totalCount()
                }
                else -> emptyList<MasterTableRow>() to 0
            }
        }.getOrElse { emptyList<MasterTableRow>() to 0 }

    suspend fun loadProductsForRoute(keyword: String): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listProducts(keyword = keyword.takeIf { it.isNotBlank() }, pageSize = 500)
            .items()
            .map { (it.productCd.orEmpty()) to (it.productName.orEmpty()) }
    }.getOrElse { emptyList() }

    suspend fun loadProductRouteInfo(productCd: String): MasterProductRouteInfoDto? = runCatching {
        apiClient.masterApi().getProductRouteInfo(productCd)
    }.getOrNull()

    suspend fun loadProductRouteSteps(productCd: String, routeCd: String): List<MasterProductRouteStepDto> = runCatching {
        apiClient.masterApi().getProductRouteSteps(productCd, routeCd)
    }.getOrElse { emptyList() }

    suspend fun loadRouteSteps(routeCd: String) = runCatching {
        apiClient.masterApi().listRouteSteps(routeCd)
    }.getOrElse { emptyList() }

    suspend fun loadHolidays(destinationCd: String): List<MasterDestinationHolidayDto> = runCatching {
        apiClient.masterApi().listDestinationHolidays(destinationCd)
    }.getOrElse { emptyList() }

    suspend fun loadWorkdays(destinationCd: String): List<MasterDestinationWorkdayDto> = runCatching {
        apiClient.masterApi().listDestinationWorkdays(destinationCd)
    }.getOrElse { emptyList() }

    suspend fun loadProcessOptions(): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listProcesses(pageSize = 500).items()
            .map { (it.processCd.orEmpty()) to (it.processName.orEmpty()) }
    }.getOrElse { emptyList() }

    suspend fun deleteItem(kind: MasterPageKind, id: Int) {
        val api = apiClient.masterApi()
        when (kind) {
            MasterPageKind.Product -> api.deleteProduct(id)
            MasterPageKind.Material -> api.deleteMaterial(id)
            MasterPageKind.MaterialInspection -> api.deleteInspectionMaster(id)
            MasterPageKind.Part -> api.deletePart(id)
            MasterPageKind.Supplier -> api.deleteSupplier(id)
            MasterPageKind.Process -> api.deleteProcess(id)
            MasterPageKind.ProcessRoute -> api.deleteProcessRoute(id)
            MasterPageKind.ProcessingFee -> api.deleteProcessingFee(id)
            MasterPageKind.Customer -> api.deleteCustomer(id)
            MasterPageKind.Carrier -> api.deleteCarrier(id)
            MasterPageKind.Machine -> api.deleteMachine(id)
            MasterPageKind.Roller -> api.deleteRoller(id)
            MasterPageKind.Destination -> api.deleteDestination(id)
            else -> Unit
        }
    }

    suspend fun batchDeleteInspection(ids: List<Int>) {
        apiClient.masterApi().batchDeleteInspectionMasters(MasterBatchDeleteBodyDto(ids))
    }

    suspend fun saveForm(kind: MasterPageKind, id: Int?, fields: Map<String, String>): Boolean = runCatching {
        val api = apiClient.masterApi()
        when (kind) {
            MasterPageKind.Product -> {
                val body = buildProductBody(fields)
                if (id == null) api.createProduct(body) else api.updateProduct(id, body)
            }
            MasterPageKind.Material -> {
                val body = MasterMaterialBodyDto(
                    materialCd = fields["material_cd"].orEmpty(),
                    materialName = fields["material_name"].orEmpty(),
                    materialType = fields["material_type"],
                    standardSpec = fields["standard_spec"],
                    unit = fields["unit"],
                    supplierCd = fields["supplier_cd"],
                    unitPrice = fields["unit_price"]?.toDoubleOrNull(),
                    safetyStock = fields["safety_stock"]?.toIntOrNull(),
                    status = fields["status"]?.toIntOrNull(),
                    note = fields["note"],
                )
                if (id == null) api.createMaterial(body) else api.updateMaterial(id, body)
            }
            MasterPageKind.MaterialInspection -> {
                val body = MasterInspectionBodyDto(
                    inspectionCd = fields["inspection_cd"].orEmpty(),
                    inspectionStandard = fields["inspection_standard"].orEmpty(),
                )
                if (id == null) api.createInspectionMaster(body) else api.updateInspectionMaster(id, body)
            }
            MasterPageKind.Part -> {
                val body = MasterPartBodyDto(
                    partCd = fields["part_cd"].orEmpty(),
                    partName = fields["part_name"].orEmpty(),
                    category = fields["category"],
                    kind = fields["kind"],
                    settlementType = fields["settlement_type"],
                    uom = fields["uom"],
                    unitPrice = fields["unit_price"]?.toDoubleOrNull(),
                    materialUnitPrice = fields["material_unit_price"]?.toDoubleOrNull(),
                    currency = fields["currency"],
                    exchangeRate = fields["exchange_rate"]?.toDoubleOrNull(),
                    supplierCd = fields["supplier_cd"],
                    status = fields["status"]?.toIntOrNull() ?: 1,
                    remarks = fields["remarks"],
                )
                if (id == null) api.createPart(body) else api.updatePart(id, body)
            }
            MasterPageKind.Supplier -> {
                val body = MasterSupplierBodyDto(
                    supplierCd = fields["supplier_cd"].orEmpty(),
                    supplierName = fields["supplier_name"].orEmpty(),
                    supplierKana = fields["supplier_kana"],
                    contactPerson = fields["contact_person"],
                    phone = fields["phone"],
                    fax = fields["fax"],
                    email = fields["email"],
                    postalCode = fields["postal_code"],
                    address1 = fields["address1"],
                    address2 = fields["address2"],
                    paymentTerms = fields["payment_terms"],
                    currency = fields["currency"],
                    remarks = fields["remarks"],
                )
                if (id == null) api.createSupplier(body) else api.updateSupplier(id, body)
            }
            MasterPageKind.Process -> {
                val body = MasterProcessBodyDto(
                    processCd = fields["process_cd"].orEmpty(),
                    processName = fields["process_name"].orEmpty(),
                    shortName = fields["short_name"],
                    category = fields["category"],
                    isOutsource = fields["is_outsource"] == "true",
                    defaultCycleSec = fields["default_cycle_sec"]?.toIntOrNull(),
                    defaultYield = fields["default_yield"]?.toDoubleOrNull(),
                    capacityUnit = fields["capacity_unit"],
                    remark = fields["remark"],
                )
                if (id == null) api.createProcess(body) else api.updateProcess(id, body)
            }
            MasterPageKind.ProcessRoute -> {
                val body = MasterProcessRouteBodyDto(
                    routeCd = fields["route_cd"].orEmpty(),
                    routeName = fields["route_name"].orEmpty(),
                    description = fields["description"],
                    isActive = fields["is_active"] != "false",
                    isDefault = fields["is_default"] == "true",
                )
                if (id == null) api.createProcessRoute(body) else api.updateProcessRoute(id, body)
            }
            MasterPageKind.ProcessingFee -> {
                val body = MasterProcessingFeeBodyDto(
                    processCd = fields["process_cd"].orEmpty(),
                    methodCd = fields["method_cd"].orEmpty(),
                    methodName = fields["method_name"],
                    unitPrice = fields["unit_price"]?.toDoubleOrNull() ?: 0.0,
                    currency = fields["currency"],
                    chargeUom = fields["charge_uom"],
                    status = fields["status"],
                    remarks = fields["remarks"],
                )
                if (id == null) api.createProcessingFee(body) else api.updateProcessingFee(id, body)
            }
            MasterPageKind.Customer -> {
                val body = MasterCustomerBodyDto(
                    customerCd = fields["customer_cd"].orEmpty(),
                    customerName = fields["customer_name"].orEmpty(),
                    phone = fields["phone"],
                    address = fields["address"],
                    customerType = fields["customer_type"],
                    status = fields["status"]?.toIntOrNull() ?: 1,
                )
                if (id == null) api.createCustomer(body) else api.updateCustomer(id, body)
            }
            MasterPageKind.Carrier -> {
                val body = MasterCarrierBodyDto(
                    carrierCd = fields["carrier_cd"].orEmpty(),
                    carrierName = fields["carrier_name"].orEmpty(),
                    phone = fields["phone"],
                    address = fields["address"],
                    status = fields["status"]?.toIntOrNull() ?: 1,
                )
                if (id == null) api.createCarrier(body) else api.updateCarrier(id, body)
            }
            MasterPageKind.Machine -> {
                val body = MasterMachineBodyDto(
                    machineCd = fields["machine_cd"].orEmpty(),
                    machineName = fields["machine_name"].orEmpty(),
                    machineType = fields["machine_type"],
                    status = fields["status"],
                    efficiency = fields["efficiency"]?.toDoubleOrNull(),
                    remark = fields["remark"],
                )
                if (id == null) api.createMachine(body) else api.updateMachine(id, body)
            }
            MasterPageKind.Roller -> {
                val body = MasterRollerBodyDto(
                    rollerCd = fields["roller_cd"].orEmpty(),
                    rollerName = fields["roller_name"],
                    exchangeFreqQty = fields["exchange_freq_qty"]?.toIntOrNull(),
                    exchangeFreqMonth = fields["exchange_freq_month"]?.toIntOrNull(),
                    cleaningFreqMonth = fields["cleaning_freq_month"]?.toIntOrNull(),
                    category = fields["category"],
                    machineCd = fields["machine_cd"],
                    note = fields["note"],
                )
                if (id == null) api.createRoller(body) else api.updateRoller(id, body)
            }
            MasterPageKind.Destination -> {
                val body = MasterDestinationBodyDto(
                    destinationCd = fields["destination_cd"].orEmpty(),
                    destinationName = fields["destination_name"].orEmpty(),
                    customerCd = fields["customer_cd"],
                    carrierCd = fields["carrier_cd"],
                    deliveryLeadTime = fields["delivery_lead_time"]?.toIntOrNull(),
                    issueType = fields["issue_type"],
                    phone = fields["phone"],
                    address = fields["address"],
                    status = fields["status"]?.toIntOrNull() ?: 1,
                )
                if (id == null) api.createDestination(body) else api.updateDestination(id, body)
            }
            else -> return@runCatching false
        }
        true
    }.getOrElse { false }

    suspend fun addHoliday(destinationCd: String, date: String) {
        apiClient.masterApi().addDestinationHoliday(destinationCd, date)
    }

    suspend fun deleteHoliday(id: Int) {
        apiClient.masterApi().deleteDestinationHoliday(id)
    }

    suspend fun addWorkday(destinationCd: String, date: String, reason: String?) {
        apiClient.masterApi().addDestinationWorkday(destinationCd, date, reason)
    }

    suspend fun deleteWorkday(id: Int) {
        apiClient.masterApi().deleteDestinationWorkday(id)
    }

    suspend fun createRouteStep(routeCd: String, stepNo: Int, processCd: String) {
        apiClient.masterApi().createRouteStep(routeCd, MasterRouteStepBodyDto(stepNo, processCd))
    }

    suspend fun deleteRouteStep(routeCd: String, stepId: Int) {
        apiClient.masterApi().deleteRouteStep(routeCd, stepId)
    }
}
