
package com.example.james.serialapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by james on 02/11/16.
 */

public class DeviceCReceiver extends BroadcastReceiver{

    private MainActivity m;

    private String cableDC = "FTDI Cable Connected";

    private ArrayList<ConnectionInfo> tabs = new ArrayList<>();

    public DeviceCReceiver(MainActivity m, ArrayList<ConnectionInfo> tabs){
        this.m = m;
        this.tabs = tabs;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(tabs != null) {
            for (ConnectionInfo t : tabs) {
                t.connectionChanged(true);
            }
        }
        if (m != null) {
            Snackbar s = Snackbar.make(m.findViewById(R.id.drawer_layout),cableDC,Snackbar.LENGTH_SHORT);
            s.show();
        } else {
            Toast.makeText(context, cableDC, Toast.LENGTH_SHORT).show();
        }
    }
}