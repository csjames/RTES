package com.example.james.avrdude;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FuseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FuseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FuseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FuseFragment newInstance(String param1, String param2) {
        FuseFragment fragment = new FuseFragment();
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
        View rootView = inflater.inflate(R.layout.fusebits, container,
                false);

        final TextView outputT = (TextView) rootView.findViewById(R.id.testOutput);

        final EditText lFuseE = (EditText) rootView.findViewById(R.id.lfuse);
        final EditText hFuseE = (EditText) rootView.findViewById(R.id.hfuse);
        final EditText eFuseE = (EditText) rootView.findViewById(R.id.efuse);

        final TextView lFuseT = (TextView) rootView.findViewById(R.id.lfusec);
        final TextView hFuseT = (TextView) rootView.findViewById(R.id.hfusec);
        final TextView eFuseT = (TextView) rootView.findViewById(R.id.efusec);

        Button burnButton = (Button) rootView.findViewById(R.id.burnFuses);

        burnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setPositiveButton("Burn", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String lfuse = lFuseE.getText().toString();
                        if(lfuse.length() == 0) lfuse = null;

                        String hfuse = hFuseE.getText().toString();
                        if(hfuse.length() == 0) hfuse = null;

                        String efuse = eFuseE.getText().toString();
                        if(efuse.length() == 0) efuse = null;

                        MainActivity.dudeHandler.setFuses(hfuse, lfuse, efuse, new DudeHandler.USBCallback() {
                            @Override
                            public void outputOnly(boolean success, String output) {
                                Log.d("A4D",output);
                                outputT.setText(output);
                            }
                            @Override
                            public void fuses(boolean success, String output, String fuseStr) {

                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                builder.setTitle("Burn fuses?");
                builder.setMessage("Are you sure you wish to burn fuses. Its not my fault if you mess up");

                AlertDialog dialog = builder.create();

                dialog.show();

            }
        });

        Button readButton = (Button) rootView.findViewById(R.id.readFuses);

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.dudeHandler.readFuses(new DudeHandler.USBCallback() {
                    @Override
                    public void outputOnly(boolean success, String output) {

                    }

                    @Override
                    public void fuses(boolean success, String output, String fuseStr) {
                        Log.d("A4D",output);

                        outputT.setText(output);

                        String[] fuses = fuseStr.split(":");

                        if(fuses.length > 2) {
                            lFuseT.setText(fuses[0]);
                            hFuseT.setText(fuses[1]);
                            eFuseT.setText(fuses[2]);
                        }
                    }
                });
            }
        });
        return rootView;
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
