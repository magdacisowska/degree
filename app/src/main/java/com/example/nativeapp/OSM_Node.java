package com.example.nativeapp;

import java.util.HashMap;
import java.util.Map;

public class OSM_Node {
    public String id, lat, lon, ver;
    public Map<String, String> tags = new HashMap<>();

    public OSM_Node(String node_id, String latitude, String longitude, String version){
        this.id = node_id;
        this.lat = latitude;
        this.lon = longitude;
        this.ver = version;
    }

    public void append_tags(String tag_k, String tag_v){
        this.tags.put(tag_k, tag_v);
    }

    // calculate distance between two nodes
    public double nodeDistance(double userLat, double userLon){
        return Math.sqrt(Math.pow(Math.abs(userLat - Double.valueOf(lat)), 2) +
                Math.pow(Math.abs(userLon - Double.valueOf(lon)), 2));
    }

    @Override
    public String toString(){
        return String.format("Node %s, tags: %s", this.id, this.tags);
    }
}
