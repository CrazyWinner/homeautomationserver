package com.mertcan.myapplication;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Mertcan on 31.12.2016.
 */

public class Client {
    boolean logged=false;
    String nick="";
    Socket mySocket;
    Thread myThread;
    DataInputStream datain;
    DataOutputStream dataout;
    int sayac=0;
    receiverActivity activity;
    public void destroy(){

        try {

            myThread.interrupt();

            Log.i("TCP","disconnected:"+mySocket);
            dataout.close();
            datain.close();
            mySocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }



    }
    public void dinle(){

        Runnable runnable=new Runnable() {
            String komut="";
            @Override
            public void run() {
                while(!myThread.isInterrupted()){
                try {
                    sayac++;
                    if(!mySocket.isClosed())
                    if(datain.available()>0){

                        int ekle=datain.read();

                        if(ekle!=13){
                    komut+=(char)ekle;}else{
                            cozumle(komut);
                            komut="";

                        }
                    sayac=0;
                    }
                    myThread.sleep(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                    if(sayac==6000){
                        destroy();

                    }
            }}
        };
        myThread=new Thread(runnable);
        myThread.start();

    }
    public void cozumle(String komut){
        Log.i("TCP","komut:"+komut);
          if(komut.length()>0) {
              if (komut.charAt(0) == '&') {
                  selfCommand(komut);
              } else {
                  Log.i("TCP", komut);
              }

          }


    }
    private void selfCommand(String komut){
           Log.i("TCP","komut:"+komut);
        if(komut.length()>6)
            switch(komut.substring(1,5)){
                case "LOGN":
                    String[] cikti=(komut.substring(6,komut.length())).split("\\|");
                    System.out.println(komut);
                    nick=cikti[0];
                    String sifre=cikti[1];
                    Log.i("TCP","loginAttempt:"+nick+":"+sifre);
                    if(sifre.equals("123456789")){logged=true;
                        yenile();
                    }else{
                        gonder("&FAIL");

                    }
                    break;
                case "CHNG":
                    if(logged){

                        String[] cikti2=(komut.substring(6,komut.length())).split("\\|");
                        byte x=(byte)Integer.parseInt(cikti2[0]),y=(byte)Integer.parseInt(cikti2[1]);

                        Log.i("TCP","YAP:"+x+":"+y);
                        byte yap=(byte)((x<<1)|y);
                    activity.ackapa(yap);
                        try {
                            Thread.currentThread().sleep(1002);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }



                    }else{
                        Log.i("TCP","Not logged in");

                    }
                    break;

            }


    }
    public void yenile(){

        String gonder="";
        for(int i =0;i<8;i++){
            gonder+=""+((activity.durum>>i)&0x01);

        }
        gonder("&UPDT&"+gonder);


    }
    public void gonder(String x){
         final String komut=x+"\r";
        Runnable xx= new Runnable() {
            @Override
            public void run() {
                try {

                    dataout.write(komut.getBytes());
                    dataout.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(xx).start();



    }
    public Client(Context cont,Socket socket){
        mySocket=socket;
        try {
            datain=new DataInputStream(socket.getInputStream());
            dataout=new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        activity=(receiverActivity) cont;
        dinle();
    }
}
