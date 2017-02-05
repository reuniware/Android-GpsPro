package com.gpspro.gpspro;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by Idjed on 05/02/2017.
 */

public class GpsproHttpRequest extends AsyncTask<URL, Integer, Long>{

    @Override
    protected Long doInBackground(URL... urls) {
        //System.out.println("GpsproHttpRequest");

        if (urls.length>0) {
            URL url = urls[0];
            HttpURLConnection urlConnection = null;
            //URL url = new URL("http://www.android.com/");
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = null;
                in = new BufferedInputStream(urlConnection.getInputStream());
                in.close();
                //System.out.println("request ok:" + url.toString());
            } catch (UnknownHostException e) {
                //System.out.println("Unavailable network ?");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        }

        return null;
    }
}





