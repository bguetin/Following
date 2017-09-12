package com.altran.bguetin.following;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    static TcpClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchTCPConnexion();

        Button followButton = (Button) findViewById(R.id.followingModeButton);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Following_Activity.class);
                startActivity(intent);
            }
        });

        Button wifiButton = (Button) findViewById(R.id.wifiModeButton);
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WifiOnlyActivity.class);
                startActivity(intent);
            }
        });

        Button gpsButton = (Button) findViewById(R.id.gpsModeButton);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,GpsOnlyActivity.class);
                startActivity(intent);
            }
        });

        Button sensorsButton = (Button) findViewById(R.id.sensorsModeButton);
        sensorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SensorsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void launchTCPConnexion(){
        System.out.println("launchTCPConnexion");
        new ConnectTask().execute("");

        // imgConnexion.setImageResource(R.drawable.circle_connexion_ok);

    }





    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {



            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();


            return null;
        }



        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }




    }

    public static void sendMessage(String a, String b, String c){
        if (mTcpClient != null) {

            //long time= System.currentTimeMillis();
            //android.util.Log.i("bob", " Time value in millisecinds "+time);
            mTcpClient.sendMessage(a + " " + b + " " + c );
        }
    }

    public static void sendToMatlabSensors(String a, String b, String c,String d,String e,String f,String g,String h,String i){
        if (mTcpClient != null) {
            String message = a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + g + " " + h + " " + i;
            System.out.println("To matab : " + message);
            //long time= System.currentTimeMillis();
            //android.util.Log.i("bob", " Time value in millisecinds "+time);
            mTcpClient.sendMessage(message);
        }
    }

    public static void sendToMatlabSensors(String a, String b, String c, String d, String e, String f){
        if (mTcpClient != null) {
            String message = a + " " + b + " " + c + " " + d + " " + e + " " + f;
            System.out.println("To matab : " + message);
            mTcpClient.sendMessage(message);
        }
    }


}
