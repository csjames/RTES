package com.example.james.avrdude;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class HexFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static final int FILE_CODE = 0;

    private TextView filePathView;

    private String path = null;
    private Button uploadFileButton, flashButton;


    public HexFragment() {
        // Required empty public constructor
    }

    public static HexFragment newInstance(String param1, String param2) {
        HexFragment fragment = new HexFragment();
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
        final View rootView = inflater.inflate(R.layout.flash, container,
                false);

        final TextView outputView = (TextView) rootView.findViewById(R.id.testOutput);

        uploadFileButton = (Button) rootView.findViewById(R.id.choosefile);

        filePathView = (TextView) rootView.findViewById(R.id.filePath);

        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_CODE );
            }
        });

        flashButton = (Button) rootView.findViewById(R.id.uploadButton);

        flashButton.setBackgroundColor(getResources().getColor(R.color.inactive));

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path == null){
                    Snackbar.make(rootView,"Please choose a file first!",Snackbar.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.dudeHandler.flashHex("/sdcard/out.hex", new DudeHandler.USBCallback() {
                                @Override
                                public void outputOnly(boolean success, String output) {
                                    outputView.setText(output);
                                }

                                @Override
                                public void fuses(boolean success, String output, String fuses) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    builder.setTitle("Flash hex file?");
                    builder.setMessage("Are you sure you wish to flash your device? Please ensure file is a hex file compiled for AVRf" +
                            "!");

                    AlertDialog dialog = builder.create();

                    dialog.show();
                }
            }
        });
        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            Uri uriPath = data.getData();

            try {
                InputStream f = null;
                f = getContext().getContentResolver().openInputStream(uriPath);

                Log.d("A4D", uriPath.toString());

                FileMover m = new FileMover();

                try {
                    m.copy(f, new File("/sdcard/out.hex"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                filePathView.setText(getFileName(uriPath));
                path = getPath(uriPath);

                Log.d("A4D", "HEX PATH: " + path);

                flashButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } catch (FileNotFoundException e) {
                Snackbar.make(getView(),"Unable to find file",Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else {
            path = null;
            flashButton.setBackgroundColor(getResources().getColor(R.color.inactive));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    public String getPath(Uri uri)
    {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
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
