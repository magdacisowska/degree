package com.example.nativeapp;

public interface AsyncTaskResultListener {

    void giveResult(int result);
    void giveImgClass(int result);
    void giveNearestNode(OSM_Node node);
}
