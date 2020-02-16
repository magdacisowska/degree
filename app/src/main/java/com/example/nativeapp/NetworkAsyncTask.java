package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;

import com.example.nativeapp.Requests;

import java.util.Map;

import static com.example.nativeapp.Requests.createChangeset;
import static com.example.nativeapp.Requests.modifyNode;

public class NetworkAsyncTask extends AsyncTask<Void, Void, Integer> {

    private AsyncTaskResultListener asyncTaskResultListener;
    private int changeset;
    private double lat, lon;
    private Map<String, String> t;
    private String basicAuthPayload;

    private static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    NetworkAsyncTask(String authPayLoad, double latitude, double longitude, Map<String, String> tags, int currentChangeset, Context c){
        lat = latitude;
        lon = longitude;
        t = tags;
        changeset = currentChangeset;
        asyncTaskResultListener = (AsyncTaskResultListener) c;
        basicAuthPayload = authPayLoad;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            String response = modifyNode(basicAuthPayload, Requests.operation.CREATE, changeset, null, lat, lon, t);
            // if response is not numeric, it means http 409 error (changeset already closed)
            if (!isNumeric(response)){
                changeset = createChangeset(basicAuthPayload);
                modifyNode(basicAuthPayload, Requests.operation.CREATE, changeset, null, lat, lon, t);
            }
        } catch (Exception e) {
            e.printStackTrace();
            changeset = -1;             // in case of other errors
        }
        return changeset;
    }

    @Override
    protected void onPostExecute(Integer changeset) {
        asyncTaskResultListener.giveResult(changeset);
    }
}
