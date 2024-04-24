package com.sellinout.ui.cartsummary.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.data.model.response.SellInOutItemModel
import com.sellinout.databinding.AdapterCartSummaryItemLayoutBinding
import com.sellinout.databinding.AdapterStockItemLayoutBinding
import com.sellinout.utils.Const
import com.sellinout.utils.amountCalculation
import com.sellinout.utils.getFormattedDate
import com.sellinout.utils.getSellInOutLabel
import com.sellinout.utils.gone
import com.sellinout.utils.invisible
import com.sellinout.utils.visible

class AdapterCartSummary(
    val isPrintScreen: Boolean?,
    val context: Context,
    val list: ArrayList<ProductInvoiceRequest>,
    private val itemDelete: (item: ProductInvoiceRequest, position: Int) -> Unit,
    private val itemEdit: (item: ProductInvoiceRequest, position: Int) -> Unit
) : Adapter<AdapterCartSummary.ViewHolder>() {


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

//            binding.txtStockPrice.text = (model.Unit ?: "${Const.CURRENCY_UNIT}").plus(" " + model.Price.toString())

            val disAmount = amountCalculation(
                model.Quantity!!,
                model.Price!!.toDouble(),
                model.discountPercent!!.toDouble()
            )

            binding.txtStockPrice.text = if (isPrintScreen == false) {
                "${Const.CURRENCY_UNIT} " + String.format("%.2f", disAmount).toDouble().toString()
            } else {
//                "${Const.CURRENCY_UNIT} " + String.format("%.2f", model.Price).toDouble().toString()
                "${Const.CURRENCY_UNIT} " + String.format("%.2f", disAmount).toDouble().toString()
            }

//            if (isPrintScreen == true) {
//                binding.txtStockQtyMrp.text =
//                    String.format("%.2f", model.Discount).toDouble().toString()
//            } else {
//                binding.txtStockQtyMrp.text = String.format("%.2f", model.Price).toDouble()
            binding.txtStockQtyMrp.text = String.format("%.2f", disAmount).toDouble()
                .toString() + "-" + model.discountPercent + "% off"
//            }

            if ((list.size - 1) == position) {
                binding.linHFooterTable.visible()
                var totalQty = 0
                list.forEach {
                    totalQty = totalQty.plus(it.Quantity?.toInt()!!)
                }
                binding.txtTotalQtyValue.text = totalQty.toString()
            }else{
                binding.linHFooterTable.gone()
            }
            if (isPrintScreen == true) {
                binding.txtDeleteButton.invisible()
                binding.txtEditButton.invisible()
//                binding.linHFooterTable.gone()
            } else {
                binding.txtEditButton.visible()
                binding.txtDeleteButton.visible()
                binding.txtDeleteButton.setOnClickListener {
                    itemDelete.invoke(list[position], position)
                }
                binding.txtEditButton.setOnClickListener {
                    itemEdit.invoke(list[position], position)
                }

            }
        }
    }
}