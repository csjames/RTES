package com.example.lsk95.serialapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by lsk95 on 11/13/2016.
 */

public class Preference extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
        Intent intent = new Intent(getApplicationContext(),SerialApp.class);

        startActivity(intent);
    }
}
