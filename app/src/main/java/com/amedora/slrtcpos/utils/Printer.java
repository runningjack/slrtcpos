package com.amedora.slrtcpos.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.amedora.slrtcpos.androidserialportapi.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by USER on 2/24/2016.
 */
public  class Printer {
    private Socket mSocket = null;    //网口打印
    private SerialPort mSerialPort = null;    //串口打印
    private OutputStream mOutputStream = null;    //输出�?
    private int type = 0;

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private static final String ACTION_USB_PERMISSION = "com.amedora.slrtcpos.USB_PERMISSION";
    private PendingIntent pendingIntent;
    private UsbInterface usbInterface;
    private UsbEndpoint outEndpoint;
    private UsbDeviceConnection connection;
    private CallBack callBack;

    public interface CallBack {
        public void onSuccess(Printer printer);

        public void onFailure(String err);
    }

    private byte[] data;

    boolean isFirst = true;

    //网口打印机
    public Printer(String printerIp, CallBack callBack) {
        type = 1;
        this.callBack = callBack;
        try {
            mSocket = new Socket();
            InetSocketAddress addr = new InetSocketAddress(printerIp, 9100);
            mSocket.connect(addr, 2000);
            mSocket.setSoTimeout(2000);
            mOutputStream = mSocket.getOutputStream();
            callBack.onSuccess(this);
            commit();
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onFailure(e.getMessage());
        }
    }

    //usb打印
    public Printer(Context context, CallBack callBack) {
        type = 2;
        this.callBack = callBack;
        usbManager = (UsbManager) context
                .getSystemService(Context.USB_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context,
                0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        for (UsbDevice device : map.values()) {
            if (1155 == device.getVendorId()
                    && 33054 == device.getProductId()) {
                usbDevice = device;
            }
        }

        if (usbDevice == null) {
            callBack.onFailure("USB_PRINT_NOT_FIND");
        } else {
            if (usbManager.hasPermission(usbDevice)) {
                usbInterface = usbDevice.getInterface(0);
                outEndpoint = usbInterface.getEndpoint(1);
                connection = usbManager.openDevice(usbDevice);
                connection.claimInterface(usbInterface, true);
                callBack.onSuccess(this);
                commit();
            } else {
                context.registerReceiver(mUsbReceiver,
                        filter);
                usbManager.requestPermission(usbDevice, pendingIntent); // Application usb device access
            }
        }
    }

    //Serial Print
    public Printer(int com, String baudrate, CallBack callBack) {
        type = 3;
        this.callBack = callBack;
        try {
            int baud = integer(baudrate);
            openSerialPort(7, baud);
            callBack.onSuccess(this);
            commit();
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onFailure("SerialPortOpenFailure!");
        }
    }

    public void write(String s) {
        try {
            data = combineTowTytes(data, s.getBytes("gbk"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }

    public void writeByte(int... command) {
       data = combineTowTytes(data,integersToBytes(command));
    }

    public void writeByteArray(byte[] command) {
        data = combineTowTytes(data,command);

    }

    byte[] integersToBytes(int[] values)    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for(int i=0; i < values.length; ++i)
        {
            try{
                dos.writeInt(values[i]);
            }catch(Exception ex){

            }

        }

        return baos.toByteArray();
    }

    public void writeHex(String hexStr) {
        data = combineTowTytes(data, ConversionNumber.HexString2Bytes(hexStr));
    }


    public void writeMapHex(String mapHexStr) {

        byte[] map = ConversionNumber.HexString2Bytes(mapHexStr);

        data = combineTowTytes(data, map);

    }

   // public  void reOver();

    public void print() throws Exception {
        //reOver();
        commit();
    }


    public void writeDec(String decStr) {

        data = combineTowTytes(data,
                ConversionNumber.HexString2Bytes(ConversionNumber
                        .int2Hex(decStr)));

    }

//	public abstract void reOver();

    //	public void print() {
//		reOver();
//		commit();
//	}
    private void commit2(Byte mdata) {
        try {
            mOutputStream.write(mdata);
            mOutputStream.flush();
            if (mSocket != null)
                mSocket.close();

    }

    catch(Exception ex ){

    }

}
    private void commit() {// 执行
        try {
            if (type == 1) {
                mOutputStream.write(data);
                mOutputStream.flush();
                if (mSocket != null)
                    mSocket.close();
            } else if (type == 2) {
                connection.bulkTransfer(outEndpoint, data, data.length, 600000);
                if (connection != null)
                    connection.close();
            } else if (type == 3) {
                mOutputStream.write(data);
                mOutputStream.flush();
                closeSerialPort();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        data = null;
        System.gc();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String action = intent.getAction();

                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        usbDevice = (UsbDevice) intent
                                .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (usbDevice != null && isFirst) {
                                isFirst = false;
                                context.unregisterReceiver(mUsbReceiver);
                                usbInterface = usbDevice.getInterface(0);
                                outEndpoint = usbInterface.getEndpoint(1);
                                connection = usbManager.openDevice(usbDevice);
                                connection.claimInterface(usbInterface, true);
                                callBack.onSuccess(Printer.this);
                                commit();
                            } else {
                                System.out.println("USB DEVICE NOT FIND!");
                                throw new Exception("USB DEVICE NOT FIND!");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private byte[] combineTowTytes(byte[] bytes1, byte[] bytes2) {

        try {
            byte[] bytes3;
            if (bytes1 == null && bytes2 != null) {
                bytes3 = bytes2;
            } else if (bytes1 != null && bytes2 == null) {
                bytes3 = bytes1;
            } else if (bytes1 == null && bytes2 == null) {
                bytes3 = null;
            } else {
                bytes3 = new byte[bytes1.length + bytes2.length];
                System.arraycopy(bytes1, 0, bytes3, 0, bytes1.length);
                System.arraycopy(bytes2, 0, bytes3, bytes1.length, bytes2.length);
            }
            return bytes3;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private  void openSerialPort(int com,int baudrate) throws Exception {

        if(mSerialPort == null){
			/* Open the serial port */
            if(com<=0)
                throw new Exception("front_printer_port com number <=0!");
            String path= "/dev/ttyS7";
            System.err.println("com:"+path);
            Log.e("com",path);
            Log.e("com", "baudrate:"+baudrate);
            mSerialPort = new SerialPort(new File(path), baudrate,0);
            mOutputStream=mSerialPort.getOutputStream();
        }
    }

    private  void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
            mOutputStream = null;
        }
    }




    private int integer(String str) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return 1;
        }
    }
}
