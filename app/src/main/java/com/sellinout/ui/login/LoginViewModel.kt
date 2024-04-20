package com.sellinout.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sellinout.base.BaseViewModel
import com.sellinout.code.Resource
import com.sellinout.data.login.LoginRepo
import com.sellinout.data.model.request.LoginRequest
import com.sellinout.data.model.response.LoginResponse
import com.sellinout.utils.launchInBackGround
import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepo: LoginRepo) : BaseViewModel() {
    private val _loginViewModelLiveData = MutableLiveData<Resource<LoginResponse>>()
    val loginViewModelLiveData = _loginViewModelLiveData as LiveData<Resource<LoginResponse>>

    fun loginApiCall(request: LoginRequest) {
        loginRepo.loginApi(request).launchInBackGround(viewModelScope,_loginViewModelLiveData)

    }
}