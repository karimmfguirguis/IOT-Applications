package com.team5.ismail.gps_positionin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button StartButton;
    private Button StopButton;
    private Button updateValues;
    private TextView latitude;
    private TextView longitude;
    private TextView latitudeValue;
    private TextView longitudeValue;
    private TextView speedValue;
    private TextView distanceValue;

    double myLatitude = 0;
    double myLongitude = 0;
    double mySpeed = 0;
    double myDistance = 0;

    MyAidlInterface mMyAidlInterface;

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       StartButton = (Button) findViewById(R.id.start_button);
       StopButton = (Button) findViewById(R.id.stop_button);
       StopButton.setEnabled(false);
       updateValues = (Button) findViewById(R.id.update_Values);
       latitude = (TextView) findViewById(R.id.latitude);
       longitude = (TextView) findViewById(R.id.longitude);
       latitudeValue = (TextView) findViewById(R.id.latitudeValue);
       longitudeValue = (TextView) findViewById(R.id.longitudeValue);
       speedValue = (TextView) findViewById(R.id.speed_Value);
       distanceValue = (TextView) findViewById(R.id.distance_Value);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void startLocService(View view){
        Intent I = new Intent(this,LocationService.class);
        bindService(I,mServiceConnection,BIND_AUTO_CREATE);
        StartButton.setEnabled(false);
        StopButton.setEnabled(true);
        //Toast.makeText(this, "binded from activity", Toast.LENGTH_SHORT).show();
    }
    public void stopLocService(View view){
        unbindService(mServiceConnection);
        StartButton.setEnabled(true);
        StopButton.setEnabled(false);
        //Toast.makeText(this, "unbinded from activity", Toast.LENGTH_SHORT).show();
    }
    public void updateValues(View view) throws RemoteException {
        myLatitude = mMyAidlInterface.get_Latitude();
        latitudeValue.setText(Double.toString(myLatitude));
        myLongitude = mMyAidlInterface.get_Longitude();
        longitudeValue.setText(Double.toString(myLongitude));
        mySpeed = mMyAidlInterface.get_AverageSpeed();
        speedValue.setText(Double.toString(mySpeed)+" Km/h");
        myDistance = mMyAidlInterface.get_Distance();
        distanceValue.setText(Double.toString(myDistance)+" m");
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mMyAidlInterface = MyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMyAidlInterface = null;
        }
    };

}
