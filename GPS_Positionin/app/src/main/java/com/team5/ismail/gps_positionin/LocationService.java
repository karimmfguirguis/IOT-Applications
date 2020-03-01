package com.team5.ismail.gps_positionin;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service {
    //you require your service to perform multi-threading (instead of processing start requests through a work queue), you can extend the Service class to handle each intent.
    private final static String TAG = LocationService.class.getSimpleName();
    private LocationManager locationManager;
    private LocationListener locationListener;
    double longitude = 0;
    double latitude = 0;
    double distance = 0;
    double average_Speed = 0;
    double totalTime = 0;
    Location mOldLoc = null;

    FileOutputStream fout;
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
    String name = "<name>" + "Tracking points" + "</name><trkseg>\n";
    String footer = "</trkseg></trk></gpx>";

    String segments = "";
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    File file;

    @Override
    public void onCreate() {
        //super.onCreate();
        final Context mcontext = getApplicationContext();
        Log.d(TAG, "onCreate: "+Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
        Log.d(TAG, "onCreate:"+ ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //set directory
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath()+"/MyGPX");
            if(!dir.exists())
                dir.mkdirs();
            file = new File(dir, String.format("myLog.gpx"));
        }
        Log.d(TAG, "BEFORE TRY!!!!!!");
        try {
            fout = new FileOutputStream(file);
            Log.d(TAG, "THE PATH IS "+getFilesDir().getPath());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                segments += "<trkpt lat=\"" + latitude + "\" lon=\"" + longitude + "\"><time>" + df.format(new Date(location.getTime())) + "</time></trkpt>\n";
                Log.d(TAG, "Longitude= " + longitude + " Latitude: " + latitude);

                if(mOldLoc != null){
                    distance += mOldLoc.distanceTo(location);
                    totalTime += ((location.getTime() - mOldLoc.getTime())/1000);
                    average_Speed = (distance / (totalTime))*3.6;
                }
                mOldLoc = location;

            }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locationListener);
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
    }


    private final MyAidlInterface.Stub mBinder = new MyAidlInterface.Stub() {
        public double get_Latitude(){
            //Log.d(TAG, "get_Latitude: " + latitude);
            return latitude;
        }
        public double get_Longitude(){
            return longitude;
        }
        public double get_Distance(){
            return distance;
        }
        public double get_AverageSpeed(){
            return average_Speed;
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        Toast.makeText(this, "binded", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        try {
            fout.write(header.getBytes());
            fout.write(name.getBytes());
            fout.write(segments.getBytes());
            fout.write(footer.getBytes());
            // close file, cleanup, etc
            fout.close();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        Toast.makeText(this, "unbinded", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
