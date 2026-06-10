package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.CompanyWorkCalendarBatchBodyDto
import com.example.smart_emap.data.model.CompanyWorkCalendarBatchResponse
import com.example.smart_emap.data.model.CompanyWorkCalendarDayTypeDto
import com.example.smart_emap.data.model.CompanyWorkCalendarListResponse
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterBatchDeleteBodyDto
import com.example.smart_emap.data.model.MasterCarrierBodyDto
import com.example.smart_emap.data.model.MasterCustomerBodyDto
import com.example.smart_emap.data.model.MasterCustomerDto
import com.example.smart_emap.data.model.MasterDestinationBodyDto
import com.example.smart_emap.data.model.MasterDestinationHolidayDto
import com.example.smart_emap.data.model.MasterDestinationWorkdayDto
import com.example.smart_emap.data.model.MasterInspectionBodyDto
import com.example.smart_emap.data.model.MasterInspectionDto
import com.example.smart_emap.data.model.MasterPartDto
import com.example.smart_emap.data.model.MasterMachineBodyDto
import com.example.smart_emap.data.model.MasterMaterialBodyDto
import com.example.smart_emap.data.model.MasterMaterialDto
import com.example.smart_emap.data.model.MaterialCsvExportItemDto
import com.example.smart_emap.data.model.MaterialMasterStatsDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.smart_emap.data.model.MasterPartBodyDto
import com.example.smart_emap.data.model.MasterProcessBodyDto
import com.example.smart_emap.data.model.MasterProcessRouteBodyDto
import com.example.smart_emap.data.model.MasterProcessingFeeBodyDto
import com.example.smart_emap.data.model.MasterProductBodyDto
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductCsvExportItemDto
import com.example.smart_emap.data.model.ProductCsvExportResultDto
import com.example.smart_emap.data.model.ProductMasterStatsDto
import com.example.smart_emap.data.model.MasterProcessOptionDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto
import com.example.smart_emap.data.model.MasterProductRouteMachineBodyDto
import com.example.smart_emap.data.model.MasterProductRouteStepBulkItemDto
import com.example.smart_emap.data.model.MasterProductRouteStepBulkMachineDto
import com.example.smart_emap.data.model.MasterProductRouteStepDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterRollerBodyDto
import com.example.smart_emap.data.model.MasterProcessRouteDto
import com.example.smart_emap.data.model.MasterRouteStepBodyDto
import com.example.smart_emap.data.model.MasterRouteStepOrderItemDto
import com.example.smart_emap.data.model.MasterRouteStepUpdateDto
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

    suspend fun loadProductsForRoute(
        keyword: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): Pair<List<Pair<String, String>>, Int> = runCatching {
        val resp = apiClient.masterApi().listProducts(
            keyword = keyword.takeIf { it.isNotBlank() },
            page = page,
            pageSize = pageSize,
        )
        resp.items().map { (it.productCd.orEmpty()) to (it.productName.orEmpty()) } to resp.totalCount()
    }.getOrElse { emptyList<Pair<String, String>>() to 0 }

    suspend fun loadProductRouteInfo(productCd: String): MasterProductRouteInfoDto? = runCatching {
        apiClient.masterApi().getProductRouteInfo(productCd)
    }.getOrNull()

    suspend fun loadProductRouteSteps(productCd: String, routeCd: String): List<MasterProductRouteStepDto> = runCatching {
        apiClient.masterApi().getProductRouteSteps(productCd, routeCd)
    }.getOrElse { emptyList() }

    suspend fun loadProcessesForProductRoute(): List<MasterProcessOptionDto> = runCatching {
        apiClient.masterApi().listProcessesForProductRoute()
    }.getOrElse { emptyList() }

    suspend fun loadAllMachinesForRoute(): List<MasterMachineFullDto> = runCatching {
        apiClient.masterApi().listMachines(pageSize = 5000).items()
    }.getOrElse { emptyList() }

    suspend fun saveProductRouteStepsBulk(steps: List<MasterProductRouteStepBulkItemDto>) {
        apiClient.masterApi().saveProductRouteStepsBulk(steps)
    }

    suspend fun createProductRouteMachine(body: MasterProductRouteMachineBodyDto): Int? = runCatching {
        apiClient.masterApi().createProductRouteMachine(body).id
    }.getOrNull()

    suspend fun updateProductRouteMachine(id: Int, body: MasterProductRouteMachineBodyDto) {
        apiClient.masterApi().updateProductRouteMachine(id, body)
    }

    suspend fun deleteProductRouteMachine(id: Int) {
        apiClient.masterApi().deleteProductRouteMachine(id)
    }

    suspend fun loadRouteSteps(routeCd: String) = runCatching {
        apiClient.masterApi().listRouteSteps(routeCd)
    }.getOrElse { emptyList() }

    suspend fun loadHolidays(destinationCd: String): List<MasterDestinationHolidayDto> = runCatching {
        apiClient.masterApi().listDestinationHolidays(destinationCd)
    }.getOrElse { emptyList() }

    suspend fun loadWorkdays(destinationCd: String): List<MasterDestinationWorkdayDto> = runCatching {
        apiClient.masterApi().listDestinationWorkdays(destinationCd)
    }.getOrElse { emptyList() }

    suspend fun loadCompanyWorkCalendar(startDate: String, endDate: String): CompanyWorkCalendarListResponse? =
        runCatching {
            apiClient.masterApi().listCompanyWorkCalendar(startDate, endDate)
        }.getOrNull()

    suspend fun loadCompanyWorkCalendarDayTypes(): List<CompanyWorkCalendarDayTypeDto> = runCatching {
        apiClient.masterApi().listCompanyWorkCalendarDayTypes()
    }.getOrElse {
        listOf(
            CompanyWorkCalendarDayTypeDto("national_holiday", "祝日"),
            CompanyWorkCalendarDayTypeDto("company_holiday", "会社休"),
            CompanyWorkCalendarDayTypeDto("paid_leave", "有給"),
            CompanyWorkCalendarDayTypeDto("extra_workday", "臨時出勤"),
        )
    }

    suspend fun batchCreateCompanyWorkCalendar(dates: List<String>, dayType: String, name: String?): CompanyWorkCalendarBatchResponse? =
        runCatching {
            apiClient.masterApi().batchCreateCompanyWorkCalendar(
                CompanyWorkCalendarBatchBodyDto(dates = dates, dayType = dayType, name = name?.trim()?.ifBlank { null }),
            )
        }.getOrNull()

    suspend fun deleteCompanyWorkCalendarEntry(id: Int) {
        apiClient.masterApi().deleteCompanyWorkCalendarEntry(id)
    }

    /** 会社稼働カレンダーに基づく月間稼働日数（API 失敗時は月〜金） */
    suspend fun loadScheduledWorkdaysForMonth(yearMonth: String): Int {
        val trimmed = yearMonth.trim()
        if (!Regex("""\d{4}-\d{2}""").matches(trimmed)) return 20
        val parts = trimmed.split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return 20
        val start = LocalDate.of(parts[0], parts[1], 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val count = loadCompanyWorkCalendar(start.format(fmt), end.format(fmt))
            ?.data?.scheduledWorkdayCount
        return count?.coerceAtLeast(1) ?: weekdayFallbackForMonth(trimmed)
    }

    private fun weekdayFallbackForMonth(yearMonth: String): Int {
        val trimmed = yearMonth.trim()
        if (!Regex("""\d{4}-\d{2}""").matches(trimmed)) return 20
        val parts = trimmed.split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return 20
        val start = LocalDate.of(parts[0], parts[1], 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        var count = 0
        var cursor = start
        while (!cursor.isAfter(end)) {
            if (cursor.dayOfWeek != DayOfWeek.SATURDAY && cursor.dayOfWeek != DayOfWeek.SUNDAY) count++
            cursor = cursor.plusDays(1)
        }
        return count.coerceAtLeast(1)
    }

    suspend fun loadScheduledWorkdaysForDateIso(dateIso: String): Int {
        val date = dateIso.trim().take(10)
        if (date.length < 7) return 20
        return loadScheduledWorkdaysForMonth(date.take(7))
    }

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
                    defaultCycleSec = fields["default_cycle_sec"]?.toDoubleOrNull(),
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

    suspend fun loadProcessRoutes(
        keyword: String,
        page: Int,
        pageSize: Int,
    ): Pair<List<MasterProcessRouteDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listProcessRoutes(
            keyword = keyword.takeIf { it.isNotBlank() },
            page = page,
            pageSize = pageSize,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<MasterProcessRouteDto>() to 0 }

    suspend fun getProcessRouteByCd(routeCd: String): MasterProcessRouteDto? = runCatching {
        apiClient.masterApi().getProcessRouteByCd(routeCd)
    }.getOrNull()

    suspend fun saveProcessRouteMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        val body = MasterProcessRouteBodyDto(
            routeCd = fields["route_cd"].orEmpty().trim(),
            routeName = fields["route_name"].orEmpty().trim(),
            description = fields["description"]?.trim()?.takeIf { it.isNotEmpty() },
            isActive = fields["is_active"] != "false",
            isDefault = fields["is_default"] == "true",
        )
        if (id == null) apiClient.masterApi().createProcessRoute(body)
        else apiClient.masterApi().updateProcessRoute(id, body)
        true
    }.getOrElse { false }

    suspend fun deleteProcessRouteMaster(id: Int) {
        apiClient.masterApi().deleteProcessRoute(id)
    }

    suspend fun loadProcessDetailsMap(): Map<String, com.example.smart_emap.data.model.MasterProcessDto> = runCatching {
        apiClient.masterApi().listProcesses(pageSize = 500).items()
            .filter { !it.processCd.isNullOrBlank() }
            .associateBy { it.processCd.orEmpty() }
    }.getOrElse { emptyMap() }

    suspend fun createRouteStep(routeCd: String, fields: Map<String, String>) {
        val body = MasterRouteStepBodyDto(
            stepNo = fields["step_no"]?.toIntOrNull() ?: 1,
            processCd = fields["process_cd"].orEmpty(),
            yieldPercent = fields["yield_percent"]?.toDoubleOrNull() ?: 100.0,
            cycleSec = fields["cycle_sec"]?.toDoubleOrNull() ?: 0.0,
            remarks = fields["remarks"]?.trim()?.takeIf { it.isNotEmpty() },
        )
        apiClient.masterApi().createRouteStep(routeCd, body)
    }

    suspend fun updateRouteStep(stepId: Int, fields: Map<String, String>) {
        val body = MasterRouteStepUpdateDto(
            stepNo = fields["step_no"]?.toIntOrNull(),
            processCd = fields["process_cd"]?.takeIf { it.isNotBlank() },
            yieldPercent = fields["yield_percent"]?.toDoubleOrNull(),
            cycleSec = fields["cycle_sec"]?.toDoubleOrNull(),
            remarks = fields["remarks"]?.trim(),
        )
        apiClient.masterApi().updateRouteStep(stepId, body)
    }

    suspend fun updateRouteStepOrder(routeCd: String, steps: List<com.example.smart_emap.data.model.MasterRouteStepDto>) {
        val order = steps.mapNotNull { step ->
            val id = step.id ?: return@mapNotNull null
            val no = step.stepNo ?: return@mapNotNull null
            MasterRouteStepOrderItemDto(id, no)
        }
        apiClient.masterApi().updateRouteStepOrder(routeCd, order)
    }

    suspend fun deleteRouteStep(routeCd: String, stepId: Int) {
        apiClient.masterApi().deleteRouteStep(routeCd, stepId)
    }

    suspend fun loadAllMaterials(): List<MasterMaterialDto> = runCatching {
        apiClient.masterApi().listMaterials(pageSize = 10000).items()
    }.getOrElse { emptyList() }

    suspend fun loadMaterialMasterStats(): MaterialMasterStatsDto = runCatching {
        val all = loadAllMaterials()
        val active = all.count { it.status == 1 }
        MaterialMasterStatsDto(total = all.size, active = active, inactive = all.size - active)
    }.getOrElse { MaterialMasterStatsDto() }

    suspend fun loadSupplierOptionsForMaterial(): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listSuppliers(pageSize = 5000).items()
            .map { (it.supplierCd.orEmpty()) to (it.supplierName.orEmpty()) }
            .filter { it.first.isNotBlank() }
    }.getOrElse { emptyList() }

    suspend fun loadMaterialById(id: Int): MasterMaterialDto? = runCatching {
        apiClient.masterApi().getMaterial(id)
    }.getOrNull()

    suspend fun loadNextMaterialCd(): String = runCatching {
        val maxCd = apiClient.masterApi().getMaxMaterialCd()["max_code"] ?: 0
        ((if (maxCd > 0) maxCd else 0) + 1).toString().padStart(5, '0')
    }.getOrElse { "10001" }

    private fun buildMaterialBody(fields: Map<String, String>): MasterMaterialBodyDto {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        fun toDouble(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        fun toInt(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
        return MasterMaterialBodyDto(
            materialCd = fields["material_cd"].orEmpty().trim(),
            materialName = fields["material_name"].orEmpty().trim(),
            materialType = blankToNull(fields["material_type"]),
            standardSpec = blankToNull(fields["standard_spec"]),
            unit = blankToNull(fields["unit"]),
            diameter = toDouble(fields["diameter"]),
            thickness = toDouble(fields["thickness"]),
            length = toDouble(fields["length"]),
            supplyClassification = blankToNull(fields["supply_classification"]),
            piecesPerBundle = toInt(fields["pieces_per_bundle"]),
            usegae = blankToNull(fields["usegae"]),
            supplierCd = blankToNull(fields["supplier_cd"]),
            unitPrice = toDouble(fields["unit_price"]),
            longWeight = toDouble(fields["long_weight"]),
            singlePrice = toDouble(fields["single_price"]),
            safetyStock = toInt(fields["safety_stock"]),
            leadTime = toInt(fields["lead_time"]),
            storageLocation = blankToNull(fields["storage_location"]),
            status = toInt(fields["status"]) ?: 1,
            toleranceRange = blankToNull(fields["tolerance_range"]),
            tolerance1 = toDouble(fields["tolerance_1"]),
            tolerance2 = toDouble(fields["tolerance_2"]),
            rangeValue = blankToNull(fields["range_value"]),
            minValue = toDouble(fields["min_value"]),
            maxValue = toDouble(fields["max_value"]),
            actualValue1 = toDouble(fields["actual_value_1"]),
            actualValue2 = toDouble(fields["actual_value_2"]),
            actualValue3 = toDouble(fields["actual_value_3"]),
            representativeModel = blankToNull(fields["representative_model"]),
            note = blankToNull(fields["note"]),
        )
    }

    suspend fun saveMaterial(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        val body = buildMaterialBody(fields)
        val api = apiClient.masterApi()
        if (id == null) api.createMaterial(body) else api.updateMaterial(id, body)
        true
    }.getOrElse { false }

    suspend fun deleteMaterial(id: Int) {
        apiClient.masterApi().deleteMaterial(id)
    }

    suspend fun updateMaterialStatus(material: MasterMaterialDto, status: Int): Boolean = runCatching {
        val id = material.id ?: return@runCatching false
        val body = MasterMaterialBodyDto(
            materialCd = material.materialCd.orEmpty(),
            materialName = material.materialName.orEmpty(),
            materialType = material.materialType,
            standardSpec = material.standardSpec,
            unit = material.unit,
            diameter = material.diameter,
            thickness = material.thickness,
            length = material.length,
            supplyClassification = material.supplyClassification,
            piecesPerBundle = material.piecesPerBundle,
            usegae = material.usegae,
            supplierCd = material.supplierCd,
            unitPrice = material.unitPrice,
            longWeight = material.longWeight,
            singlePrice = material.singlePrice,
            safetyStock = material.safetyStock,
            leadTime = material.leadTime,
            storageLocation = material.storageLocation,
            status = status,
            toleranceRange = material.toleranceRange,
            tolerance1 = material.tolerance1,
            tolerance2 = material.tolerance2,
            rangeValue = material.rangeValue,
            minValue = material.minValue,
            maxValue = material.maxValue,
            actualValue1 = material.actualValue1,
            actualValue2 = material.actualValue2,
            actualValue3 = material.actualValue3,
            representativeModel = material.representativeModel,
            note = material.note,
        )
        apiClient.masterApi().updateMaterial(id, body)
        true
    }.getOrElse { false }

    suspend fun exportMaterialMasterCsv(items: List<MaterialCsvExportItemDto>): String = runCatching {
        val response = apiClient.masterApi().exportMaterialsCsv(items)
        response.string()
    }.getOrElse { throw IllegalStateException("CSVファイルの出力に失敗しました") }

    suspend fun loadInspectionMasters(
        keyword: String,
        page: Int,
        pageSize: Int,
    ): Pair<List<MasterInspectionDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listInspectionMasters(
            keyword = keyword.takeIf { it.isNotBlank() },
            page = page,
            pageSize = pageSize,
        )
        val list = resp.data?.list.orEmpty()
        list to (resp.data?.total ?: list.size)
    }.getOrElse { emptyList<MasterInspectionDto>() to 0 }

    suspend fun saveInspectionMaster(id: Int?, inspectionCd: String, inspectionStandard: String): Boolean =
        runCatching {
            val body = MasterInspectionBodyDto(inspectionCd = inspectionCd, inspectionStandard = inspectionStandard)
            if (id == null) apiClient.masterApi().createInspectionMaster(body) else apiClient.masterApi().updateInspectionMaster(id, body)
            true
        }.getOrElse { false }

    suspend fun deleteInspectionMaster(id: Int) {
        apiClient.masterApi().deleteInspectionMaster(id)
    }

    suspend fun loadParts(
        keyword: String,
        status: Int?,
        page: Int,
        pageSize: Int,
    ): Pair<List<MasterPartDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listParts(
            keyword = keyword.takeIf { it.isNotBlank() },
            status = status,
            page = page,
            pageSize = pageSize,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<MasterPartDto>() to 0 }

    suspend fun loadAllPartsForQr(): List<MasterPartDto> = runCatching {
        apiClient.masterApi().listParts(pageSize = 10000).items()
    }.getOrElse { emptyList() }

    suspend fun loadPartById(id: Int): MasterPartDto? = runCatching {
        apiClient.masterApi().getPart(id).data
    }.getOrNull()

    suspend fun savePartMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        val body = MasterPartBodyDto(
            partCd = fields["part_cd"].orEmpty().trim(),
            partName = fields["part_name"].orEmpty().trim(),
            category = blankToNull(fields["category"]),
            kind = blankToNull(fields["kind"]),
            settlementType = blankToNull(fields["settlement_type"]),
            uom = blankToNull(fields["uom"]),
            unitPrice = fields["unit_price"]?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
            materialUnitPrice = fields["material_unit_price"]?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
            currency = blankToNull(fields["currency"]),
            exchangeRate = fields["exchange_rate"]?.trim()?.takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
            supplierCd = blankToNull(fields["supplier_cd"]),
            status = fields["status"]?.toIntOrNull() ?: 1,
            remarks = blankToNull(fields["remarks"]),
        )
        if (id == null) apiClient.masterApi().createPart(body) else apiClient.masterApi().updatePart(id, body)
        true
    }.getOrElse { false }

    suspend fun deletePartMaster(id: Int) {
        apiClient.masterApi().deletePart(id)
    }

    suspend fun loadSuppliers(
        keyword: String,
        page: Int,
        pageSize: Int,
    ): Pair<List<com.example.smart_emap.data.model.MasterSupplierDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listSuppliers(
            keyword = keyword.takeIf { it.isNotBlank() },
            page = page,
            pageSize = pageSize,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<com.example.smart_emap.data.model.MasterSupplierDto>() to 0 }

    suspend fun saveSupplierMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        val body = MasterSupplierBodyDto(
            supplierCd = fields["supplier_cd"].orEmpty().trim(),
            supplierName = fields["supplier_name"].orEmpty().trim(),
            supplierKana = blankToNull(fields["supplier_kana"]),
            contactPerson = blankToNull(fields["contact_person"]),
            phone = blankToNull(fields["phone"]),
            fax = blankToNull(fields["fax"]),
            email = blankToNull(fields["email"]),
            postalCode = blankToNull(fields["postal_code"]),
            address1 = blankToNull(fields["address1"]),
            address2 = blankToNull(fields["address2"]),
            paymentTerms = blankToNull(fields["payment_terms"]),
            currency = blankToNull(fields["currency"]) ?: "JPY",
            remarks = blankToNull(fields["remarks"]),
        )
        if (id == null) apiClient.masterApi().createSupplier(body) else apiClient.masterApi().updateSupplier(id, body)
        true
    }.getOrElse { false }

    suspend fun deleteSupplierMaster(id: Int) {
        apiClient.masterApi().deleteSupplier(id)
    }

    suspend fun loadCustomers(
        keyword: String,
        status: Int? = null,
        customerType: String? = null,
    ): Pair<List<MasterCustomerDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listCustomers(
            keyword = keyword.takeIf { it.isNotBlank() },
            status = status,
            customerType = customerType?.takeIf { it.isNotBlank() },
            page = 1,
            pageSize = 5000,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<MasterCustomerDto>() to 0 }

    suspend fun saveCustomerMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        val body = MasterCustomerBodyDto(
            customerCd = fields["customer_cd"].orEmpty().trim(),
            customerName = fields["customer_name"].orEmpty().trim(),
            phone = blankToNull(fields["phone"]),
            address = blankToNull(fields["address"]),
            customerType = blankToNull(fields["customer_type"]),
            status = fields["status"]?.toIntOrNull() ?: 1,
        )
        if (id == null) apiClient.masterApi().createCustomer(body) else apiClient.masterApi().updateCustomer(id, body)
        true
    }.getOrElse { false }

    suspend fun updateCustomerStatus(id: Int, status: Int) {
        apiClient.masterApi().updateCustomerStatus(id, status)
    }

    suspend fun deleteCustomerMaster(id: Int) {
        apiClient.masterApi().deleteCustomer(id)
    }

    suspend fun loadCarriers(
        keyword: String,
        status: Int? = null,
    ): Pair<List<com.example.smart_emap.data.model.MasterCarrierDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listCarriers(
            keyword = keyword.takeIf { it.isNotBlank() },
            status = status,
            page = 1,
            pageSize = 5000,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<com.example.smart_emap.data.model.MasterCarrierDto>() to 0 }

    suspend fun saveCarrierMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        val body = MasterCarrierBodyDto(
            carrierCd = fields["carrier_cd"].orEmpty().trim(),
            carrierName = fields["carrier_name"].orEmpty().trim(),
            contactPerson = blankToNull(fields["contact_person"]),
            phone = blankToNull(fields["phone"]),
            address = blankToNull(fields["address"]),
            shippingTime = blankToNull(fields["shipping_time"]),
            reportNo = blankToNull(fields["report_no"]),
            note = blankToNull(fields["note"]),
            status = fields["status"]?.toIntOrNull() ?: 1,
        )
        if (id == null) apiClient.masterApi().createCarrier(body) else apiClient.masterApi().updateCarrier(id, body)
        true
    }.getOrElse { false }

    suspend fun updateCarrierStatus(id: Int, status: Int) {
        apiClient.masterApi().updateCarrierStatus(id, status)
    }

    suspend fun deleteCarrierMaster(id: Int) {
        apiClient.masterApi().deleteCarrier(id)
    }

    suspend fun loadProcesses(keyword: String = ""): Pair<List<com.example.smart_emap.data.model.MasterProcessDto>, Int> = runCatching {
        val resp = apiClient.masterApi().listProcesses(
            keyword = keyword.takeIf { it.isNotBlank() },
            page = 1,
            pageSize = 1000,
        )
        resp.items() to resp.totalCount()
    }.getOrElse { emptyList<com.example.smart_emap.data.model.MasterProcessDto>() to 0 }

    suspend fun saveProcessMaster(id: Int?, fields: Map<String, String>): Boolean = runCatching {
        fun blankToNull(v: String?) = v?.trim()?.takeIf { it.isNotEmpty() }
        val yieldPercent = fields["default_yield_percent"]?.toDoubleOrNull() ?: 100.0
        val body = MasterProcessBodyDto(
            processCd = fields["process_cd"].orEmpty().trim(),
            processName = fields["process_name"].orEmpty().trim(),
            shortName = blankToNull(fields["short_name"]),
            category = blankToNull(fields["category"]),
            isOutsource = fields["is_outsource"] == "true",
            defaultCycleSec = fields["default_cycle_sec"]?.toDoubleOrNull() ?: 0.0,
            defaultYield = yieldPercent / 100.0,
            capacityUnit = blankToNull(fields["capacity_unit"]) ?: "pcs",
            remark = blankToNull(fields["remark"]),
        )
        if (id == null) apiClient.masterApi().createProcess(body) else apiClient.masterApi().updateProcess(id, body)
        true
    }.getOrElse { false }

    suspend fun deleteProcessMaster(id: Int) {
        apiClient.masterApi().deleteProcess(id)
    }

    suspend fun loadAllProcessesForQr(): List<com.example.smart_emap.data.model.MasterProcessDto> = runCatching {
        apiClient.masterApi().listProcesses(page = 1, pageSize = 10000).items()
    }.getOrElse { emptyList() }

    suspend fun exportPartMasterCsv(items: List<com.example.smart_emap.data.model.PartCsvExportItemDto>): String = runCatching {
        apiClient.masterApi().exportPartsCsv(items).string()
    }.getOrElse { throw IllegalStateException("CSVファイルの出力に失敗しました") }
}
