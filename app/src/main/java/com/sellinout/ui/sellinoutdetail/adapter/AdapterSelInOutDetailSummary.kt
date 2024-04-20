package com.sellinout.ui.sellinoutdetail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sellinout.data.model.response.SellInOutDetailModel
import com.sellinout.databinding.AdapterCartSummaryItemLayoutBinding
import com.sellinout.utils.Const
import com.sellinout.utils.invisible
import com.sellinout.utils.visible

class AdapterSelInOutDetailSummary(
    val isPrintScreen: Boolean?,
    val context: Context,
    val list: ArrayList<SellInOutDetailModel>,
    private val itemDelete: (item: SellInOutDetailModel, position: Int) -> Unit
) : Adapter<AdapterSelInOutDetailSummary.ViewHolder>() {


    inner class ViewHolder(val binding: AdapterCartSummaryItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterCartSummaryItemLayoutBinding.inflate(
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

//            val date = context.getFormattedDate(model.valDate, "yyyy-M-d'T'HH:mm:ss", "dd-MM-yyyy")
//            Log.e("DATEFORMAT", ">> ${date}")
            /* binding.txtStockDate.text = date ?: model.valDate
             binding.txtStockQty.text = model.quantity.toString()
             binding.txtStockPrice.text = (model.unit ?: "Rs.").plus(" " + model.amount.toString())
             binding.txtStockSale.text = getSellInOutLabel(model.vchType.toString())*/
            binding.txtStockName.text = model.ItemName
            binding.txtStockColor.text = model.ItemColor
            binding.txtStockSize.text = model.ItemSize
            binding.txtStockQty.text = model.Quantity?.toInt().toString()
            binding.txtStockQtyMrp.text = String.format("%.2f", model.Discount).toDouble().toString()
//            binding.txtStockPrice.text = (model.Unit ?: "${Const.CURRENCY_UNIT}").plus(" " + model.Price.toString())
            binding.txtStockPrice.text =
                "${Const.CURRENCY_UNIT} " + String.format("%.2f", model.Price).toDouble().toString()
            if (isPrintScreen == true) {
                binding.txtDeleteButton.invisible()
                binding.txtEditButton.invisible()
            } else {
                binding.txtEditButton.visible()
                binding.txtDeleteButton.visible()
                binding.txtDeleteButton.setOnClickListener {
                    itemDelete.invoke(list[position], position)
                }
            }
        }
    }
}