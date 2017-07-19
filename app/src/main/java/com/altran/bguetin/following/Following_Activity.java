package com.altran.bguetin.following;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Following_Activity extends AppCompatActivity implements LocationListener {

    private LocationManager lm;

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    TextView textGPSRes;
    TextView textLevel;
    TextView textFrequency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following_mode);
        textGPSRes = (TextView) findViewById(R.id.textGPSResult);
        textLevel = (TextView) findViewById(R.id.textLevelResult);
        textFrequency = (TextView) findViewById(R.id.textFrequencyResult);

        Button updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifi();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        lm.getBestProvider(criteria,true);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        getGPSLocation(location);
        getWifi();
        //Toast.makeText(this,"New position",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String msg = "onProviderDisabled";
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        String msg = "onProviderEnabled";
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
        String msg = "new status" + newStatus;
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static double calculateDistance(int freqInMHz, int signalLevelInDb) {
        return Math.pow(10.0, (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0);
    }

    protected void getWifi(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Level of a Scan Result
        //List<ScanResult> wifiList = wifiManager.getScanResults();
        //for (ScanResult scanResult : wifiList) {
          //  int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
            //System.out.println("Level is " + level + " out of 5");}

        // Level of current connection
        int signalLevelInDb = wifiManager.getConnectionInfo().getRssi();
        int freqInMHz = wifiManager.getConnectionInfo().getFrequency();
        int level = WifiManager.calculateSignalLevel(signalLevelInDb, 5);
        //System.out.println("Level is " + level + " out of 5");
        double distance = calculateDistance(freqInMHz, signalLevelInDb);
        String sDistance = String.format("%.3f",distance);
        textLevel.setText(sDistance + " meters");
        textFrequency.setText(String.valueOf(freqInMHz + " MHz"));
    }

    protected void getGPSLocation(Location location){
        latitude = location.getLatitude();
        String sLatitude = String.format("%.7f",latitude);
        longitude = location.getLongitude();
        String sLongitude = String.format("%.7f",longitude);
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
        textGPSRes.setText(sLatitude + "° N\n" + sLongitude + "° E");
    }

}