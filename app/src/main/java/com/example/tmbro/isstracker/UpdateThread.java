package com.example.tmbro.isstracker;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Trist on 29-12-2017.
 */

public class UpdateThread extends AsyncTask<Context, Integer, double[]> {
double lat;
double lon;
Context currentContext;

    public UpdateThread(Context context){
        currentContext = context;
    }

    @Override
    protected double[] doInBackground(Context... integers) {

        MapController mCon = new MapController("https://api.wheretheiss.at/v1/satellites/25544", currentContext);

            while (true) {
                lat = mCon.getSatLat();
                lon = mCon.getSatLong();
                mCon = new MapController("https://api.wheretheiss.at/v1/satellites/25544", currentContext);
                 try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }
}

