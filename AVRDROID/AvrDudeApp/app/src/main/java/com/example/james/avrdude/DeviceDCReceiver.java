package com.example.james.avrdude;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by james on 02/11/16.
 */

public class DeviceDCReceiver extends BroadcastReceiver{

    MainActivity m;

    String cableDC = "FTDI Cable Disconnected";

    private ArrayList<ConnectionInfo> tabs = new ArrayList<>();

    public DeviceDCReceiver(MainActivity m, ArrayList<ConnectionInfo> tabs){
        this.tabs = tabs;
        this.m = m;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(tabs != null) {
            for (ConnectionInfo t : tabs) {
                t.connectionChanged(false);
            }
        }

        if (m != null) {
            Snackbar.make(m.findViewById(R.id.activity_main),cableDC,Snackbar.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, cableDC, Toast.LENGTH_SHORT).show();
        }
    }
}
