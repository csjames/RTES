package com.example.james.avrdude;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DeviceCReceiver c;
    DeviceDCReceiver dc;

    UsbManager manager;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public static DudeHandler dudeHandler;

    private static final int REQUEST_CODE = 0x11;

    private ArrayList<ConnectionInfo> tabs = new ArrayList<ConnectionInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();

        dudeHandler = new DudeHandler(this);
        dudeHandler.programmer = "C232HM";
        dudeHandler.chip = "m644p";

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        ActionBar.Tab tab1 = actionBar
                .newTab()
                .setText("Settings")
                .setTabListener(new SupportFragmentTabListener<SettingsFragment>(R.id.flContainer, this,
                        "first", SettingsFragment.class));

        actionBar.addTab(tab1);
        actionBar.selectTab(tab1);

        ActionBar.Tab tab2 = actionBar
                .newTab()
                .setText("Upload HEX")
                .setTabListener(new SupportFragmentTabListener<HexFragment>(R.id.flContainer, this,
                        "second", HexFragment.class));
        actionBar.addTab(tab2);

        ActionBar.Tab tab3 = actionBar
                .newTab()
                .setText("Fuses")
                .setTabListener(new SupportFragmentTabListener<FuseFragment>(R.id.flContainer, this,
                        "second", FuseFragment.class));
        actionBar.addTab(tab3);

        ActionBar.Tab tab4 = actionBar
                .newTab()
                .setText("Serial")
                .setTabListener(new SupportFragmentTabListener<SerialFragment>(R.id.flContainer, this,
                        "second", SerialFragment.class));
        actionBar.addTab(tab4);

        FileMover m = new FileMover();

        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);

        dc = new DeviceDCReceiver(MainActivity.this,tabs);
        IntentFilter filterdc = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");
        registerReceiver(dc,filterdc);

        c = new DeviceCReceiver(MainActivity.this,tabs);
        IntentFilter filterc = new IntentFilter("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        registerReceiver(c,filterc);

    }

    public void register(ConnectionInfo e){
        tabs.add(e);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dc);
        unregisterReceiver(c);
    }

    public DudeHandler getDudeHandler() {
        return dudeHandler;
    }

    public void setDudeHandler(DudeHandler dudeHandler) {
        this.dudeHandler = dudeHandler;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FileMover m = new FileMover();
                m.copyAsset(this, "avrdude.conf");
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}