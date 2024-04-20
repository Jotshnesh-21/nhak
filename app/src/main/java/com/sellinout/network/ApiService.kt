package com.sellinout.network

import com.sellinout.data.model.BaseResponse
import com.sellinout.data.model.request.LoginRequest
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.data.model.request.SettInOutDetailRequest
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.InvoiceNumberResponse
import com.sellinout.data.model.response.LoginResponse
import com.sellinout.data.model.response.SellInOutDetailResponse
import com.sellinout.data.model.response.SellInOutResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("Login/TryLogin")
    fun loginApi(@Body request: LoginRequest): Call<LoginResponse>

    @POST("Item/GetItem")
    fun getItems(@Body request: LoginRequest): Call<LoginResponse>

    @POST("Invoice/GetSaleInSaleOut")
    fun getSellInOutItem(@Body request: SettInOutRequest): Call<SellInOutResponse>

    @POST("Invoice/Post")
    fun addToCart(@Body request: ArrayList<ProductInvoiceRequest>): Call<BaseResponse>

    @POST("Invoice/GetInvoice")
    fun getInvoiceDetail(@Body request: SettInOutDetailRequest): Call<SellInOutDetailResponse>

    @POST("Invoice/GetInvoiceNumber")
    fun getInvoiceNumber(@Body request: SettInOutRequest): Call<InvoiceNumberResponse>

    @GET("Item/StockSummary/{account_code}")
    fun getStockSummary(@Path("account_code") accountCode: Int): Call<ResponseBody>
}