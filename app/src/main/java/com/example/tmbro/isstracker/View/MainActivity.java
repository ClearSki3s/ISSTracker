package com.example.tmbro.isstracker.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tmbro.isstracker.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String USER_HOME = "USER_HOME";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    Geocoder geocoder;
    List<Address> addresses;

    double latitude;
    double longitude;
    boolean addressFailed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences.Editor editor = getSharedPreferences(USER_HOME, MODE_PRIVATE).edit();
        SharedPreferences reader = getSharedPreferences(USER_HOME, MODE_PRIVATE);

        final TextView textView = findViewById(R.id.updateTxt);

        float readlat = reader.getFloat("lat", 1);
        float readlon = reader.getFloat("lon", 1);

        latitude = readlat;
        longitude = readlon;
        textView.setText(getString(R.string.home_location) + reader.getString("place", getString(R.string.please_update)));

        final Button saveButton = findViewById(R.id.saveButton);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Context context = this;
        Activity activity = this;

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    Boolean stop = false;
                    try {

                        int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                        if(off==0){
                            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(onGPS);
                            stop = true;
                        }

                        //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(!stop){
                        List<String> providers = lm.getProviders(true);
                        Location location = null;
                        for (String provider : providers) {
                            Location l = lm.getLastKnownLocation(provider);
                            if (l == null) {
                                continue;
                            }
                            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                                // Found best last known location: %s", l);
                                location = l;
                            }
                        }
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        geocoder = new Geocoder(context, Locale.getDefault());
                        int i = 0;
                        while (addresses == null || addresses.isEmpty()) {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            i++;
                            if(i>50) {
                                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.noAdress),Toast.LENGTH_SHORT);
                                toast.show();
                                addressFailed = true;
                                break;
                            }

                        }
                        if(!addressFailed) {
                            editor.putFloat("lat", (float) latitude);
                            editor.putFloat("lon", (float) longitude);
                            editor.putString("place", addresses.get(0).getLocality());
                            editor.apply();
                            textView.setText(getString(R.string.home_location) + addresses.get(0).getLocality());
                        }
                        }

                        stop = true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
        });

        final Button button = findViewById(R.id.mapButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MapActivity.class);

                intent.putExtra("USER_LON", longitude);
                intent.putExtra("USER_LAT", latitude);
                startActivity(intent);
                finish();

            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast), duration);
                    toast.show();
                }
                return;
            }
        }
    }
}
