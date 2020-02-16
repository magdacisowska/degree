package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;

import com.example.nativeapp.OSM_Node;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import static com.example.nativeapp.Requests.getBoundBox;

public class BoundingBoxAsyncTask extends AsyncTask<Double, Void, Integer> {

    private AsyncTaskResultListener asyncTaskResultListener;
    private String basicAuthPayload;

    BoundingBoxAsyncTask(Context c, String authPayLoad){
        asyncTaskResultListener = (AsyncTaskResultListener) c;
        basicAuthPayload = authPayLoad;
    }

    @Override
    protected Integer doInBackground(Double... doubles) {
        final double RADIUS = 0.0005;
        double lat = doubles[0];
        double lon = doubles[1];

        List<OSM_Node> nodeList = new ArrayList<>();
        try {
            nodeList = getBoundBox(basicAuthPayload, lon - RADIUS, lat - RADIUS, lon + RADIUS, lat + RADIUS);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        // if nodeList returned by getBoundBox is not empty, find the nearest node
        if (nodeList.size() > 0) {
            List<Double> distances = new ArrayList<>();
            for (OSM_Node node: nodeList){
                distances.add(node.nodeDistance(lat, lon));
            }

            int nearestNodeIndex = distances.indexOf(Collections.min(distances));
            OSM_Node nearestNode = nodeList.get(nearestNodeIndex);

            if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 20){
                return R.drawable.dwadziescia;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 30){
                return R.drawable.trzydziesci;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 40){
                return R.drawable.czterdziesci;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 50){
                return R.drawable.piecdziesiat;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 60){
                return R.drawable.szescdziesiat;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 70){
                return R.drawable.siedemdziesiat;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 80){
                return R.drawable.osiemdziesiat;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 90){
                return R.drawable.dziewiecdziesiat;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 100){
                return R.drawable.sto;
            } else if (Integer.valueOf(nearestNode.tags.get("maxspeed")) == 120){
                return R.drawable.stodwadziescia;
            } else {
                return R.drawable.koniec;
            }
        }
        else {
            return R.drawable.zakaz;
        }
    }

    // use asyncTaskResultListener Interface to pass the result to the Main Activity
    @Override
    protected void onPostExecute(Integer drawable) {
        asyncTaskResultListener.giveResult(drawable);
    }
}
