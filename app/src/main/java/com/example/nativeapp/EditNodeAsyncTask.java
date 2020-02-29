package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;

import com.example.nativeapp.AsyncTaskResultListener;
import com.example.nativeapp.OSM_Node;
import com.example.nativeapp.Requests;

import java.util.Map;

import static com.example.nativeapp.Requests.createChangeset;
import static com.example.nativeapp.Requests.modifyNode;

public class EditNodeAsyncTask extends AsyncTask<Void, Void, Integer> {

    private AsyncTaskResultListener asyncTaskResultListener;
    private int changeset;
    private Map<String, String> t;
    private String basicAuthPayload;
    private OSM_Node node;

    private static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    EditNodeAsyncTask(String authPayLoad, OSM_Node nodeToEdit, Map<String, String> tags, int currentChangeset, Context c){
        t = tags;
        changeset = currentChangeset;
        node = nodeToEdit;
        asyncTaskResultListener = (AsyncTaskResultListener) c;
        basicAuthPayload = authPayLoad;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            String response = modifyNode(basicAuthPayload, Requests.operation.MODIFY, changeset, node, 0, 0, t);
            // if response is not numeric, it means http 409 error (changeset already closed)
            if (!isNumeric(response)){
                changeset = createChangeset(basicAuthPayload);
                modifyNode(basicAuthPayload, Requests.operation.MODIFY, changeset, node, 0, 0, t);
            }
        } catch (Exception e) {
            e.printStackTrace();
            changeset = -1;             // in case of other errors
        }
        return changeset;
    }

    @Override
    protected void onPostExecute(Integer changeset) {
    }
}
