package com.usbprinter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity implements View.OnClickListener {

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbInterface mInterface;
    private UsbEndpoint mEndPoint;
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static Boolean forceCLaim = true;
    private static int vendorID=0;
    HashMap<String, UsbDevice> mDeviceList;
    Iterator<UsbDevice> mDeviceIterator;
    int mdataLength=0;
    EditText data;
    Button print,cut;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mDeviceList = mUsbManager.getDeviceList();
        mDeviceIterator = mDeviceList.values().iterator();

        print = (Button) findViewById(R.id.buttonPrint);
        cut = (Button) findViewById(R.id.cutBtn);
        data = (EditText) findViewById(R.id.m_data);
        print.setOnClickListener(this);
        cut.setOnClickListener(this);

        Toast.makeText(this, "Device List Size: " + String.valueOf(mDeviceList.size()), Toast.LENGTH_SHORT).show();
        TextView textView = (TextView) findViewById(R.id.deviceCount);
        String usbDevice = "";
        //This is just testing what devices are connected
        while (mDeviceIterator.hasNext())
        {
            UsbDevice usbDevice1 = mDeviceIterator.next();
            usbDevice += "\n" +
                    "DeviceID: " + usbDevice1.getDeviceId() + "\n" +
                    "DeviceName: " + usbDevice1.getDeviceName() + "\n" +
                    "DeviceClass: " + usbDevice1.getDeviceClass() + " - " + translateDeviceClass(usbDevice1.getDeviceClass()) + "\n" +
                    "DeviceSubClass: " + usbDevice1.getDeviceSubclass() + "\n" +
                    "VendorID: " + usbDevice1.getVendorId() + "\n" +
                    "ProductID: " + usbDevice1.getProductId() + "\n";

            vendorID = usbDevice1.getVendorId();
            int interfaceCount = usbDevice1.getInterfaceCount();
            Toast.makeText(this, "INTERFACE COUNT: " + String.valueOf(interfaceCount), Toast.LENGTH_SHORT).show();

            mDevice = usbDevice1;


            if (mDevice == null)
            {
                Toast.makeText(this, "mDevice is null", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "mDevice is not null", Toast.LENGTH_SHORT).show();
            }
          //  textView.setText(usbDevice);
            if (vendorID == 6868)
            {
                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);
                mUsbManager.requestPermission(mDevice, mPermissionIntent);
            }
        }

        if (mDevice == null)
        {
            Toast.makeText(this, "mDevice is null", Toast.LENGTH_SHORT).show();
        } else
        {
            Toast.makeText(this, "mDevice is not null", Toast.LENGTH_SHORT).show();
        }

    }
    public void onClick(View view)
    {
      if(view==print)
      {
          mdataLength=data.length();
          //setup();
          if(mdataLength>0)
          {
              print(mConnection, mInterface);
          }
          else
          {
              AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                      MainActivity.this);
              // set title
              alertDialogBuilder.setTitle("Error");

              // set dialog message
              alertDialogBuilder
                      .setMessage("Enter Data what you want to print!")
                      .setCancelable(false)
                      .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
                              // if this button is clicked, close
                              // current activity
                              dialog.cancel();
                          }
                      });

              // create alert dialog
              AlertDialog alertDialog = alertDialogBuilder.create();

              // show it
              alertDialog.show();

          }
      }
      else if(view==cut)
      {
          cut(mConnection, mInterface);
      }
    }

     private String translateDeviceClass(int deviceClass) {
        switch (deviceClass) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "Application specific USB class";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB class for audio devices";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB class for CDC devices (communications device class)";
            case UsbConstants.USB_CLASS_COMM:
                return "USB class for communication devices";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB class for content security devices";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB class for content smart card devices";
            case UsbConstants.USB_CLASS_HID:
                return "USB class for human interface devices (for example, mice and keyboards)";
            case UsbConstants.USB_CLASS_HUB:
                return "USB class for USB hubs";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB class for mass storage devices";
            case UsbConstants.USB_CLASS_MISC:
                return "USB class for wireless miscellaneous devices";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB class indicating that the class is determined on a per-interface basis";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB class for physical devices";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB class for printers";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB class for still image devices (digital cameras)";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "Vendor specific USB class";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB class for video devices";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB class for wireless controller devices";
            default:
                return "Unknown USB class!";
        }
    }
    //Broadcast receiver to obtain permission from user for connection
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            // if(vendorID==6868)
                            {
                                //call method to set up device communication
                                mInterface = device.getInterface(0);
                                mEndPoint = mInterface.getEndpoint(1);
                                mConnection = mUsbManager.openDevice(device);
                            }

                            //setup();
                        }
                    }
                    else {
                        //Log.d("SUB", "permission denied for device " + device);
                        Toast.makeText(context, "PERMISSION DENIED FOR THIS DEVICE", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    private void print(UsbDeviceConnection connection, UsbInterface intrface){

        String test = data.getText().toString()+ "\r\n";
        byte [] testBytes = test.getBytes();

        if(intrface==null){
            Toast.makeText(this, "INTERFACE IS NULL", Toast.LENGTH_SHORT).show();
        }
        if(connection==null){
            Toast.makeText(this, "CONNECTION IS NULL", Toast.LENGTH_SHORT).show();
        }

        if(forceCLaim==null){
            Toast.makeText(this, "FORCE CLAIM IS NULL", Toast.LENGTH_SHORT).show();
        }

        connection.claimInterface(intrface, forceCLaim);
        connection.bulkTransfer(mEndPoint, testBytes, testBytes.length, 0);


    }
    private void cut(UsbDeviceConnection connection, UsbInterface intrface){


        byte [] testBytes = {27,105};
        if(intrface==null){
            Toast.makeText(this, "INTERFACE IS NULL", Toast.LENGTH_SHORT).show();
        }
        if(connection==null){
            Toast.makeText(this, "CONNECTION IS NULL", Toast.LENGTH_SHORT).show();
        }

        if(forceCLaim==null){
            Toast.makeText(this, "FORCE CLAIM IS NULL", Toast.LENGTH_SHORT).show();
        }

        connection.claimInterface(intrface, forceCLaim);
        connection.bulkTransfer(mEndPoint, testBytes, testBytes.length, 0);


    }
}