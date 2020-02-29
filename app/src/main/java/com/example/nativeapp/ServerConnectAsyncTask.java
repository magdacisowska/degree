package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnectAsyncTask extends AsyncTask<Void, Void, Integer> {

    private AsyncTaskResultListener asyncTaskResultListener;
    private Socket socket;
    private Mat img;

    ServerConnectAsyncTask(Mat blob, Context c) throws IOException {
        img = blob;
        asyncTaskResultListener = (AsyncTaskResultListener) c;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, buf);
        byte[] imgBytes = buf.toArray();

        try {
            Log.i("INSIDE ASYNC TASK", "FUCK");
            socket = new Socket("192.168.0.109",8888);
            Log.i("INSIDE ASYNC TASK", "1");
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            Log.i("INSIDE ASYNC TASK", "2");
            DataInputStream din = new DataInputStream(socket.getInputStream());
            Log.i("INSIDE ASYNC TASK", "3");

            dout.write(imgBytes);
            Log.i("INSIDE ASYNC TASK", "4");
            dout.flush();
            Log.i("INSIDE ASYNC TASK", "5");

            String str = din.readUTF();
            Log.i("IMGCLASS FROM SERVER __", str);

            dout.close();
            din.close();
            socket.close();

            return Integer.valueOf(str);
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
