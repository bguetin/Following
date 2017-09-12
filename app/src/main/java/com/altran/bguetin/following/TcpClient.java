package com.altran.bguetin.following;

import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bguetin on 19/05/2017.
 */

public class TcpClient {

    public static final String SERVER_IP = "10.240.217.215"; //server IP address "10.245.4.18"
    public static final int SERVER_PORT = 3001;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    Socket socket =  null;

    ImageView connection = null;
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }


    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        System.out.println("launch " + SERVER_IP + " " + SERVER_PORT);
        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();


                    System.out.println("sizeServerMessage " + mServerMessage.length());

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }


                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);


            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);


        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }


    public boolean isClosed(){

        if (socket!=null)
            return socket.isClosed();
        else
            return true;

    }

}
