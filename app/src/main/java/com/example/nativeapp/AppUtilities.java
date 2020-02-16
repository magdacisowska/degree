package com.example.nativeapp;

import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

/** Abstract class
 * Has some helper functions for the entire app
 */
public abstract class AppUtilities extends AppCompatActivity {

    // encode basic authentication credentials for REST API
    public String encodeAuth(){
        String osm_user = getResources().getString(R.string.osm_user);
        String osm_password = getResources().getString(R.string.osm_password);

        String usernameColonPassword = osm_user + ":" + osm_password;
        return "Basic " + Base64.encodeToString(usernameColonPassword.getBytes(), Base64.DEFAULT);
    }
}

