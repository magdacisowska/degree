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
                drawable = R.drawable.piecdziesiat;
                break;
            case 3:
                drawable = R.drawable.szescdziesiat;
                break;
            case 4:
                drawable = R.drawable.siedemdziesiat;
                break;
            case 5:
                drawable = R.drawable.osiemdziesiat;
                break;
            case 6:
                drawable = R.drawable.dziewiecdziesiat;
                break;
            case 7:
                drawable = R.drawable.sto;
                break;
            case 8:
                drawable = R.drawable.stodwadziescia;
                break;
            case 9:
                drawable = R.drawable.zakaz;
                break;
            case 10:
                drawable = R.drawable.wyprzedzanie;
                break;
            case 11:
                drawable = R.drawable.wyprzedzanie_ciez;
                break;
            case 12:
                drawable = R.drawable.zatrzymywania;
                break;
            case 13:
                drawable = R.drawable.wjazdu;
                break;
            case 14:
                drawable = R.drawable.osobowe;
                break;
            case 15:
                drawable = R.drawable.zawracanie;
                break;
            case 16:
                drawable = R.drawable.prawo;
                break;
            case 17:
                drawable = R.drawable.lewo;
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
            case 2:             // 50
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "50");
                break;
            case 3:             // 60
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "60");
                break;
            case 4:             // 70
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "70");
                break;
            case 5:             // 80
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "80");
                break;
            case 6:             // 90
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "90");
                break;
            case 7:             // 100
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "100");
                break;
            case 8:             // 120
                tags.put("traffic_sign", "maxspeed");
                tags.put("maxspeed", "120");
                break;
            case 9:             // zakaz ruchu
                tags.put("traffic_sign", "no_traffic");
                break;
            case 10:             // zakaz wyprzedzania
                tags.put("traffic_sign", "no_overtaking");
                break;
            case 11:             // zakaz wyprzedzania ciężarowe
                tags.put("traffic_sign", "no_overtaking_lorries");
                break;
            case 12:             // zakaz zatrzymywania się
                tags.put("traffic_sign", "no_stopping");
                break;
            case 13:             // zakaz wjazdu
                tags.put("traffic_sign", "no_entry");
                break;
            case 14:             // zakaz ruchu osobowych
                tags.put("traffic_sign", "no_cars");
                break;
            case 15:             // zakaz zawracania
                tags.put("traffic_sign", "no_uturn");
                break;
            case 16:             // zakaz w prawo
                tags.put("traffic_sign", "no_right");
                break;
            case 17:             // zakaz w lewo
                tags.put("traffic_sign", "no_left");
                break;
        }
        return tags;
    }
}

