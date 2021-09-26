package com.example.inventory_alpha_01.controller

import android.annotation.SuppressLint
import android.os.*
import android.text.TextUtils
import android.widget.Toast
import com.example.inventory_alpha_01.helper.DateUtils.getCurrFormatDate
import com.example.inventory_alpha_01.helper.DateUtils.DATE_FORMAT_FULL
import com.example.inventory_alpha_01.utils.Bluetooth
import com.example.inventory_alpha_01.utils.FileUtils
import com.example.inventory_alpha_01.utils.SharedPreference
import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import java.io.File
import java.lang.Exception
import java.util.*

open class UHF: Application() {
    var uhf: RFIDWithUHFBLE = RFIDWithUHFBLE.getInstance()
    lateinit var controller: Controller
    lateinit var listener: DeviceListener
    private var total: Long = 0
    private var loopFlag = false
    private var isScanning = false
    private var isRunning = false
    var mStrTime: Long = 0;

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        uhf = RFIDWithUHFBLE.getInstance()
        controller = Controller(applicationContext, SharedPreference.Name.BLUETOOTH)
        uhf.init(applicationContext)

        uhf.setKeyEventCallback { key ->
            if (KEY_EVENT){
                if (SINGLE_SHOOT){
                    startInventorySingle()
                }else{
                    startInventory()
                }
            }
        }
    }

    private fun startInventorySingle() {
        val info: UHFTAGInfo? = uhf.inventorySingleTag()
        if(info != null){
            val msg = handler.obtainMessage(FLAG_UHF_INFO)
            msg.obj = info
            handler.sendMessage(msg)
        }
    }

    fun startInventory() {
        if (uhf.connectStatus == ConnectionStatus.CONNECTED){
            if (loopFlag){
                stopInventory()
            }else{
                startThread()
            }
        }
    }

    private fun startThread() {
        if (isRunning) {
            listener.scanning(false)
            return
        }
        isRunning = true
        listener.scanning(true)
        TagThread().start()
    }

    private fun stopInventory() {
        loopFlag = false
        cancelInventoryTask()
    }

    private val mTimer = Timer()
    private var mInventoryPerMinuteTask: TimerTask? = null
    private val period = (6 * 1000 ).toLong()// 每隔多少ms
    private val path = Environment.getExternalStorageDirectory().absolutePath + File.separator + "BluetoothReader" + File.separator
    private var fileName: String? = null

    private fun inventoryPerMinute() {
        cancelInventoryTask()
        isScanning = true
        fileName = path + "battery_" + getCurrFormatDate(DATE_FORMAT_FULL) + ".txt"
        mInventoryPerMinuteTask = object : TimerTask() {
            override fun run() {
                val data: String = getCurrFormatDate(DATE_FORMAT_FULL)
                    .toString() + "\tElectricity：" + uhf.battery + "%\n"
                FileUtils.writeFile(fileName, data, true)
                inventory()
            }
        }
        mTimer.schedule(mInventoryPerMinuteTask, 0, period)
    }

    private fun cancelInventoryTask() {
        if (mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask?.cancel()
            mInventoryPerMinuteTask = null
        }
    }


    inner class TagThread : Thread() {
        override fun run() {
            val msg: Message = handler.obtainMessage(FLAG_START)
            if (uhf.startInventoryTag()) {
                loopFlag = true
                isScanning = true
                mStrTime = System.currentTimeMillis()
                msg.arg1 = FLAG_SUCCESS
            } else {
                msg.arg1 = FLAG_FAIL
            }
            handler.sendMessage(msg)
            isRunning = false //执行完成设置成false
            var startTime = System.currentTimeMillis()
            while (loopFlag) {
                val list = getUHFInfo()
                if (list?.isEmpty() == true || list == null) {
                    SystemClock.sleep(1)
                } else {
                    handler.sendMessage(handler.obtainMessage(FLAG_UHF_INFO_LIST, list))
                }
                if (System.currentTimeMillis() - startTime > 100) {
                    startTime = System.currentTimeMillis()
                    handler.sendEmptyMessage(FLAG_UPDATE_TIME)
                }
            }
            stopInventory()
        }
    }

    private fun addEPCToList(list: List<UHFTAGInfo>) {

        for (i in list.indices){
            val uhfTagInfo = list[i]
            if (!TextUtils.isEmpty(uhfTagInfo.epc)){
                listener.dataScanning(uhfTagInfo.epc, list)
            }
        }
    }

    private fun addEPCToList(uhfTagInfo: UHFTAGInfo) {
        listener.singleScanning(uhfTagInfo.epc)
    }

    private fun inventory() {
        mStrTime = System.currentTimeMillis()
        val info = uhf.inventorySingleTag()
        if (info != null) {
            val msg = handler.obtainMessage(FLAG_UHF_INFO)
            msg.obj = info
            handler.sendMessage(msg)
        }
        handler.sendEmptyMessage(FLAG_UPDATE_TIME)
    }

    private fun clearData() {
        total = 0
    }

    protected open fun getDeviceStatus(listener: DeviceListener?) {
        this.listener = listener!!
    }

    interface DeviceListener{
        fun batteryStatus(battery: Int) {}
        fun temperatureStatus(temperature: Int) {}
        fun connectionStatus(connection: String?) {}
        fun scanning(scanStatus: Boolean) {}
        fun dataScanning(tag: String, data: Any) {}
        fun singleScanning(tag: String?) {}
    }

    inner class ConnectStatus : Bluetooth.IConnectStatus {
        override fun getStatus(connectionStatus: ConnectionStatus) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                if (!loopFlag) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                loopFlag = false
                isScanning = false
            }
        }
    }

    private val handler: Handler = object : Handler(Looper.myLooper()!!) {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                FLAG_STOP -> {
                    if (msg.arg1 == FLAG_SUCCESS) {
                    } else {
                        Toast.makeText(applicationContext,
                            "Inventory stop fail",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                FLAG_UHF_INFO_LIST -> {
                    clearData()
                    val list = msg.obj as List<UHFTAGInfo>
                    addEPCToList(list)
                }
                FLAG_START -> {
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //开始读取标签成功
                    } else {
                    }
                }
                FLAG_UPDATE_TIME -> {
                    var useTime: Float = (System.currentTimeMillis() - mStrTime) / 1000.0f
                }
                FLAG_UHF_INFO -> {
                    val info = msg.obj as UHFTAGInfo
                    addEPCToList(info)

                }
            }
        }
    }

    @Synchronized
    private fun getUHFInfo(): List<UHFTAGInfo?>? {
        var list: List<UHFTAGInfo?>? = null
        try {
            list = uhf.readTagFromBufferList()
        } catch (e: Exception) {
            println(e.message)
        }
        return list
    }

    companion object{
        var KEY_EVENT = false
        const val SINGLE_SHOOT = false
        const val FLAG_START = 0 //开始
        const val FLAG_STOP = 1 //停止
        const val FLAG_UPDATE_TIME = 2 // 更新时间
        const val FLAG_UHF_INFO = 3
        const val FLAG_UHF_INFO_LIST = 5
        const val FLAG_SUCCESS = 10 //成功
        const val FLAG_FAIL = 11 //失败
    }

}