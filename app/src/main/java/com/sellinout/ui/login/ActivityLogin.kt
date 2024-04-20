package com.sellinout.ui.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.viewModels
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.base.BaseActivity
import com.sellinout.data.model.request.LoginRequest
import com.sellinout.data.model.response.LoginResponse
import com.sellinout.databinding.LoginActivityBinding
import com.sellinout.network.ApiService
import com.sellinout.network.RetrofitClient
import com.sellinout.ui.MainActivity
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.SharePrefsKey.ACCOUNT_CODE
import com.sellinout.utils.handleResponse
import com.sellinout.utils.navigateClearStack
import com.sellinout.utils.showToast
import com.sellinout.utils.toJson
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class ActivityLogin : BaseActivity(R.layout.login_activity) {

    private val loginViewModel by viewModels<LoginViewModel>()
    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        onClickListener()
//        attachObserver()
    }

    private fun onClickListener() {
//        binding.edtUsername.setText("7535904438".toString())
//        binding.edtBaseUrl.setText("http://103.97.197.86:81/".toString())
//        binding.edtBaseUrl.setText("https://nhak.logicfirst.in/".toString()) // NEW URL
//        binding.edtPassword.setText("1234567890".toString())
        binding.btnLogin.setOnClickListener {
            if (binding.edtUsername.text.toString().isBlank()) {
                this.showToast("Please enter username")
            } else if (binding.edtPassword.text.toString().isBlank()) {
                this.showToast("Please enter password")
            } else if (binding.edtPassword.text.toString().length < 6) {
                this.showToast("Password should contain 6 character length")
            } else if (binding.edtBaseUrl.text.toString().isBlank()) {
                this.showToast("Please enter URL")
            } else if (!Patterns.WEB_URL.matcher(binding.edtBaseUrl.text.toString()).matches()) {
                this.showToast("Please enter valid URL")
            } else {
                //API CALL
                /* loginViewModel.loginApiCall(
                     LoginRequest(
                         binding.edtUsername.text.toString(),
                         binding.edtPassword.text.toString()
                     )
                 )*/
                requestDidStart()
                val apiService = RetrofitClient.getClient(binding.edtBaseUrl.text.toString().trim())
                    .create(ApiService::class.java)
                val call = apiService.loginApi(
                    LoginRequest(
                        binding.edtUsername.text.toString(),
                        binding.edtPassword.text.toString()
                    )
                )
                call.enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        requestDidFinish()
                        if (response.isSuccessful) {
                            val it = response.body()
                            if (it?.Ststus == 1) {
                                if (it.Data?.IsActive == true) {
                                    showToast("Login Success")
                                    Prefs.putString(SharePrefsKey.USER_DATA, it.Data.toJson())
                                    Prefs.putString(SharePrefsKey.USER_DATA, it.Data.toJson())
                                    Log.e("BASEURL", binding.edtBaseUrl.text.toString().trim())
                                    Prefs.putString(
                                        SharePrefsKey.STR_BASE_URL,
                                        binding.edtBaseUrl.text.toString().trim()
                                    )
                                    Log.e(
                                        "BASEURL",
                                        Prefs.getString(SharePrefsKey.STR_BASE_URL, "")
                                    )
                                    Prefs.putBoolean(SharePrefsKey.IS_GUEST_USER, false)
                                    it.Data.AccountCode?.let { it1 ->
                                        Prefs.putInt(
                                            ACCOUNT_CODE,
                                            it1
                                        )
                                    }
                                    navigateClearStack<MainActivity>()
                                } else {
                                    showToast("Account is deactivated")
                                }
                            } else {
                                showToast(it?.ErrorMessage)
                            }
                        } else {
                            showToast("Something went wrong, Please try again after sometime")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        requestDidFinish()
                        showToast("Something went wrong, Please try again after sometime")
                    }

                })
            }
        }
    }

    private fun attachObserver() {
        handleResponse(loginViewModel.loginViewModelLiveData) {
            if (it.Ststus == 1) {
                if (it.Data?.IsActive == true) {
                    this.showToast("Login Success")
                    Prefs.putString(SharePrefsKey.USER_DATA, it.Data.toJson())
                    Prefs.putString(SharePrefsKey.USER_DATA, it.Data.toJson())
                    Log.e("BASEURL", binding.edtBaseUrl.text.toString().trim())
                    Prefs.putString(
                        SharePrefsKey.STR_BASE_URL,
                        binding.edtBaseUrl.text.toString().trim()
                    )
                    Log.e("BASEURL", Prefs.getString(SharePrefsKey.STR_BASE_URL, ""))
                    Prefs.putBoolean(SharePrefsKey.IS_GUEST_USER, false)
                    it.Data.AccountCode?.let { it1 -> Prefs.putInt(ACCOUNT_CODE, it1) }
                    this.navigateClearStack<MainActivity>()
                } else {
                    this.showToast("Account is deactivated")
                }
//                this.navigateClearStack<ActivitySellInOutList>()
            } else {
                this.showToast(it.ErrorMessage)
            }
        }
    }
}