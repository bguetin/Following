package com.altran.bguetin.following;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Range;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import static com.altran.bguetin.following.Following_Activity.calculateDistance;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener,LocationListener{
    WifiManager wifiManager;
    SensorManager sensorManager;

    Sensor sensorGyro;
    Sensor sensorAcc;
    Sensor sensorMag;
    Sensor sensorGrav;
    Sensor sensor6DOF;

    double ax = 0.0;
    double ay = 0.0;
    double az = 0.0;   // these are the acceleration in x,y and z axis

    double gyroRotationX = 0.0;
    double gyroRotationY = 0.0;
    double gyroRotationZ = 0.0;

    DecimalFormat df;
    private LocationManager lm;
    double latitude;
    double longitude;
    double altitude;
    float accuracy;

    float[] mGravity = new float[3];
    float[] mQuatRot = new float[14];
    float[] mAcc;
    float[] mGeomagnetic;
    float[] linear_acceleration = new float[3];
    private static final boolean ADAPTIVE_ACCEL_FILTER = true;
    float lastAccel[] = new float[3];
    float accelFilter[] = new float[3];
    float orientationFilter[] = new float[3];
    float lastOrientation[] = new float[3];

    float azimut;
    float pitch;
    float roll;

    float accGravityX;
    float accGravityY;
    float accGravityZ;
    float alpha = (float) 0.8;

    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        wifiManager  = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorGrav = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensor6DOF = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);

        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_NORMAL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    getWifi();
                    SystemClock.sleep(1);
                }
            }
        }).start();


        mChart = (LineChart) findViewById(R.id.chart);
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
    }

    private void addRoll(double roll) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSetRoll();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float)roll), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSetRoll() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        //set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        //set.setCircleRadius(0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.WHITE);
        //set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }



    protected void getWifi(){
        // Level of current connection
        int signalLevelInDb = wifiManager.getConnectionInfo().getRssi();
        int freqInMHz = wifiManager.getConnectionInfo().getFrequency();
        double distance = calculateDistance(freqInMHz, signalLevelInDb);
        int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();

        String sDistance = new DecimalFormat("00.0").format(distance);
        String sFreq = String.format("%04d",freqInMHz);
        String sSignal = new DecimalFormat("000").format(signalLevelInDb);

        String sAzim = setStringFormat(azimut);
        String sPitch = setStringFormat(pitch);
        String sRoll = setStringFormat(roll);

        String sAccGravityX = setStringFormat(mGravity[0]);
        String sAccGravityY = setStringFormat(mGravity[1]);
        String sAccGravityZ = setStringFormat(mGravity[2]);

        String sAccX = setStringFormat(linear_acceleration[0]);
        String sAccY = setStringFormat(linear_acceleration[1]);
        String sAccZ = setStringFormat(linear_acceleration[2]);

        String sGyroRotX = setStringFormat(gyroRotationX);
        String sGyroRotY = setStringFormat(gyroRotationY);
        String sGyroRotZ = setStringFormat(gyroRotationZ);

        String sRotX = setStringFormat(mQuatRot[0]);
        String sRotY = setStringFormat(mQuatRot[1]);
        String sRotZ = setStringFormat(mQuatRot[2]);
        String sTransX = setStringFormat(mQuatRot[4]);
        String sTransY = setStringFormat(mQuatRot[5]);
        String sTransZ = setStringFormat(mQuatRot[6]);

        MainActivity.sendToMatlabSensors(sPitch,sRoll,sAzim,sAccX,sAccY,sAccZ);
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_POSE_6DOF){
            mQuatRot = event.values;
        }

        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            gyroRotationX=event.values[0];
            gyroRotationY=event.values[1];
            gyroRotationZ=event.values[2];
        }

        if (event.sensor.getType()==Sensor.TYPE_GRAVITY){
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            accGravityX = event.values[0];
            accGravityY = event.values[1];
            accGravityZ = event.values[2];

            //mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
            //mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
            //mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - mGravity[0];
            linear_acceleration[1] = event.values[1] - mGravity[1];
            linear_acceleration[2] = event.values[2] - mGravity[2];

            //filterAccelerometer(linear_acceleration[0],linear_acceleration[1],linear_acceleration[2]);

        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
                //filterOrientation(azimut, pitch, roll);

                addRoll(roll);

            }
        }

    }

    protected void filterOrientation(float yaw, float pitch, float roll){
        // high pass filter
        float updateFreq = 1000; // match this to your update speed
        float cutOffFreq = 0.9f;
        float RC = 1.0f / cutOffFreq;
        float dt = 1.0f / updateFreq;
        float filterConstant = RC / (dt + RC);
        float alpha = filterConstant;
        float kAccelerometerMinStep = 0.033f;
        float kAccelerometerNoiseAttenuation = 3.0f;

        if (ADAPTIVE_ACCEL_FILTER) {
            double toClamp = Math.abs(norm(orientationFilter[0], orientationFilter[1], orientationFilter[2]) - norm(yaw, pitch, roll)) / kAccelerometerMinStep - 1.0f;
            float d = Math.max(0, Math.min(1,(float) toClamp));
            alpha = d * filterConstant / kAccelerometerNoiseAttenuation + (1.0f - d) * filterConstant;
        }

        orientationFilter[0] = alpha * (orientationFilter[0] + yaw - lastOrientation[0]);
        orientationFilter[1] = alpha * (orientationFilter[1] + pitch - lastOrientation[1]);
        orientationFilter[2] = alpha * (orientationFilter[2] + roll - lastOrientation[2]);

        lastOrientation[0] = yaw;
        lastOrientation[1] = pitch;
        lastOrientation[2] = roll;
    }

    public double norm(float a, float b, float c){
        return Math.sqrt(Math.pow((double)a,2)+Math.pow((double)b,2)+Math.pow((double)c,2));
    }

    public String setStringFormat(float a){
        DecimalFormat decimalFormat;
        if(a<0){
            decimalFormat = new DecimalFormat("00.000000");
        }
        else {
            decimalFormat = new DecimalFormat("000.000000");
        }
        return decimalFormat.format(a);
    }

    public String setStringFormat(Double a){
        DecimalFormat decimalFormat;
        if(a<0){
            decimalFormat = new DecimalFormat("00.000000");
        }
        else {
            decimalFormat = new DecimalFormat("000.000000");
        }
        return decimalFormat.format(a);
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
    }

}
