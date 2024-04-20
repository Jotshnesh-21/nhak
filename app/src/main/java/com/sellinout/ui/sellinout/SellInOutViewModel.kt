package com.sellinout.ui.sellinout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sellinout.base.BaseViewModel
import com.sellinout.code.Resource
import com.sellinout.data.model.request.SettInOutDetailRequest
import com.sellinout.data.model.request.SettInOutRequest
import com.sellinout.data.model.response.SellInOutDetailResponse
import com.sellinout.data.model.response.SellInOutResponse
import com.sellinout.data.selinout.SellInOutRepo
import com.sellinout.utils.launchInBackGround
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SellInOutViewModel @Inject constructor(private val sellInOutRepo: SellInOutRepo) :
    BaseViewModel() {
    private val _sellInOutViewModelLiveData = MutableLiveData<Resource<SellInOutResponse>>()
    val sellInOutViewModelLiveData =
        _sellInOutViewModelLiveData as LiveData<Resource<SellInOutResponse>>

    private val _sellInOutDetailViewModelLiveData = MutableLiveData<Resource<SellInOutDetailResponse>>()
    val sellInOutDetailViewModelLiveData =
        _sellInOutDetailViewModelLiveData as LiveData<Resource<SellInOutDetailResponse>>

    fun getSellInOutItem(request: SettInOutRequest) {
        sellInOutRepo.getSellInOutItem(request)
            .launchInBackGround(viewModelScope, _sellInOutViewModelLiveData)

    }
    fun getInvoiceDetail(request: SettInOutDetailRequest) {
        sellInOutRepo.getInvoiceDetail(request)
            .launchInBackGround(viewModelScope, _sellInOutDetailViewModelLiveData)

    }


}