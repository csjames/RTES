package com.example.james.avrdude;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;


public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    UsbManager manager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings, container,
                false);

        final Spinner chips = (Spinner) rootView.findViewById(R.id.device);

        ArrayAdapter<CharSequence> chipadapter = ArrayAdapter.createFromResource(rootView.getContext(),
        R.array.chips, android.R.layout.simple_spinner_item);

        chipadapter.sort(new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence lhs, CharSequence rhs) {
                return lhs.toString().compareTo(rhs.toString());   //or whatever your sorting algorithm
            }
        });

        chipadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chips.setAdapter(chipadapter);
        final Spinner programmers = (Spinner) rootView.findViewById(R.id.programmer);

        final ArrayAdapter<CharSequence> programmerAdapter = ArrayAdapter.createFromResource(rootView.getContext(),
                R.array.programmers, android.R.layout.simple_spinner_item);

        programmerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        programmerAdapter.sort(new Comparator<CharSequence>() {
            @Override
            public int compare(CharSequence lhs, CharSequence rhs) {
                return lhs.toString().compareTo(rhs.toString());   //or whatever your sorting algorithm
            }
        });

        programmers.setAdapter(programmerAdapter);


        chips.setSelection(chipadapter.getPosition("m644p"));

        programmers.setSelection(programmerAdapter.getPosition("C232HM"));

        programmers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.dudeHandler.programmer = (String) programmers.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        chips.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.dudeHandler.chip = (String) chips.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        Button b = (Button) rootView.findViewById(R.id.testConnection);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("A4D","CLICKED");
                ((TextView) rootView.findViewById(R.id.testOutput)).setText("");

                MainActivity.dudeHandler.testConnection(new DudeHandler.USBCallback() {
                    @Override
                    public void outputOnly(boolean success, String output) {
                        ((TextView) rootView.findViewById(R.id.testOutput)).setText(output);

                        if(success){
                            Snackbar.make(rootView,"Great Success",Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(rootView,"Failed",Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void fuses(boolean success, String output, String fuses) {
                    }
                });

            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
