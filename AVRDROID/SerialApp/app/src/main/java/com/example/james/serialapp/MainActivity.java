package com.example.james.serialapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jackpal.androidterm.emulatorview.ColorScheme;
import jackpal.androidterm.emulatorview.EmulatorView;
import jackpal.androidterm.emulatorview.TermSession;

public class MainActivity extends AppCompatActivity {

    UsbManager mUsbManager;
    UsbDevice device;
    UsbSerialDevice serialDevice;

    ArrayBlockingQueue<Integer> buff = new ArrayBlockingQueue<Integer>(8192);

    ActionBarDrawerToggle mDrawerToggle;

    boolean localEcho;

    byte[] outC = new byte[1];
    OutputStream o = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            System.out.println((char) b);
            outC[0] = (byte) b;
            if(serialDevice!=null){
                if(ctrlPressed){
                    ctrlPressed = false;

                    outC[0] = (byte) (outC[0] - 64);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ctrl.setTextColor(Color.BLACK);
                        }
                    });
                }
                serialDevice.write(outC);
            }
            if(localEcho) try {
                buff.put(b);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    InputStream i = new InputStream() {
        @Override
        public int read() throws IOException {
            try {
                int i = buff.take();
                System.out.println(i);
                return i;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        }
    };

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    private TermSession termSession = new TermSession();

    private int baudRate = 9600;
    private int flowControl = UsbSerialDevice.FLOW_CONTROL_OFF;
    private int dataBits = UsbSerialDevice.DATA_BITS_8;
    private int stopBits = UsbSerialDevice.STOP_BITS_1;
    private int parityType = UsbSerialDevice.PARITY_NONE;
    private EmulatorView emulator;

    boolean ctrlPressed = false;
    private Button ctrl;
    private DeviceDCReceiver dc;
    private DeviceCReceiver cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        constructMenu();

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                null,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("SerialApp");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Options");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        emulator = new EmulatorView(this, null);
        emulator.attachSession(termSession);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT); // 2 pixels
        params.addRule(RelativeLayout.ABOVE, R.id.buttonBar);
        ((RelativeLayout) findViewById(R.id.terminalParent)).addView(emulator, params);

        DisplayMetrics dm = new DisplayMetrics();
        dm.setToDefaults();
        emulator.setDensity(dm);
        emulator.setTextSize(12);

        termSession.initializeEmulator(80,24);
        termSession.setTermIn(i);
        termSession.setTermOut(o);
        termSession.setDefaultUTF8Mode(false);

        ColorScheme c = new ColorScheme(0xff45392e,0xfffaebd7,0xff7d756d,0xffeabc52);    // who doesnt love hardcoded values.
        emulator.setColorScheme(c);


        try {
            o.write("heyhey".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Button b = (Button) findViewById(R.id.connect);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);
                if(mUsbManager.getDeviceList().values().size() == 0){
                    Toast.makeText(MainActivity.this, "There is no USB Serial Device Connected :(", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(UsbDevice d : mUsbManager.getDeviceList().values()){
                    mUsbManager.requestPermission(d, mPermissionIntent);
                    break;
                }
            }
        });

        Button r = (Button) findViewById(R.id.reconnect);

        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serialDevice!=null){
                    serialDevice.close();
                    emulator.getTermSession().reset();
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "You arent connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ctrl = (Button) findViewById(R.id.ctrlBtn);

        ctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serialDevice!=null){
                    if(!ctrlPressed) {
                        ctrlPressed = true;
                        ctrl.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }else{
                        ctrlPressed = false;
                        ctrl.setTextColor(Color.BLACK);
                    }
                }
            }
        });

        Button upButton = (Button) findViewById(R.id.upBtn);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDirection('A');
            }
        });

        Button downButton = (Button) findViewById(R.id.downBtn);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDirection('B');
            }
        });

        Button leftButton = (Button) findViewById(R.id.leftBtn);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDirection('D');
            }
        });

        Button rightButton = (Button) findViewById(R.id.rightBtn);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDirection('C');
            }
        });

        Button homeButton = (Button) findViewById(R.id.homeBtn);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    o.write(0x1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button endButton = (Button) findViewById(R.id.endBtn);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    o.write(0x5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        Button tabBtn = (Button) findViewById(R.id.tabBtn);
        tabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    o.write(0x9);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dc = new DeviceDCReceiver(MainActivity.this,null);
        IntentFilter filterdc = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");
        registerReceiver(dc,filterdc);

        cr = new DeviceCReceiver(MainActivity.this,null);
        IntentFilter filterc = new IntentFilter("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        registerReceiver(cr,filterc);
    }

    public void sendDirection(int direction){
        try {
            o.write(0x1b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] b = new byte[2];
        b[0] = '[';
        b[1] = (byte) direction;
        try {
            o.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            UsbDeviceConnection d = mUsbManager.openDevice(device);
                            serialDevice = UsbSerialDevice.createUsbSerialDevice(device, d);

                            serialDevice.open();
                            serialDevice.setBaudRate(baudRate);
                            serialDevice.setDataBits(dataBits);
                            serialDevice.setStopBits(stopBits);
                            serialDevice.setFlowControl(flowControl);
                            serialDevice.setParity(parityType);

                            try {
                                serialDevice.read(new UsbSerialInterface.UsbReadCallback() {
                                    @Override
                                    public void onReceivedData(byte[] bytes) {
                                        for (int i = 0; i < bytes.length; i++) {
                                            try {
                                                buff.put((int) bytes[i]);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }catch (Exception e){
                                Toast.makeText(MainActivity.this, "You are already connected mate, try disconnecting", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(MainActivity.this, "Connected  :) ", Toast.LENGTH_SHORT).show();
                            serialDevice.write("\n".getBytes());
                            dc.setD(serialDevice);
                        }
                    }
                    else {
                        Log.d("SerialApp", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getTitle().equals("Keyboard")){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(emulator, InputMethodManager.SHOW_IMPLICIT);
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    public void constructMenu(){

        CheckBox c = (CheckBox) findViewById(R.id.local_echo);
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                localEcho = isChecked;
            }
        });

        final Spinner baud = (Spinner) findViewById(R.id.baud);
        ArrayAdapter<CharSequence> fromResource = ArrayAdapter.createFromResource(this, R.array.Baud_Rate, android.R.layout.simple_spinner_item);
        fromResource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baud.setAdapter(fromResource);

        baud.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baudRate = Integer.parseInt((String) baud.getItemAtPosition(position));
                if(serialDevice != null) serialDevice.setBaudRate(baudRate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner flow = (Spinner) findViewById(R.id.flow);
        ArrayAdapter<CharSequence> fromResource1 = ArrayAdapter.createFromResource(this, R.array.Flow_Control, android.R.layout.simple_spinner_item);
        fromResource1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flow.setAdapter(fromResource1);

        flow.setSelection(0);

        flow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        flowControl = UsbSerialDevice.FLOW_CONTROL_OFF;
                        break;
                    case 1:
                        flowControl = UsbSerialDevice.FLOW_CONTROL_RTS_CTS;
                        break;
                    case 2:
                        flowControl = UsbSerialDevice.FLOW_CONTROL_DSR_DTR;
                        break;
                    case 3:
                        flowControl = UsbSerialDevice.FLOW_CONTROL_XON_XOFF;
                        break;

                }
                if(serialDevice!= null) serialDevice.setFlowControl(flowControl);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Spinner data = (Spinner) findViewById(R.id.data);
        ArrayAdapter<CharSequence> fromResource2 = ArrayAdapter.createFromResource(this, R.array.Data_Bits, android.R.layout.simple_spinner_item);
        fromResource2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        data.setAdapter(fromResource2);

        data.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        dataBits = UsbSerialDevice.DATA_BITS_5;
                        break;
                    case 1:
                        dataBits = UsbSerialDevice.DATA_BITS_6;
                        break;
                    case 2:
                        dataBits = UsbSerialDevice.DATA_BITS_7;
                        break;
                    case 3:
                        dataBits = UsbSerialDevice.DATA_BITS_8;
                        break;
                }
                if(serialDevice!= null) serialDevice.setDataBits(dataBits);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Spinner stop = (Spinner) findViewById(R.id.stop);
        ArrayAdapter<CharSequence> fromResource3 = ArrayAdapter.createFromResource(this, R.array.Stop_Bits, android.R.layout.simple_spinner_item);
        fromResource3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stop.setAdapter(fromResource3);

        stop.setSelection(0);

        stop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        stopBits = UsbSerialDevice.STOP_BITS_1;
                        break;
                    case 1:
                        stopBits = UsbSerialDevice.STOP_BITS_2;
                        break;
                }
                if(serialDevice!=null) serialDevice.setStopBits(stopBits);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Spinner parity = (Spinner) findViewById(R.id.parity);
        ArrayAdapter<CharSequence> fromResource4 = ArrayAdapter.createFromResource(this, R.array.Parity, android.R.layout.simple_spinner_item);
        fromResource4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parity.setAdapter(fromResource4);

        parity.setSelection(0);

        parity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        parityType = UsbSerialDevice.PARITY_NONE;
                        break;
                    case 1:
                        parityType = UsbSerialDevice.PARITY_ODD;
                        break;
                    case 2:
                        parityType = UsbSerialDevice.PARITY_EVEN;
                        break;
                    case 3:
                        parityType = UsbSerialDevice.PARITY_MARK;
                        break;
                    case 4:
                        parityType = UsbSerialDevice.PARITY_SPACE;
                        break;
                }
                if(serialDevice!=null) serialDevice.setParity(parityType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        baud.post(new Runnable() {
            @Override
            public void run() {
                baud.setSelection(3);
                data.setSelection(3);
            }
        });


    }

}
