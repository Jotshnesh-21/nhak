package com.sellinout.data.login

import com.sellinout.code.Resource
import com.sellinout.code.getRemoteFlow
import com.sellinout.data.model.request.LoginRequest
import com.sellinout.data.model.response.LoginResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts

interface LoginRepo {

    fun loginApi(request: LoginRequest): Flow<Resource<LoginResponse>>
}

@ExperimentalContracts
@Singleton
class LoginRepoImpl @Inject constructor(private val networkService: LoginNetworkService) :
    LoginRepo {
    override fun loginApi(request: LoginRequest): Flow<Resource<LoginResponse>> {
        return getRemoteFlow {
            networkService.loginApi(request)
        }
    }
}

interface LoginNetworkService {

    @POST("Login/TryLogin")
    suspend fun loginApi(@Body request: LoginRequest): Response<LoginResponse>

    @POST("Item/GetItem")
    suspend fun getItems(@Body request: LoginRequest): Response<LoginResponse>
}