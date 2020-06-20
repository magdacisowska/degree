package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnectAsyncTask extends AsyncTask<Void, Void, Integer> {

    private AsyncTaskResultListener asyncTaskResultListener;
    private Socket socket;
    private String ip;
    private Mat img;

    ServerConnectAsyncTask(Mat blob, String serverIP, Context c) throws IOException {
        img = blob;
        ip = serverIP;
        asyncTaskResultListener = (AsyncTaskResultListener) c;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, buf);
        byte[] imgBytes = buf.toArray();

        try {
            socket = new Socket(ip,8888);
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

            dout.write(imgBytes);
            dout.flush();

            DataInputStream din = new DataInputStream(socket.getInputStream());
            char first = (char) din.read();
            char second = (char) din.read();
            String sign = new StringBuilder().append(first).append(second).toString();

            dout.close();
            din.close();
            socket.close();
            return Integer.valueOf(sign);
        } catch (IOException e) {
            e.printStackTrace();
            return 99;
        }
    }

    @Override
    protected void onPostExecute(Integer imgClass) {
        asyncTaskResultListener.giveImgClass(imgClass);
    }
}
