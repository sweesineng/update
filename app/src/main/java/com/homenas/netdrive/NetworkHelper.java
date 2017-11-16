package com.homenas.netdrive;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by engss on 15/11/2017.
 */

public class NetworkHelper extends AsyncTask {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    public NetworkHelper(Context context){
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object... arg0) {
        ConnectivityManager conMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        try{
            if( netInfo != null) {
                switch (netInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        Log.i(TAG, "Wifi Connection");
                        Constants.IPAddress = null;
                    case ConnectivityManager.TYPE_MOBILE:
                        Log.i(TAG, "Mobile Connection");
                        getIPAdress();
                        Log.i(TAG,"IPAddress: " + Constants.IPAddress + " Bcast: " + Constants.BcastAddress);
                }
            }else{
                Log.i(TAG, "No Connection");
                Constants.IPAddress = null;
            }
        }catch(Exception ex){
            Constants.IPAddress = null;
        }
        return null;
    }

    private void getIPAdress() {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                if(!networkInterface.isLoopback()){
                    List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
                    Iterator<InterfaceAddress> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        InterfaceAddress interfaceAddress = iterator.next();
                        String sAddr = interfaceAddress.getAddress().toString().substring(1);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if(isIPv4){
                            Constants.IPAddress = sAddr;
                            Constants.BcastAddress = interfaceAddress.getBroadcast().toString().substring(1);
                        }else{
                            int delim = sAddr.indexOf('%');
                            Constants.IPAddress = delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
