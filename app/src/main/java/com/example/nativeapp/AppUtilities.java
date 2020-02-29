package com.example.nativeapp;

import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public int decodeAnsToDrawable(int code){
        int drawable;
        switch (code){
            case 0:
                drawable = R.drawable.dwadziescia;
                break;
            case 1:
                drawable = R.drawable.trzydziesci;
                break;
            case 2:
                drawable = R.drawable.czterdziesci;
                break;
            case 3:
                drawable = R.drawable.piecdziesiat;
                break;
            case 4:
                drawable = R.drawable.szescdziesiat;
                break;
            case 5:
                drawable = R.drawable.siedemdziesiat;
                break;
            default:
                drawable = R.drawable.koniec;
                break;
        }
        return drawable;
    }

    public Map<String, String> decodeAnsToTags(int code){
        Map<String, String> tags = new HashMap<>();
        switch (code){
            case 0:             // 20
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "20");
                break;
            case 1:             // 30
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "30");
                break;
            case 2:             // 40
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "40");
                break;
            case 3:             // 50
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "50");
                break;
            case 4:             // 60
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "60");
                break;
            case 5:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "70");
                break;
            case 6:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "80");
                break;
            case 7:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "90");
                break;
            case 8:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "100");
                break;
            case 9:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "120");
                break;
        }
        return tags;
    }
}

