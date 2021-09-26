package com.example.inventory_alpha_01.ui.read_tag

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.inventory_alpha_01.adapter.ArrayMyAdapter
import com.example.inventory_alpha_01.adapter.MyAdapter
import com.example.inventory_alpha_01.controller.UHF
import com.example.inventory_alpha_01.databinding.ActivityReadTagBinding
import java.util.ArrayList

class ReadTag : UHF(), AdapterView.OnItemClickListener {
    lateinit var bind: ActivityReadTagBinding
    private lateinit var progressDialog: ProgressDialog
    lateinit var dataScan: HashSet<String>
    lateinit var arrayAdapter: ArrayMyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadTagBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        uhf.setBeep(false)
        Toast.makeText(this, "battery: ${uhf.battery}", Toast.LENGTH_SHORT).show()
        initUI()
        initDevice()
        initAdapter()
        bind.btnScan.setOnClickListener {
            startInventory()
        }
    }

    private fun initAdapter() {
        arrayAdapter = ArrayMyAdapter(this, ArrayList(), object : ArrayMyAdapter.Event {
            override fun show(myAdapter: MyAdapter?) {

            }
        })
        bind.lvTags.adapter = arrayAdapter
    }

    private fun initUI() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")

        bind.lvTags.onItemClickListener = this
        bind.bottomNavigation.visibility = View.GONE
    }

    private fun initDevice() {
        KEY_EVENT = true
        dataScan = HashSet()
        getDeviceStatus(object : DeviceListener {
            override fun dataScanning(tag: String, data: Any) {
                super.dataScanning(tag, data)
                if (!dataScan.contains(tag)){
                    dataScan.add(tag)
                    val adapter = MyAdapter(tag, tag, null, null, null)
                    adapter.strListType = null
                    arrayAdapter.add(adapter)
                    arrayAdapter.notifyDataSetChanged()
                    bind.tvCount.text = "${arrayAdapter.count}"
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        progressDialog.dismiss()
        KEY_EVENT = true
    }

    fun refreshAdapter(){
        arrayAdapter.clear()
        dataScan.clear()
        bind.tvCount.text = "0"
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        //Toast.makeText(this, getAdapter().getDataList().get(i).getStrKey(), Toast.LENGTH_SHORT).show();
    }
}