package com.example.tmbro.isstracker.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.tmbro.isstracker.Controller.NotificationController;
import com.example.tmbro.isstracker.R;
import com.example.tmbro.isstracker.Model.UpdateThread;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION = 1;
    int off = 0;
    public GoogleMap mMap;
    UpdateThread th;
    boolean alreadysent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d("CREATION", "Thread might run");
        th = new UpdateThread(this.getApplicationContext());
        th.execute();

        try {
            off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if(off==0){
            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(onGPS);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        double lati = intent.getDoubleExtra("USER_LAT", 50);
        double loni = intent.getDoubleExtra("USER_LON", 8);
        /*Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        Timer t = new Timer();

        TimerTask task = new TimerTask(){

                @Override
                public void run(){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<Double> lat = th.lat();
                            List<Double> lon = th.lon();
                            Log.d("COÖRDS", "Lat: "+lat+" Lon: "+lon);
                            mMap.clear();

                            LatLng homeco = new LatLng(lati, loni);

                            mMap.addMarker(new MarkerOptions().position(homeco).title(getString(R.string.home_marker))).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.house_icon));

                            if(!lat.isEmpty() && !lon.isEmpty()) {

                                LatLng one = new LatLng(lat.get(0), lon.get(0));
                                LatLng two = new LatLng(lat.get(1), lon.get(1));
                                LatLng three = new LatLng(lat.get(2), lon.get(2));
                                LatLng four = new LatLng(lat.get(3), lon.get(3));
                                LatLng five = new LatLng(lat.get(4), lon.get(4));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat.get(4), lon.get(4))).title(getString(R.string.ISS_marker)).anchor(0.5f, 0.5f)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.space_station));
                                LatLng six = new LatLng(lat.get(5), lon.get(5));
                                LatLng seven = new LatLng(lat.get(6), lon.get(6));
                                LatLng eight = new LatLng(lat.get(7), lon.get(7));
                                LatLng nine = new LatLng(lat.get(8), lon.get(8));
                                mMap.addPolyline(new PolylineOptions()
                                        .add(one, two, three, four, five, six, seven, eight, nine)
                                        .color(Color.BLUE));
                                //mMap.addMarker(new MarkerOptions().position(new LatLng(lat.get(i), lon.get(i))).title("Hier is een spacestation."));

                                Location satellite = new Location("");
                                satellite.setLatitude(lat.get(4));
                                satellite.setLongitude(lon.get(4));

                                Location home = new Location("");
                                home.setLatitude(lati);
                                home.setLongitude(loni);

                                Circle circle = mMap.addCircle(new CircleOptions()
                                        .center(five)
                                        .radius(1500000)
                                        .strokeColor(Color.GREEN));

                                if(home.distanceTo(satellite)<1500000 && !alreadysent){
                                    new NotificationController(getApplicationContext());
                                    alreadysent = true;
                                }
                                if((home.distanceTo(satellite)>1500000)) {
                                    alreadysent = false;
                                }
                            }

                        }
                    });
                }
            };

        t.scheduleAtFixedRate(task, 0, 1200);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);

                } else {

                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast), duration);
                    toast.show();
                }
                return;
            }
        }
    }

    private void sendNotification() {
        // Intent to start the main Activity
        Intent notificationIntent = new Intent(getApplicationContext(), MapActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(
                0,
                createNotification(getString(R.string.close_toast), "", notificationPendingIntent));

    }

    // Create notification
    private Notification createNotification(String msg, String name, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ISS");
        notificationBuilder
                .setSmallIcon(R.drawable.space_station)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText(name)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    @Override
    public void onDestroy(){
        Log.d("DESTROY","activity destroyed");
        System.exit(0);
        super.onDestroy();
    }
}


