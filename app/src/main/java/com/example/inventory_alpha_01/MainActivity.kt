package com.example.inventory_alpha_01

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.inventory_alpha_01.databinding.ActivityMainBinding
import com.example.inventory_alpha_01.ui.bluetooth.BluetoothActivity
import com.example.inventory_alpha_01.ui.read_tag.Navigation
import com.example.inventory_alpha_01.ui.read_tag.ReadTag
import com.rscja.deviceapi.RFIDWithUHFBLE

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var uhf: RFIDWithUHFBLE
    private var mBtAdapter: BluetoothAdapter? = null
    private val isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initVar()
    }

    private fun initVar() {
        uhf = RFIDWithUHFBLE.getInstance()
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBtAdapter = bluetoothManager.adapter
        binding.btnSearch.setOnClickListener(this)
        binding.btnReadTag.setOnClickListener(this)
        binding.btnCustomReadTag.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            binding.btnSearch.id -> {
                startActivity(Intent(this, BluetoothActivity::class.java))
            }
            binding.btnReadTag.id -> {
                startActivity(Intent(this, ReadTag::class.java))
            }
            binding.btnCustomReadTag.id -> {
                startActivity(Intent(this, Navigation::class.java))
            }
        }
    }



    companion object{
        private const val TAG = "MainActivity"
        private const val REQUEST_ENABLE_BT = 2
        private const val REQUEST_SELECT_DEVICE = 1
        public const val SHOW_HISTORY_CONNECTED_LIST = "showHistoryConnectedList"
        private const val RUNNING_DISCONNECT_TIMER = 10
    }
}