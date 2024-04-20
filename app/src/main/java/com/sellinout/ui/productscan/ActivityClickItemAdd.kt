package com.sellinout.ui.productscan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sellinout.R
import com.sellinout.base.BaseFragment
import com.sellinout.databinding.ActivityItemDetailBinding
import com.sellinout.ui.MainActivity
import com.sellinout.utils.Const

class ActivityClickItemAdd : BaseFragment(R.layout.activity_item_detail) {
    private lateinit var binding: ActivityItemDetailBinding

    fun newInstance(value: String?,file: String?): ActivityClickItemAdd {
        val args = Bundle()
        args.putString(Const.FILE_URL, file)
        val fragment = ActivityClickItemAdd()
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityItemDetailBinding.inflate(inflater, container, false)
        setOnClickListener()
        return binding.root
    }

    private fun setOnClickListener() {

        Glide.with(this).load(arguments?.getString(Const.FILE_URL, "").toString()).into(binding.imgProductImage)

        binding.btnAddToCart.setOnClickListener { }
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}