package com.mertcan.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class receiverActivity extends AppCompatActivity {
    int TCP_PORT=9922;
    ArrayList<Client> clients;
    UsbDevice device;
    byte durum=0;
    UsbDeviceConnection usbConnection;
    boolean dondur=true;
    BroadcastReceiver receiver;
    ServerSocket ss=null;
    UsbSerialDevice serial;
    @Override
    public void onDestroy(){
        dondur=false;
        usbConnection.close();
        serial.close();
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(receiver!=null)
        unregisterReceiver(receiver);

        super.onDestroy();
    }
    public void ackapa(byte x){
        Log.i("ackapa",""+x);
        if(serial!=null){
        byte[] gonder={x};
        serial.write(gonder);}
        int kacinci=x>>1;
        int ne=x&0x01;
        if(ne==1){
        durum |= 1 << (kacinci-2);}else{

            durum &= ~(1 << (kacinci-2));
        }


        hepsiniYenile();
    }
    @Override
    public void onPause(){
        unregisterReceiver(receiver);
        receiver=null;
        super.onPause();
    }
    @Override
    public void onResume(){

        register();
        super.onResume();
    }
    public void hepsiniYenile(){
        String gonder2="";
        for(int i =0;i<8;i++){
            gonder2+=""+((durum>>i)&0x01);

        }
        for(Client client:clients){


            client.gonder("&UPDT&"+gonder2);
            Log.i("gonder",gonder2);

        }
    }
    private void register(){
        IntentFilter filter=new IntentFilter();
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("TCP","shutdown");
                finish();
            }
        };
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver,filter);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receiver);
        Toast.makeText(this,"Hi biatch",Toast.LENGTH_SHORT).show();

          clients=new ArrayList<Client>();
        Log.i("started","!");


            dondur=true;
            new AsyncTask<Integer,Integer,Integer>(){
                @Override
                protected Integer doInBackground(Integer... params) {
                    try {

                        ss=new ServerSocket(TCP_PORT);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while(dondur){
                    try {

                        Socket socket=ss.accept();

                        clients.add(new Client(receiverActivity.this,socket));
                        Log.i("TCP","new socket:"+socket);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }}


                    return null;
                }
            }.execute();





        UsbManager usbManager=(UsbManager)getSystemService(USB_SERVICE);



        for(UsbDevice x:usbManager.getDeviceList().values()){
            device=x;

        }
        if(device!=null){
            usbConnection=usbManager.openDevice(device);
            UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {

                @Override
                public void onReceivedData(final byte[] arg0)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                             if(arg0.length>0)
                            durum=arg0[0];

                        }
                    });

                }

            };
            serial = UsbSerialDevice.createUsbSerialDevice(device,usbConnection,-1);
            if(serial==null)finish();
            serial.open();
            serial.setBaudRate(9600);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            serial.read(mCallback);}


    }

}
