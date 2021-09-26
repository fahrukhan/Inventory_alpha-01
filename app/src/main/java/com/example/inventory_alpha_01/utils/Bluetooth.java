package com.example.inventory_alpha_01.utils;

import static com.example.inventory_alpha_01.utils.SharedPreference.*;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import com.example.inventory_alpha_01.utils.SPUtils;
import com.example.inventory_alpha_01.controller.Controller;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback;

import java.util.ArrayList;
import java.util.List;

public class Bluetooth extends AppCompatActivity{

    private Context context;
    private boolean mIsActiveDisconnect = true; // 是否主动断开连接
    private static final int RECONNECT_NUM = Integer.MAX_VALUE; // 重连次数
    private int mReConnectCount = RECONNECT_NUM; // 重新连接次数

    private Controller controller;

    private BTStatus btStatus;

    private Listener listener;


//    public static boolean isScanning;
    private RFIDWithUHFBLE uhf = RFIDWithUHFBLE.getInstance();

    public BluetoothDevice mDevice = null;
    public void disconnect(boolean isActiveDisconnect) {
        mIsActiveDisconnect = isActiveDisconnect; // 主动断开为true
        uhf.disconnect();
    }

    public void connect(Context context, String deviceAddress, Listener listener) {
        this.listener = listener;
        this.context = context;
        btStatus = new BTStatus();
        controller = new Controller(context, Name.BLUETOOTH);
        uhf.disconnect();
        if (uhf.getConnectStatus() == ConnectionStatus.CONNECTING) {
//            showToast(R.string.connecting);
            listener.message(MSG.BT_CONNECTING);

        } else {
            uhf.connect(deviceAddress, btStatus);
        }
    }

    private void reConnect(String deviceAddress) {
        if (!mIsActiveDisconnect && mReConnectCount > 0) {
            connect(context,deviceAddress, listener);
            mReConnectCount--;
        }
    }

    private void saveConnectedDevice(String address, String name) {
        List<String[]> list = FileUtils.readXmlList();
        for (int k = 0; k < list.size(); k++) {
            if (address.equals(list.get(k)[0])) {
                list.remove(list.get(k));
                break;
            }
        }
        String[] strArr = new String[]{address, name};
        list.add(0, strArr);
        FileUtils.saveXmlList(list);
    }

    private boolean shouldShowDisconnected() {
        return mIsActiveDisconnect || mReConnectCount == 0;
    }

    private List<IConnectStatus> connectStatusList = new ArrayList<>();
    public interface IConnectStatus {
        void getStatus(ConnectionStatus connectionStatus);
    }

    public interface Listener{
        void message(String msg);
    }
    private String remoteBTName = "";
    private String remoteBTAdd = "";
    class BTStatus implements ConnectionStatusCallback<Object> {
        @Override
        public void getStatus(final ConnectionStatus connectionStatus, final Object device1) {
            runOnUiThread(new Runnable() {
                public void run() {
                    BluetoothDevice device = (BluetoothDevice) device1;
                    remoteBTName = "";
                    remoteBTAdd = "";
                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        remoteBTName = device.getName();
                        remoteBTAdd = device.getAddress();
                        controller.setBluetoothName(String.valueOf(device.getName()));
                        controller.setBluetoothAddress(String.valueOf(device.getAddress()));
                        listener.message(MSG.BT_CONNECTED);


//                        tvAddress.setText(String.format("%s(%s)\nconnected", remoteBTName, remoteBTAdd));
                        if (shouldShowDisconnected()) {
//                            showToast(R.string.connect_success);
//                            listener.message(device.getName());
                        }

                        // 保存已链接记录
                        if (!TextUtils.isEmpty(remoteBTAdd)) {
//                            saveConnectedDevice(remoteBTAdd, remoteBTName);
                        }


                        mIsActiveDisconnect = false;
                        mReConnectCount = RECONNECT_NUM;
                    }
                    else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                        if (device != null) {
                            remoteBTName = device.getName();
                            remoteBTAdd = device.getAddress();
                            if (shouldShowDisconnected())
//                            tvAddress.setText(String.format("%s(%s)\ndisconnected", remoteBTName, remoteBTAdd));
                                listener.message(MSG.BT_DISCONNECT);
                        }
                        else {
                            if (shouldShowDisconnected())
//                            tvAddress.setText("disconnected");
                                listener.message(MSG.BT_DISCONNECT);
                        }
                        if (shouldShowDisconnected())
//                            showToast(R.string.disconnect);
                            listener.message(MSG.BT_DISCONNECT);

                        boolean reconnect = SPUtils.getInstance(context).getSPBoolean(SPUtils.AUTO_RECONNECT, false);
                        if (mDevice != null && reconnect) {
                            reConnect(mDevice.getAddress()); // 重连
                        }
                    }

                    for (IConnectStatus iConnectStatus : connectStatusList) {
                        if (iConnectStatus != null) {
                            iConnectStatus.getStatus(connectionStatus);
                        }
                    }
                }
            });
        }
    }
}
