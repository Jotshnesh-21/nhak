package com.sellinout.ui.printscreen.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sellinout.R
import com.sellinout.databinding.AdapterPrinterLitemBinding

class AdapterPrinterList(
    val list: ArrayList<BluetoothDevice>,
    val context: Context,
    private val itemClick: (position: Int, model: BluetoothDevice) -> Unit
) : Adapter<AdapterPrinterList.ViewHolder>() {

    inner class ViewHolder(itemView: AdapterPrinterLitemBinding) :
        RecyclerView.ViewHolder(itemView.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterPrinterLitemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list.get(position)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ADAPTER", model.name.toString().plus("1212  ${model.address}"))
        }
        holder.itemView.findViewById<AppCompatTextView>(R.id.txtName).text =
            model.name.toString().plus("  ${model.address}")

        holder.itemView.findViewById<AppCompatTextView>(R.id.txtName).setOnClickListener {
            itemClick.invoke(position, model)
        }

    }
}