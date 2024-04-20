package com.sellinout.ui.productscan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sellinout.base.BaseViewModel
import com.sellinout.code.Resource
import com.sellinout.data.model.BaseResponse
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.InvoiceNumberResponse
import com.sellinout.data.model.response.SellInOutResponse
import com.sellinout.data.selinout.SellInOutRepo
import com.sellinout.utils.launchInBackGround
import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

@HiltViewModel
class ProductScanViewModel @Inject constructor(private val sellInOutRepo: SellInOutRepo) :
    BaseViewModel() {
    private val _addToCartViewModelLiveData = MutableLiveData<Resource<BaseResponse>>()
    val addToCartViewModelLiveData =
        _addToCartViewModelLiveData as LiveData<Resource<BaseResponse>>

    private val _getInvoiceViewModelLiveData = MutableLiveData<Resource<InvoiceNumberResponse>>()
    val getInvoiceViewModelLiveData =
        _getInvoiceViewModelLiveData as LiveData<Resource<InvoiceNumberResponse>>

    fun addToCart(request: ArrayList<ProductInvoiceRequest>) {
        sellInOutRepo.addToCart(request)
            .launchInBackGround(viewModelScope, _addToCartViewModelLiveData)

    }

    fun getInvoiceNumber(request: SettInOutRequest) {
        sellInOutRepo.getInvoiceNumber(request)
            .launchInBackGround(viewModelScope, _getInvoiceViewModelLiveData)

    }
}