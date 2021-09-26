package com.example.inventory_alpha_01.ui.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.inventory_alpha_01.MainActivity
import com.example.inventory_alpha_01.R
import com.example.inventory_alpha_01.databinding.DeviceListBinding
import com.example.inventory_alpha_01.utils.FileUtils.clearXmlList
import com.example.inventory_alpha_01.utils.FileUtils.readXmlList
import com.rscja.deviceapi.RFIDWithUHFBLE
import java.util.*

class DeviceListActivity : AppCompatActivity() {
    private lateinit var binding: DeviceListBinding
    private var mEmptyList: TextView? = null
    private var deviceList: MutableList<MyDevice>? = null
    private var deviceAdapter: DeviceAdapter? = null
    private var devRssiValues: MutableMap<String?, Int>? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mScanning = false
    private var uhf = RFIDWithUHFBLE.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DeviceListBinding.inflate(layoutInflater)
        val view = binding.root

        // Set the background of the form to be transparent
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(view)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show()
            finish()
        }
        init()
    }

    private fun init() {
        binding.close.setOnClickListener { finish() }

        devRssiValues = HashMap()
        deviceList = ArrayList()
        deviceAdapter = DeviceAdapter(deviceList!!)

        binding.btnCancel.setOnClickListener {
            if (!mScanning) {
                scanLeDevice(true)
            } else {
                finish()
            }
        }
        val btnClearHistory: Button = findViewById(R.id.btnClearHistory)
        btnClearHistory.setOnClickListener {
            clearXmlList()
            (deviceList as ArrayList<MyDevice>).clear()
            deviceAdapter!!.notifyDataSetChanged()
            mEmptyList!!.visibility = View.VISIBLE
        }
        val isHistoryList: Boolean =
            getIntent().getBooleanExtra(MainActivity.SHOW_HISTORY_CONNECTED_LIST, false)
        if (isHistoryList) {
            binding.titleDevices.text = "historycally connected devices"
            binding.empty.text = "No History"

            binding.btnCancel.visibility = View.GONE

            val deviceList: List<Array<String>> = readXmlList()
            for (device in deviceList) {
                val myDevice = MyDevice(device[0], device[1])
                addDevice(myDevice, 0)
            }
        } else { // Search for Bluetooth devices
            binding.titleDevices.text = "selectcdevice"
            binding.empty.text = "Scanning..."
            binding.btnClearHistory.visibility = View.GONE
            scanLeDevice(true)
        }
        val newDevicesListView = findViewById(R.id.new_devices) as ListView
        newDevicesListView.adapter = deviceAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener
    }

    private fun scanLeDevice(enable: Boolean) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({
                mScanning = false
                uhf.stopScanBTDevices()
                binding.btnCancel.text = "Scan"
            }, SCAN_PERIOD)
            mScanning = true
            uhf.startScanBTDevices { bluetoothDevice, rssi, bytes ->
                runOnUiThread {
                    Log.d(TAG, "扫描成功")
                    val myDevice = MyDevice(bluetoothDevice.address, bluetoothDevice.name)
                    addDevice(myDevice, rssi)
                }
            }

            binding.btnCancel.text = "Cancel"
        } else {
            mScanning = false
            uhf.stopScanBTDevices()
            binding.btnCancel.text = "Scan"
        }
    }

    override fun onStop() {
        super.onStop()
        uhf.stopScanBTDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        uhf.stopScanBTDevices()
    }

    private fun addDevice(device: MyDevice, rssi: Int) {
        var deviceFound = false
        for (listDev in deviceList!!) {
            if (listDev.address == device.address) {
                deviceFound = true
                break
            }
        }
        devRssiValues!![device.address] = rssi
        if (!deviceFound) {
            deviceList!!.add(device)
            mEmptyList!!.visibility = View.GONE
        }

        // 根据信号强度重新排序
        deviceList!!.sortWith { device1, device2 ->
            val key1 = device1.address
            val key2 = device2.address
            val v1 = devRssiValues!![key1]!!
            val v2 = devRssiValues!![key2]!!
            when {
                v1 > v2 -> {
                    -1
                }
                v1 < v2 -> {
                    1
                }
                else -> {
                    0
                }
            }
        }

        if (!deviceFound) {
            deviceAdapter!!.notifyDataSetChanged()
        }
    }

    private val mDeviceClickListener =
        OnItemClickListener { _, _, position, _ ->
            val device = deviceList!![position]
            uhf.stopScanBTDevices()
            val address = device.address!!.trim { it <= ' ' }
            if (!TextUtils.isEmpty(address)) {
                val b = Bundle()
                b.putString(BluetoothDevice.EXTRA_DEVICE, device.address)
                val result = Intent()
                result.putExtras(b)
                setResult(Activity.RESULT_OK, result)
                finish()
            } else {
                Toast.makeText(this,"Invalid bluetooth address", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "scanLeDevice==============>")
        scanLeDevice(false)
    }

//    internal inner class MyDevice {
//        var address: String? = null
//        var name: String? = null
//        var bondState = 0
//
//        constructor() {}
//        constructor(address: String?, name: String?) {
//            this.address = address
//            this.name = name
//        }
//    }

    data class MyDevice(
        var address: String? = null,
        var name: String? = null,
        var bondState: Int = 0

    ){
        constructor(address: String?, name: String?) : this() {
            this.address = address
            this.name = name
        }
    }

    internal inner class DeviceAdapter(private var devices: List<MyDevice>) :
        BaseAdapter() {
        //private var inflater: LayoutInflater = LayoutInflater.from(this@DeviceListActivity)

        override fun getCount(): Int {
            return devices.size
        }

        override fun getItem(position: Int): Any {
            return devices[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val vg: ViewGroup = convertView as ViewGroup
            val device = devices[position]
            val tvAdd = vg.findViewById<View>(R.id.address) as TextView
            val tvName = vg.findViewById<View>(R.id.name) as TextView
            val tvPaired = vg.findViewById<View>(R.id.paired) as TextView
            val tvRssi = vg.findViewById<View>(R.id.rssi) as TextView
            val rssiVal = devRssiValues!![device.address]!!.toInt()
            if (rssiVal != 0) {
                tvRssi.text = String.format("Rssi = %d", rssiVal)
                tvRssi.setTextColor(Color.BLACK)
                tvRssi.visibility = View.VISIBLE
            }
            tvName.text = device.name
            tvName.setTextColor(Color.BLACK)
            tvAdd.text = device.address
            tvAdd.setTextColor(Color.BLACK)
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.name)
                tvPaired.text = "paired"
                tvPaired.setTextColor(Color.RED)
                tvPaired.visibility = View.VISIBLE
            } else {
                tvPaired.visibility = View.GONE
            }
            return vg
        }

    }

    companion object {
        const val TAG = "DeviceListActivity"
        private const val SCAN_PERIOD: Long = 10000 //10 seconds
    }
}