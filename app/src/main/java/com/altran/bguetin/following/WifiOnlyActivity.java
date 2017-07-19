package com.altran.bguetin.following;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static com.altran.bguetin.following.Following_Activity.calculateDistance;

public class WifiOnlyActivity extends AppCompatActivity {

    TextView textDistance;
    TextView textFrequency;
    TextView textSignal;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_only);

        wifiManager  = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        textDistance = (TextView) findViewById(R.id.textDistanceResult);
        textFrequency = (TextView) findViewById(R.id.textFrequencyResult);
        textSignal = (TextView) findViewById(R.id.textSignalResult);


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    getWifi();
                    SystemClock.sleep(1);
                }
            }
        }).start();

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

        System.out.println(sDistance + " m " + sFreq + " MHz " + sSignal + " dB");

        MainActivity.sendMessage(sDistance,sFreq,sSignal);
        System.out.println("aftersend");
    }

    public void refreshTxtViews(String sDistance, int freqInMHz, int signalLevelInDb){
        textDistance.setText(sDistance + " meters");
        textFrequency.setText(String.valueOf(freqInMHz + " MHz"));
        textSignal.setText(String.valueOf(signalLevelInDb) + " dB");
    }

}
