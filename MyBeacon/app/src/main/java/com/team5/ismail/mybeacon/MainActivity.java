package com.team5.ismail.mybeacon;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;

import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 30000;

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private TextView beaconID;
    private TextView URL;
    private TextView voltage;
    private TextView temperature;
    private TextView distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        beaconID = (TextView) findViewById(R.id.Beacon_ID);
        URL = (TextView) findViewById(R.id.URL_value);
        voltage = (TextView) findViewById(R.id.voltage_value);
        temperature = (TextView) findViewById(R.id.temp_value);
        distance = (TextView) findViewById(R.id.distance_value);


        //scanLeDevice(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {

                    Log.d(TAG, "onLeScan: IN SCAN CALLBACK!");

                    //if (device.getAddress().equals("FD:57:E0:DC:63:40")) {
                        Log.d(TAG, "onLeScan: " + scanRecord);
                        List<ADStructure> structures =
                                ADPayloadParser.getInstance().parse(scanRecord);
                        //Log.e("mainActivity", "" + device.getAddress() + " name: " + device.getName());

                        // For each AD structure contained in the advertisement packet.
                        for (ADStructure structure : structures) {
                            // If the AD structure represents Eddystone UID.
                            if (structure instanceof EddystoneUID) {
                                // Eddystone UID
                                EddystoneUID es = (EddystoneUID) structure;

                                double dist = 0.0;
                                dist=Math.pow(10.0,((double) rssi + 70.0)/ -20.0);
                                Log.i("MyActivity","UID");
                                Log.i("MyActivity", "Tx Power = " + es.getTxPower());
                                Log.i("MyActivity", "Namespace ID = " + es.getNamespaceIdAsString());
                                Log.i("MyActivity", "Instance ID = " + es.getInstanceIdAsString());
                                Log.i("MyActivity", "Beacon ID = " + es.getBeaconIdAsString());

                                beaconID.setText(es.getBeaconIdAsString());
                                distance.setText(Double.toString(dist));

                        /*Log.d(TAG, ("Namespace ID = \n" + es.getNamespaceIdAsString()+
                                "\nInstance ID = " + es.getInstanceIdAsString()+
                                "\nBeacon ID = \n" + es.getBeaconIdAsString()+
                                "\nDistance: " + String.format("%.2f",dist)) );
*/
                                Log.i("MyActivity", "Disance: " + dist);
                                Log.i("MyActivity", "rssi "+rssi);
                                // As byte arrays if you want.
                                byte[] namespaceId = es.getNamespaceId();
                                byte[] instanceId = es.getInstanceId();
                                byte[] beaconId = es.getBeaconId();
                            }
                            // If the AD structure represents Eddystone URL.
                            else if (structure instanceof EddystoneURL) {
                                // Eddystone URL
                                EddystoneURL es = (EddystoneURL) structure;
                                Log.i("MyActivity","URL");
                                Log.i("MyActivity", "Tx Power = " + es.getTxPower());
                                Log.i("MyActivity", "URL = " + es.getURL());

                                URL.setText(es.getURL().toString());
                            }
                            // If the AD structure represents Eddystone TLM.
                            else if (structure instanceof EddystoneTLM) {
                                // Eddystone TLM
                                EddystoneTLM es = (EddystoneTLM) structure;

                                Log.i("MyActivity","TLM");
                                Log.i("MyActivity", "TLM Version = " + es.getTLMVersion());
                                Log.i("MyActivity", "Battery Voltage = " + es.getBatteryVoltage());
                                Log.i("MyActivity", "Beacon Temperature = " + es.getBeaconTemperature());
                                Log.i("MyActivity", "Advertisement Count = " + es.getAdvertisementCount());
                                Log.i("MyActivity", "Elapsed Time = " + es.getElapsedTime());

                                voltage.setText(Integer.toString(es.getBatteryVoltage()));
                                temperature.setText(Float.toString(es.getBeaconTemperature()) );

                            }
                        }
                    //}


                }
            };



}
