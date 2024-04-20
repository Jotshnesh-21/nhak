package com.sellinout.ui.printscreen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.R
import com.sellinout.data.model.request.ProductInvoiceRequest
import com.sellinout.databinding.BottomsheetPrinterListBinding
import com.sellinout.ui.printscreen.adapter.AdapterPrinterList
import com.sellinout.utils.Const
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.amountCalculation
import com.sellinout.utils.checkBluetoothConnectPermissions
import com.sellinout.utils.checkBluetoothPermissions
import com.sellinout.utils.getCurrentDate
import com.sellinout.utils.requestBluetoothConnectPermissions
import com.sellinout.utils.requestBluetoothPermissions
import java.io.OutputStream
import java.util.UUID

class BottomSheetPrinterList(
    private val itemClick: (position: Int, model: BluetoothDevice) -> Unit
) : BottomSheetDialogFragment(R.layout.bottomsheet_printer_list) {

    private lateinit var binding: BottomsheetPrinterListBinding
    private var adapter: AdapterPrinterList? = null
    private var selectedDevice: BluetoothDevice? = null
    private var list: ArrayList<BluetoothDevice> = arrayListOf()
    var bluetoothAdapter: BluetoothAdapter? = null
    var pairedDevices: Set<BluetoothDevice>? = null
    var socket: BluetoothSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = BottomsheetPrinterListBinding.inflate(layoutInflater)
        val view = binding.root
        initView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun initView() {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (requireContext().checkBluetoothPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (requireContext().checkBluetoothConnectPermissions()) {
                    listOfBluetoothDevice()
                } else {
                    requireActivity().requestBluetoothConnectPermissions()
                }
            } else {
                listOfBluetoothDevice()
            }
        } else {
            requireActivity().requestBluetoothPermissions()
        }
    }

    private fun listOfBluetoothDevice() {
        pairedDevices = if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter?.bondedDevices
        } else {
            bluetoothAdapter?.bondedDevices
        }
        list.clear()
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceAddress = device.address
            list.add(device)
        }
        setAdapterList()
    }

    private fun setAdapterList() {
        if (list.size > 0) {
            binding.rvPrinterList.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = AdapterPrinterList(list, requireActivity(), itemClick = { position, model ->
                selectedDevice = model// Get the selected BluetoothDevice
                ConnectBluetoothTask(binding).execute()
            })
            binding.rvPrinterList.adapter = adapter

        } else {
            Toast.makeText(
                requireActivity(), "No Device Found.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class ConnectBluetoothTask(private val binding: BottomsheetPrinterListBinding) :
        AsyncTask<Int, Unit, BluetoothSocket>() {

        override fun onPreExecute() {
            super.onPreExecute()
            println("running onPreExecute on ${Thread.currentThread().name}")
//            binding.displayText.text = "heavy calculation ongoing..."
        }

        override fun doInBackground(vararg params: Int?): BluetoothSocket? {
            println("running doInBackground on ${Thread.currentThread().name}")
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            val uuid: UUID =
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for Serial Port Profile (SPP)
            socket = selectedDevice?.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            return socket
//            return AsyncResult.Success(param, factorial)
        }

        override fun onPostExecute(result: BluetoothSocket?) {
            println("running onPostExecute on ${Thread.currentThread().name}")
            super.onPostExecute(result)
            if (result != null) {

                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Connected", Toast.LENGTH_SHORT).show()
                }
//                if (binding!!.edtPrintText.text.toString() != "") {

                /* val textToPrint = "Company name:xxxxxxxxxx\n" + "Address:xxxxxxx\n\n" +
                         "Order #3212\t\t Jan 21, 2024\n\n" +
                         "Item Name mix 8x7 1 20 $122\n" +
                         "Item Name mix 8x7 1 20 $122\n" +
                         "Item Name mix 8x7 1 20 $122\n" +
                         "Tot.Qty.:3\n\n" +
                         "Item Amount\t\t$366\n" +
                         "Discount\t\t$66\n" +
                         "=====================\n" +
                         "Total Amount\t\t$300\n"*/
                val textToPrint = generateTheStringForPrint()
                val outputStream: OutputStream = result.outputStream
                outputStream.write(textToPrint.toByteArray())
                outputStream.flush()

                if (socket != null) {
                    socket?.close()
                    dismiss()
                }
            }
        }
    }

    private fun generateTheStringForPrint(): String {
        val arrayList = Gson().fromJson<ArrayList<ProductInvoiceRequest>>(
            Prefs.getString(
                SharePrefsKey.CART_DATA, ""
            ), object : TypeToken<ArrayList<ProductInvoiceRequest>>() {}.type
        )

        val companyName = SharePrefsKey.getUserData().PrintName.toString()
        val address = SharePrefsKey.getUserData().Address.toString()
        val currentData = requireActivity().getCurrentDate("MMM dd, yyyy")
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            "\n\nCompany name:${companyName}\n" + "Address:${address}\n\n" + "Order #3212\t\t ${currentData}\n\n"
        )
        arrayList.forEach {
//            stringBuilder.append(it.ItemName + " " + it.ItemColor + " " + it.ItemSize + " " + it.Quantity + " " + it.Discount + " ${Const.CURRENCY_UNIT}" + it.Price + "\n")
            /*stringBuilder.append(
                it.ItemName + " "
                        + it.Quantity + " "
                        + String.format("%.2f", it.Discount).toDouble().toString()
                        + " ${Const.CURRENCY_UNIT}" + String.format("%.2f",it.Price).toDouble().toString()
                        + "\n"
            )*/
            val disAmount = amountCalculation(
                it.Quantity!!,
                it.Price!!.toDouble(),
                it.discountPercent!!.toDouble()
            )
            stringBuilder.append(
                it.ItemName + " "
                        + it.Quantity + " "
                        + String.format("%.2f", it.Price).toDouble().toString() + "-" + it.discountPercent + "% off"
                        + " ${Const.CURRENCY_UNIT}" + String.format("%.2f",disAmount).toDouble().toString()
                        + "\n"
            )
            stringBuilder.append(it.ItemColor + " " + it.ItemSize + "\n")
        }
        var itemAmount: Double? = 0.0
        var itemDiscount: Double? = 0.0
        var itemTotalAmount: Double? = 0.0
        var itemTotalQuantity: Double? = 0.0
        arrayList.forEach {
            itemAmount = itemAmount?.plus((it.Price!!*it.Quantity!!))
            itemDiscount = itemDiscount?.plus(it.Discount!!)
            itemTotalQuantity = itemTotalQuantity?.plus(it.Quantity!!)
        }
        itemTotalAmount = itemAmount?.minus(itemDiscount!!)

        stringBuilder.append(
            "Tot.Qty.:${/*arrayList.size*/itemTotalQuantity}\n\n" + "Item Amount\t\t${
                "${Const.CURRENCY_UNIT}" + String.format("%.2f", itemAmount).toDouble().toString()
            }\n" + "Discount\t\t${
                "${Const.CURRENCY_UNIT}" + String.format("%.2f", itemDiscount).toDouble().toString()
            }\n" + "==================\n" + "Total Amount\t\t${
                "${Const.CURRENCY_UNIT}" + String.format("%.2f", itemTotalAmount).toDouble()
                    .toString()
            }\n"
        )
        return stringBuilder.toString()
    }
}