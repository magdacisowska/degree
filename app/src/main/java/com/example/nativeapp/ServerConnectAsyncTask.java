package com.example.nativeapp;

import android.content.Context;
import android.os.AsyncTask;

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

    {
        try {
            socket = new Socket("localhost",2004);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());

            dout.write(imgBytes);
            dout.flush();

            String str = din.readUTF();

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
