package com.sellinout.data.selinout

import com.sellinout.code.Resource
import com.sellinout.code.getRemoteFlow
import com.sellinout.data.model.BaseResponse
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.data.model.request.SettInOutDetailRequest
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.InvoiceNumberResponse
import com.sellinout.data.model.response.SellInOutDetailResponse
import com.sellinout.data.model.response.SellInOutResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts


interface SellInOutRepo {
    fun getInvoiceDetail(request: SettInOutDetailRequest): Flow<Resource<SellInOutDetailResponse>>
    fun getSellInOutItem(request: SettInOutRequest): Flow<Resource<SellInOutResponse>>
    fun addToCart(request: ArrayList<ProductInvoiceRequest>): Flow<Resource<BaseResponse>>
    fun getInvoiceNumber(request: SettInOutRequest): Flow<Resource<InvoiceNumberResponse>>
}

@ExperimentalContracts
@Singleton
class SellInOutImpl @Inject constructor(private val networkService: SellInOutNetworkService) :
    SellInOutRepo {
    override fun getInvoiceDetail(request: SettInOutDetailRequest): Flow<Resource<SellInOutDetailResponse>> {
        return getRemoteFlow {
            networkService.getInvoiceDetail(request)
        }
    }

    override fun getInvoiceNumber(request: SettInOutRequest): Flow<Resource<InvoiceNumberResponse>> {
        return getRemoteFlow {
            networkService.getInvoiceNumber(request)
        }
    }

    override fun getSellInOutItem(request: SettInOutRequest): Flow<Resource<SellInOutResponse>> {
        return getRemoteFlow {
            networkService.getSellInOutItem(request)
        }
    }

    override fun addToCart(request: ArrayList<ProductInvoiceRequest>): Flow<Resource<BaseResponse>> {
        return getRemoteFlow {
            networkService.addToCart(request)
        }
    }
}

interface SellInOutNetworkService {

    @POST("Invoice/GetSaleInSaleOut")
    suspend fun getSellInOutItem(@Body request: SettInOutRequest): Response<SellInOutResponse>

    @POST("Invoice/Post")
    suspend fun addToCart(@Body request: ArrayList<ProductInvoiceRequest>): Response<BaseResponse>

    @POST("Invoice/GetInvoice")
    suspend fun getInvoiceDetail(@Body request: SettInOutDetailRequest): Response<SellInOutDetailResponse>

    @POST("Invoice/GetInvoiceNumber")
    suspend fun getInvoiceNumber(@Body request: SettInOutRequest): Response<InvoiceNumberResponse>
}

