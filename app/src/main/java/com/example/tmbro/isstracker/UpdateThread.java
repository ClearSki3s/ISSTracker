package com.example.tmbro.isstracker;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Trist on 29-12-2017.
 */

public class UpdateThread extends AsyncTask<Integer, Integer, Void> {
int counter = 0;
    @Override
    protected Void doInBackground(Integer... integers) {

            while (true) {
                Log.d("SUCCES", "Thread runs");
                counter++;
                 try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress();
            }

    }

    @Override
    protected void onProgressUpdate(Integer... counter) {
        Log.d("COUNTING", ""+counter);
    }
}

