package com.mylab.qrscanner.data.api

import com.mylab.qrscanner.data.model.ApiResponse
import com.mylab.qrscanner.data.model.LabItem
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @GET("api/items/{itemId}")
    suspend fun getItemById(@Path("itemId") itemId: String): Response<ApiResponse<LabItem>>
    
    @GET("api/items/verify/{qrCode}")
    suspend fun verifyItemByQRCode(@Path("qrCode") qrCode: String): Response<ApiResponse<LabItem>>
    
    @GET("api/items")
    suspend fun getAllItems(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null,
        @Query("condition") condition: String? = null
    ): Response<ApiResponse<List<LabItem>>>
    
    @POST("api/items")
    suspend fun createItem(@Body item: LabItem): Response<ApiResponse<LabItem>>
    
    @PUT("api/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body item: LabItem
    ): Response<ApiResponse<LabItem>>
    
    @DELETE("api/items/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String): Response<ApiResponse<Unit>>
    
    @GET("api/stats")
    suspend fun getStats(): Response<ApiResponse<Map<String, Any>>>
}




