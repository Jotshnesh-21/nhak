package com.sellinout.ui.sellinout.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sellinout.data.model.response.SellInOutItemModel
import com.sellinout.databinding.AdapterStockItemLayoutBinding
import com.sellinout.ui.MainActivity
import com.sellinout.ui.cartsummary.CardSummaryActivity
import com.sellinout.utils.Const
import com.sellinout.utils.getFormattedDate
import com.sellinout.utils.getSellInOutLabel

class AdapterSellInOut(
    val context: Context,
    val list: ArrayList<SellInOutItemModel>,
    val itemClick: (SellInOutItemModel, Int) -> Unit
) : Adapter<AdapterSellInOut.ViewHolder>() {


    inner class ViewHolder(val binding: AdapterStockItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterStockItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        with(holder) {

            val date = context.getFormattedDate(model.valDate, "yyyy-M-d'T'HH:mm:ss", "dd-MM-yyyy")
            binding.txtStockDate.text = date ?: model.valDate
            binding.txtStockQty.text = model.quantity.toString()
            binding.txtStockPrice.text =
                ("${Const.CURRENCY_UNIT}").plus(" " + model.amount.toString())

            binding.txtStockSale.text = model.VoucherNo
//            getSellInOutLabel(model.vchType.toString())
            when (model.vchType.toString()) {
                "3" -> {
                    context.getColor(android.R.color.holo_red_light) //SELL IN RED COLOR
                    binding.txtStockDate.setTextColor(Color.RED)
                    binding.txtStockQty.setTextColor(Color.RED)
                    binding.txtStockPrice.setTextColor(Color.RED)
                    binding.txtStockSale.setTextColor(Color.RED)
                }

                "9" -> {
                    context.getColor(android.R.color.black) //SELL OUT WHITE
                    binding.txtStockDate.setTextColor(Color.BLACK)
                    binding.txtStockQty.setTextColor(Color.BLACK)
                    binding.txtStockPrice.setTextColor(Color.BLACK)
                    binding.txtStockSale.setTextColor(Color.BLACK)
                }

                else -> {
                    context.getColor(android.R.color.black)
                    binding.txtStockDate.setTextColor(Color.BLACK)
                    binding.txtStockQty.setTextColor(Color.BLACK)
                    binding.txtStockPrice.setTextColor(Color.BLACK)
                    binding.txtStockSale.setTextColor(Color.BLACK)
                }
            }
            binding.linMain.setOnClickListener {
                itemClick.invoke(list[position], position)
            }
        }
    }
}