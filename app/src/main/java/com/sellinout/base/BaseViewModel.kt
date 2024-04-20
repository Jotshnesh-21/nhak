package com.sellinout.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sellinout.code.Resource


abstract class BaseViewModel : ViewModel() {

    protected val _resourceLiveData = MutableLiveData<Resource<*>>()
    val resourceLiveData = _resourceLiveData as LiveData<Resource<*>>
}