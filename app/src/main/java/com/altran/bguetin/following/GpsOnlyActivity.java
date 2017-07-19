package com.altran.bguetin.following;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GpsOnlyActivity extends AppCompatActivity implements LocationListener{

    private LocationManager lm;

    TextView textGPSRes;
    TextView textAccuracyRes;
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_only);
        textGPSRes = (TextView) findViewById(R.id.textGpsResult);
        textAccuracyRes = (TextView) findViewById(R.id.textAccuracyGpsResult);

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

    protected void getGPSLocation(Location location){
        latitude = location.getLatitude();
        String sLatitude = String.format("%.6f",latitude);
        longitude = location.getLongitude();
        String sLongitude = String.format("%.6f",longitude);
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
        textGPSRes.setText(sLatitude + "° N\n" + sLongitude + "° E");
        textAccuracyRes.setText(String.valueOf(accuracy) + " m");
        //System.out.println(sLatitude + "° N and " + sLongitude + "° E");
    }
}
