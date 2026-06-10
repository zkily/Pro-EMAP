package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.CompanyWorkCalendarBatchBodyDto
import com.example.smart_emap.data.model.CompanyWorkCalendarBatchResponse
import com.example.smart_emap.data.model.CompanyWorkCalendarDayTypeDto
import com.example.smart_emap.data.model.CompanyWorkCalendarListResponse
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterBatchDeleteBodyDto
import com.example.smart_emap.data.model.MasterCarrierBodyDto
import com.example.smart_emap.data.model.MasterCarrierDto
import com.example.smart_emap.data.model.MasterCustomerBodyDto
import com.example.smart_emap.data.model.MasterCustomerDto
import com.example.smart_emap.data.model.MasterDestinationBodyDto
import com.example.smart_emap.data.model.MasterDestinationDto
import com.example.smart_emap.data.model.MasterDestinationHolidayDto
import com.example.smart_emap.data.model.MasterDestinationWorkdayDto
import com.example.smart_emap.data.model.MasterInspectionBodyDto
import com.example.smart_emap.data.model.MasterInspectionDto
import com.example.smart_emap.data.model.MasterListEnvelope
import com.example.smart_emap.data.model.MasterMachineBodyDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterMaterialBodyDto
import com.example.smart_emap.data.model.MasterMaterialDto
import com.example.smart_emap.data.model.MasterOptionDto
import com.example.smart_emap.data.model.MasterPartBodyDto
import com.example.smart_emap.data.model.MasterPartDto
import com.example.smart_emap.data.model.MasterProcessBodyDto
import com.example.smart_emap.data.model.MasterProcessDto
import com.example.smart_emap.data.model.MasterProcessRouteBodyDto
import com.example.smart_emap.data.model.MasterProcessRouteDto
import com.example.smart_emap.data.model.MasterProcessingFeeBodyDto
import com.example.smart_emap.data.model.MasterProcessingFeeDto
import com.example.smart_emap.data.model.MasterProductBodyDto
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.EquipmentEfficiencyListResponse
import com.example.smart_emap.data.model.ProductByProcessDto
import com.example.smart_emap.data.model.ProductMachineConfigRowDto
import com.example.smart_emap.data.model.ProductMachineConfigUpdateBody
import com.example.smart_emap.data.model.ProductProcessBomListResponse
import com.example.smart_emap.data.model.ProductProcessBomRowDto
import com.example.smart_emap.data.model.UpdateProductProcessBomBody
import com.example.smart_emap.data.model.ProductCsvExportItemDto
import com.example.smart_emap.data.model.ProductCsvExportResultDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto
import com.example.smart_emap.data.model.MasterProductRouteStepDto
import com.example.smart_emap.data.model.MasterRollerBodyDto
import com.example.smart_emap.data.model.MasterRollerDto
import com.example.smart_emap.data.model.MasterRouteStepBodyDto
import com.example.smart_emap.data.model.MasterRouteStepDto
import com.example.smart_emap.data.model.MasterRouteStepOrderItemDto
import com.example.smart_emap.data.model.MasterRouteStepUpdateDto
import com.example.smart_emap.data.model.MasterSupplierBodyDto
import com.example.smart_emap.data.model.MasterSupplierDto
import com.example.smart_emap.data.model.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MasterApiService {
    // --- existing ---
    @GET("/api/master/machines")
    suspend fun listMachines(
        @Query("keyword") keyword: String? = null,
        @Query("machine_type") machineType: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int? = null,
    ): MasterListEnvelope<MasterMachineFullDto>

    @GET("/api/master/destinations/options")
    suspend fun destinationOptions(): List<DestinationOptionDto>

    @GET("/api/master/products")
    suspend fun listProducts(
        @Query("pageSize") pageSize: Int = 9999,
        @Query("destination_cd") destinationCd: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("product_cd") productCd: String? = null,
        @Query("category") category: String? = null,
        @Query("kind") kind: String? = null,
        @Query("status") status: String? = null,
    @Query("material_cd") materialCd: String? = null,
    @Query("product_type") productType: String? = null,
    @Query("page") page: Int = 1,
    ): MasterListEnvelope<MasterProductDto>

    @GET("/api/master/products/max-cd")
    suspend fun getMaxProductCd(): Int

    @POST("/api/master/products/recalculate-scrap-length")
    suspend fun recalculateProductScrapLength(): Map<String, Any?>

    @POST("/api/master/products/export-csv")
    suspend fun exportProductsCsv(@Body body: List<ProductCsvExportItemDto>): ProductCsvExportResultDto

    @POST("/api/master/products")
    suspend fun createProduct(@Body body: MasterProductBodyDto): MasterProductDto

    @PUT("/api/master/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body body: MasterProductBodyDto): MasterProductDto

    @DELETE("/api/master/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Map<String, String>

    // --- materials ---
    @GET("/api/master/materials")
    suspend fun listMaterials(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("material_type") materialType: String? = null,
        @Query("supply_classification") supplyClassification: String? = null,
        @Query("usegae") usegae: String? = null,
        @Query("storage_location") storageLocation: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterMaterialDto>

    @GET("/api/master/materials/{id}")
    suspend fun getMaterial(@Path("id") id: Int): MasterMaterialDto

    @GET("/api/master/materials/max-cd")
    suspend fun getMaxMaterialCd(): Map<String, Int>

    @POST("/api/master/materials")
    suspend fun createMaterial(@Body body: MasterMaterialBodyDto): MasterMaterialDto

    @PUT("/api/master/materials/{id}")
    suspend fun updateMaterial(@Path("id") id: Int, @Body body: MasterMaterialBodyDto): MasterMaterialDto

    @DELETE("/api/master/materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: Int): Map<String, String>

    @POST("/api/master/materials/export-csv")
    suspend fun exportMaterialsCsv(@Body body: List<com.example.smart_emap.data.model.MaterialCsvExportItemDto>): okhttp3.ResponseBody

    // --- material inspection ---
    @GET("/api/material/inspection-master")
    suspend fun listInspectionMasters(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): ApiEnvelope<com.example.smart_emap.data.model.PagedListDto<MasterInspectionDto>>

    @POST("/api/material/inspection-master")
    suspend fun createInspectionMaster(@Body body: MasterInspectionBodyDto): ApiEnvelope<MasterInspectionDto>

    @PUT("/api/material/inspection-master/{id}")
    suspend fun updateInspectionMaster(
        @Path("id") id: Int,
        @Body body: MasterInspectionBodyDto,
    ): ApiEnvelope<MasterInspectionDto>

    @DELETE("/api/material/inspection-master/{id}")
    suspend fun deleteInspectionMaster(@Path("id") id: Int): ApiEnvelope<Any>

    @DELETE("/api/material/inspection-master/batch")
    suspend fun batchDeleteInspectionMasters(@Body body: MasterBatchDeleteBodyDto): ApiEnvelope<Any>

    // --- parts ---
    @GET("/api/master/parts")
    suspend fun listParts(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterPartDto>

    @POST("/api/master/parts")
    suspend fun createPart(@Body body: MasterPartBodyDto): ApiEnvelope<MasterPartDto>

    @PUT("/api/master/parts/{id}")
    suspend fun updatePart(@Path("id") id: Int, @Body body: MasterPartBodyDto): ApiEnvelope<MasterPartDto>

    @DELETE("/api/master/parts/{id}")
    suspend fun deletePart(@Path("id") id: Int): ApiEnvelope<Any>

    @GET("/api/master/parts/{id}")
    suspend fun getPart(@Path("id") id: Int): ApiEnvelope<MasterPartDto>

    @POST("/api/master/parts/export-csv")
    suspend fun exportPartsCsv(@Body body: List<com.example.smart_emap.data.model.PartCsvExportItemDto>): okhttp3.ResponseBody

    // --- suppliers ---
    @GET("/api/master/suppliers")
    suspend fun listSuppliers(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterSupplierDto>

    @POST("/api/master/suppliers")
    suspend fun createSupplier(@Body body: MasterSupplierBodyDto): MasterSupplierDto

    @PUT("/api/master/suppliers/{id}")
    suspend fun updateSupplier(@Path("id") id: Int, @Body body: MasterSupplierBodyDto): MasterSupplierDto

    @DELETE("/api/master/suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") id: Int): Map<String, String>

    // --- processes ---
    @GET("/api/master/processes")
    suspend fun listProcesses(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterProcessDto>

    @POST("/api/master/processes")
    suspend fun createProcess(@Body body: MasterProcessBodyDto): MasterProcessDto

    @PUT("/api/master/processes/{id}")
    suspend fun updateProcess(@Path("id") id: Int, @Body body: MasterProcessBodyDto): MasterProcessDto

    @DELETE("/api/master/processes/{id}")
    suspend fun deleteProcess(@Path("id") id: Int): Map<String, String>

    // --- process routes ---
    @GET("/api/master/process-routes")
    suspend fun listProcessRoutes(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterProcessRouteDto>

    @POST("/api/master/process-routes")
    suspend fun createProcessRoute(@Body body: MasterProcessRouteBodyDto): MasterProcessRouteDto

    @PUT("/api/master/process-routes/{id}")
    suspend fun updateProcessRoute(@Path("id") id: Int, @Body body: MasterProcessRouteBodyDto): MasterProcessRouteDto

    @DELETE("/api/master/process-routes/{id}")
    suspend fun deleteProcessRoute(@Path("id") id: Int): Map<String, String>

    @GET("/api/master/process-routes/by-cd/{routeCd}")
    suspend fun getProcessRouteByCd(@Path("routeCd") routeCd: String): MasterProcessRouteDto

    @GET("/api/master/process-routes/by-cd/{routeCd}/steps")
    suspend fun listRouteSteps(@Path("routeCd") routeCd: String): List<MasterRouteStepDto>

    @POST("/api/master/process-routes/by-cd/{routeCd}/steps")
    suspend fun createRouteStep(
        @Path("routeCd") routeCd: String,
        @Body body: MasterRouteStepBodyDto,
    ): MasterRouteStepDto

    @PUT("/api/master/process-routes/steps/{stepId}")
    suspend fun updateRouteStep(
        @Path("stepId") stepId: Int,
        @Body body: MasterRouteStepUpdateDto,
    ): MasterRouteStepDto

    @PUT("/api/master/process-routes/by-cd/{routeCd}/steps/order")
    suspend fun updateRouteStepOrder(
        @Path("routeCd") routeCd: String,
        @Body body: List<MasterRouteStepOrderItemDto>,
    ): Map<String, String>

    @DELETE("/api/master/process-routes/by-cd/{routeCd}/steps/{stepId}")
    suspend fun deleteRouteStep(
        @Path("routeCd") routeCd: String,
        @Path("stepId") stepId: Int,
    ): Map<String, String>

    // --- product process routes ---
    @GET("/api/master/product/process/routes/{productCd}")
    suspend fun getProductRouteInfo(@Path("productCd") productCd: String): MasterProductRouteInfoDto

    @GET("/api/master/product/process/routes/{productCd}/{routeCd}")
    suspend fun getProductRouteSteps(
        @Path("productCd") productCd: String,
        @Path("routeCd") routeCd: String,
    ): List<MasterProductRouteStepDto>

    // --- processing fees ---
    @GET("/api/master/process-processing-fees")
    suspend fun listProcessingFees(
        @Query("process_cd") processCd: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterProcessingFeeDto>

    @POST("/api/master/process-processing-fees")
    suspend fun createProcessingFee(@Body body: MasterProcessingFeeBodyDto): ApiEnvelope<MasterProcessingFeeDto>

    @PUT("/api/master/process-processing-fees/{id}")
    suspend fun updateProcessingFee(
        @Path("id") id: Int,
        @Body body: MasterProcessingFeeBodyDto,
    ): ApiEnvelope<MasterProcessingFeeDto>

    @DELETE("/api/master/process-processing-fees/{id}")
    suspend fun deleteProcessingFee(@Path("id") id: Int): ApiEnvelope<Any>

    // --- customers ---
    @GET("/api/master/customers")
    suspend fun listCustomers(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterCustomerDto>

    @POST("/api/master/customers")
    suspend fun createCustomer(@Body body: MasterCustomerBodyDto): MasterCustomerDto

    @PUT("/api/master/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: Int, @Body body: MasterCustomerBodyDto): MasterCustomerDto

    @DELETE("/api/master/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: Int): Map<String, String>

    // --- carriers ---
    @GET("/api/master/carriers")
    suspend fun listCarriers(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterCarrierDto>

    @POST("/api/master/carriers")
    suspend fun createCarrier(@Body body: MasterCarrierBodyDto): MasterCarrierDto

    @PUT("/api/master/carriers/{id}")
    suspend fun updateCarrier(@Path("id") id: Int, @Body body: MasterCarrierBodyDto): MasterCarrierDto

    @DELETE("/api/master/carriers/{id}")
    suspend fun deleteCarrier(@Path("id") id: Int): Map<String, String>

    // --- machines (CRUD below uses same listMachines above) ---
    @POST("/api/master/machines")
    suspend fun createMachine(@Body body: MasterMachineBodyDto): MasterMachineFullDto

    @PUT("/api/master/machines/{id}")
    suspend fun updateMachine(@Path("id") id: Int, @Body body: MasterMachineBodyDto): MasterMachineFullDto

    @DELETE("/api/master/machines/{id}")
    suspend fun deleteMachine(@Path("id") id: Int): Map<String, String>

    // --- rollers ---
    @GET("/api/master/roller-master")
    suspend fun listRollers(
        @Query("keyword") keyword: String? = null,
        @Query("machine_cd") machineCd: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterRollerDto>

    @POST("/api/master/roller-master")
    suspend fun createRoller(@Body body: MasterRollerBodyDto): MasterRollerDto

    @PUT("/api/master/roller-master/{id}")
    suspend fun updateRoller(@Path("id") id: Int, @Body body: MasterRollerBodyDto): MasterRollerDto

    @DELETE("/api/master/roller-master/{id}")
    suspend fun deleteRoller(@Path("id") id: Int): Map<String, String>

    // --- destinations ---
    @GET("/api/master/destinations")
    suspend fun listDestinations(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): MasterListEnvelope<MasterDestinationDto>

    @POST("/api/master/destinations")
    suspend fun createDestination(@Body body: MasterDestinationBodyDto): MasterDestinationDto

    @PUT("/api/master/destinations/{id}")
    suspend fun updateDestination(@Path("id") id: Int, @Body body: MasterDestinationBodyDto): MasterDestinationDto

    @DELETE("/api/master/destinations/{id}")
    suspend fun deleteDestination(@Path("id") id: Int): Map<String, String>

    @GET("/api/master/destinations/holidays")
    suspend fun listDestinationHolidays(
        @Query("destination_cd") destinationCd: String,
    ): List<MasterDestinationHolidayDto>

    @POST("/api/master/destinations/holidays")
    suspend fun addDestinationHoliday(
        @Query("destination_cd") destinationCd: String,
        @Query("holiday_date") holidayDate: String,
    ): MasterDestinationHolidayDto

    @DELETE("/api/master/destinations/holidays/{id}")
    suspend fun deleteDestinationHoliday(@Path("id") id: Int): Map<String, String>

    @GET("/api/master/destinations/workdays")
    suspend fun listDestinationWorkdays(
        @Query("destination_cd") destinationCd: String,
    ): List<MasterDestinationWorkdayDto>

    @POST("/api/master/destinations/workdays")
    suspend fun addDestinationWorkday(
        @Query("destination_cd") destinationCd: String,
        @Query("work_date") workDate: String,
        @Query("reason") reason: String? = null,
    ): MasterDestinationWorkdayDto

    @DELETE("/api/master/destinations/workdays/{id}")
    suspend fun deleteDestinationWorkday(@Path("id") id: Int): Map<String, String>

    @GET("/api/master/company-work-calendar/day-types")
    suspend fun listCompanyWorkCalendarDayTypes(): List<CompanyWorkCalendarDayTypeDto>

    @GET("/api/master/company-work-calendar")
    suspend fun listCompanyWorkCalendar(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): CompanyWorkCalendarListResponse

    @POST("/api/master/company-work-calendar/batch")
    suspend fun batchCreateCompanyWorkCalendar(@Body body: CompanyWorkCalendarBatchBodyDto): CompanyWorkCalendarBatchResponse

    @DELETE("/api/master/company-work-calendar/{id}")
    suspend fun deleteCompanyWorkCalendarEntry(@Path("id") id: Int): Map<String, String>

    @GET("/api/master/carriers/options")
    suspend fun carrierOptions(): List<MasterOptionDto>

    @GET("/api/master/customers/options")
    suspend fun customerOptions(): List<MasterOptionDto>

    @GET("/api/master/product/process/routes/products-by-process")
    suspend fun listProductsByProcess(
        @Query("process_cd") processCd: String,
    ): List<ProductByProcessDto>

    @GET("/api/master/product-machine-config")
    suspend fun listProductMachineConfig(
        @Query("limit") limit: Int = 99999,
    ): MasterListEnvelope<ProductMachineConfigRowDto>

    @PUT("/api/master/product-machine-config/{id}")
    suspend fun updateProductMachineConfig(
        @Path("id") id: Int,
        @Body body: ProductMachineConfigUpdateBody,
    ): ProductMachineConfigRowDto

    @GET("/api/master/product-process-bom")
    suspend fun listProductProcessBom(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("sort_by") sortBy: String? = "product_name",
        @Query("sort_order") sortOrder: String? = "asc",
    ): ProductProcessBomListResponse

    @PUT("/api/master/product-process-bom/{productCd}")
    suspend fun updateProductProcessBom(
        @Path("productCd") productCd: Int,
        @Body body: UpdateProductProcessBomBody,
    ): ProductProcessBomRowDto

    @GET("/api/master/equipment-efficiency")
    suspend fun listEquipmentEfficiencyMaster(
        @Query("processType") processType: String? = null,
        @Query("limit") limit: Int? = null,
    ): EquipmentEfficiencyListResponse
}
