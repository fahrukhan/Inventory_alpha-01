/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory_alpha_01.ui.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.inventory_alpha_01.R;
import com.example.inventory_alpha_01.controller.Application;
import com.example.inventory_alpha_01.controller.Controller;
import com.example.inventory_alpha_01.utils.Bluetooth;
import com.example.inventory_alpha_01.utils.FileUtils;
import com.example.inventory_alpha_01.utils.SharedPreference.Name;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ScanBTCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class BluetoothActivity extends Application {
    // private BluetoothAdapter mBtAdapter;
    private TextView mEmptyList ,tvScanning, tvConnect;
    private Button btnDisconnect;
    public static final String TAG = "DeviceListActivity";

    private List<MyDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private boolean mIsActiveDisconnect = true; // 是否主动断开连接
    private Timer mDisconnectTimer = new Timer();
    private long period = 1000 * 30; // 隔多少时间更新一次
    private long lastTouchTime = System.currentTimeMillis(); // 上次接触屏幕操作的时间戳

    private Handler mHandler = new Handler();
    private boolean mScanning;

    private ProgressBar pbScanning;

    private RFIDWithUHFBLE uhf = RFIDWithUHFBLE.getInstance();
    public BluetoothAdapter mBtAdapter = null;
    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_device_list);
        setActionBar(this,getString(R.string.bluetooth_title));
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getString(R.string.bluetooth_device_not_suported), Toast.LENGTH_SHORT).show();
            finish();
        }

        init();
        checkBluetooth();

        if (mBtAdapter == null) {
            Toast.makeText(this, getString(R.string.bluetooth_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        scanLeDevice(true);
        btnDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                uhf.disconnect();
                tvConnect.setText(getString(R.string.bluetooth_dissconnected));
                controller.setBluetoothAddress("");
            }
        });
    }

    private void init() {
        controller = new Controller(this, Name.BLUETOOTH);
        mEmptyList =findViewById(R.id.empty);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        devRssiValues = new HashMap<String, Integer>();
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);

        tvConnect = findViewById(R.id.tv_connect);
        pbScanning = findViewById(R.id.pb_scanning);
        tvScanning = findViewById(R.id.tv_scanning);
        btnDisconnect = findViewById(R.id.btn_disconnect_bt);

        if (uhf.getConnectStatus()== ConnectionStatus.CONNECTED){
            tvConnect.setText(controller.getBluetoothName());
            btnDisconnect.setVisibility(View.VISIBLE);
        }else {
            tvConnect.setText(String.valueOf(uhf.getConnectStatus()));
        }
        Button cancelButton = findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> scanLeDevice(!mScanning));


        //boolean isHistoryList = getIntent().getBooleanExtra(MainActivity.SHOW_HISTORY_CONNECTED_LIST, false);
        boolean isHistoryList = false;
        if (isHistoryList) {
            cancelButton.setVisibility(View.GONE);
            List<String[]> deviceList = FileUtils.readXmlList();
            for (String[] device : deviceList) {
                MyDevice myDevice = new MyDevice(device[0], device[1]);
                addDevice(myDevice, 0);
            }
        } else { // 搜索蓝牙设备
            mEmptyList.setVisibility(View.VISIBLE);
            scanLeDevice(true);
        }

        ListView newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        uhf.init(getApplicationContext());
    }

    private void checkBluetooth(){
        if (mBtAdapter == null) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 2);
        }
    }



    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    uhf.stopScanBTDevices();
                    cancelButton.setText("Scan");
                    pbScanning.setVisibility(View.GONE);
                    tvScanning.setText("Select a Device");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            uhf.startScanBTDevices(new ScanBTCallback() {
                @Override
                public void getDevices(final BluetoothDevice bluetoothDevice, final int rssi, byte[] bytes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "扫描成功");
                            MyDevice myDevice = new MyDevice(bluetoothDevice.getAddress(), bluetoothDevice.getName());
                            addDevice(myDevice, rssi);
                        }
                    });
                }
            });
            cancelButton.setText("Cancel");
            pbScanning.setVisibility(View.VISIBLE);
            tvScanning.setText("Scanning...");
        } else {
            mScanning = false;
            uhf.stopScanBTDevices();
            cancelButton.setText("Scan");
            pbScanning.setVisibility(View.GONE);
            tvScanning.setText(getString(R.string.bluetooth_select_device));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        uhf.stopScanBTDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uhf.stopScanBTDevices();
    }

    private void addDevice(MyDevice device, int rssi) {
        boolean deviceFound = false;
        for (MyDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            if (device.getName()!=null){
                deviceList.add(device);
                mEmptyList.setVisibility(View.GONE);
            }
        }

        // 根据信号强度重新排序
        Collections.sort(deviceList, new Comparator<MyDevice>() {
            @Override
            public int compare(MyDevice device1, MyDevice device2) {
                String key1 = device1.getAddress();
                String key2 = device2.getAddress();
                int v1 = devRssiValues.get(key1);
                int v2 = devRssiValues.get(key2);
                if (v1 > v2) {
                    return -1;
                } else if (v1 < v2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        if (!deviceFound) {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyDevice device = deviceList.get(position);
            uhf.stopScanBTDevices();
            //scanLeDevice(false);
            tvConnect.setText(getString(R.string.bluetooth_connecting));

            String address = device.getAddress().trim();
            if(!TextUtils.isEmpty(address)) {
                new Bluetooth().connect(BluetoothActivity.this, device.getAddress(), new Bluetooth.Listener() {
                    @Override
                    public void message(String i) {
                        tvConnect.setText(String.valueOf(uhf.getConnectStatus()));
                        if (i.equals(String.valueOf(ConnectionStatus.CONNECTED))){
                            tvConnect.setText(controller.getBluetoothName());
                            btnDisconnect.setVisibility(View.VISIBLE);
                        }
                        else {
                            tvConnect.setText(getString(R.string.bluetooth_error_connect));
                            btnDisconnect.setVisibility(View.GONE);
                        }
                    }
                });

            } else {
                Toast.makeText(BluetoothActivity.this, getString(R.string.bluetooth_device_invalid), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onPause() {
        super.onPause();
        Log.d(TAG, "scanLeDevice==============>");
        scanLeDevice(false);
    }

    static class MyDevice {
        private String address;
        private String name;
        private int bondState;

        public MyDevice() {
        }

        public MyDevice(String address, String name) {
            this.address = address;
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBondState() {
            return bondState;
        }

        public void setBondState(int bondState) {
            this.bondState = bondState;
        }
    }

    static class DeviceAdapter extends BaseAdapter {
        Context context;
        List<MyDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<MyDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.bean_bluetooth_device, null);
            }

            MyDevice device = devices.get(position);
            final TextView tvAdd = vg.findViewById(R.id.address);
            final TextView tvName = vg.findViewById(R.id.name);

            tvName.setText(device.getName());
            tvAdd.setText(device.getAddress());

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tvName.setTextColor(Color.BLUE);
                tvAdd.setTextColor(Color.BLUE);
            }
            return vg;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.bluetooth_not_turn_on), Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }
}
