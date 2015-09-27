package com.infinitedimensions.samples.gcm;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.infinitedimensions.samples.gcm.helpers.APIFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


public class GCMApp extends Application {
    public static String versionName;
    public static DBHandler wpDB;

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        versionName = "";
    }


    private void initWpDb() {
        if (!createAndVerifyWpDb()) {
            wpDB = new DBHandler(this);
        }
    }

    private boolean createAndVerifyWpDb() {
        try {
            wpDB = new DBHandler(this);
            // verify account data

            return true;
        } catch (SQLiteException sqle) {
            return false;
        } catch (RuntimeException re) {
            return false;
        }
    }

    public static Context getContext() {
        return mContext;
    }


    //GCM stuff
    private  final int MAX_ATTEMPTS = 5;
    private  final int BACKOFF_MILLI_SECONDS = 2000;
    private  final Random random = new Random();


    // Register this account with the server.
    public void register(final Context context, final String regId) {

        Log.i(GCMConfigORG.TAG, "registering device (regId = " + regId + ")");

        String serverUrl = GCMConfigORG.YOUR_SERVER_URL;

        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {

            Log.d(GCMConfigORG.TAG, "Attempt #" + i + " to register");

            //Send Broadcast to Show message on screen
            //displayMessageOnScreen(context, context.getString( R.string.server_registering, i, MAX_ATTEMPTS));

            // Post registration values to web server
            //post(serverUrl, params);

            //update user profile with device id
                String username = "";

                APIFunctions userFunction = new APIFunctions();
                JSONObject json = userFunction.updateUserDevice(regId, username);
                String responseMessage = "";
                if(json!=null) {
                    try {
                        String res = json.getString("result");
                        if (res.equals("OK")) {
                            responseMessage = json.getString("message");

                            //set device registered in preferences
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("rD", "1");
                            editor.commit();

                        } else {
                            responseMessage = json.getString("error");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    GCMRegistrar.setRegisteredOnServer(context, true);

                    //Send Broadcast to Show message on screen
                    String message = context.getString(R.string.server_registered);
                    //displayMessageOnScreen(context, message);
                }

            return;
        }

        String message = getApplicationContext().getString(R.string.server_register_error,
                MAX_ATTEMPTS);

        //Send Broadcast to Show message on screen
        //ageOnScreen(context, message);
    }

    //Function to display simple Alert Dialog
    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Set Dialog Title
        alertDialog.setTitle(title);

        // Set Dialog Message
        alertDialog.setMessage(message);

        if(status != null)
            // Set alert dialog icon
            alertDialog.setIcon( R.mipmap.ic_launcher);

        // Set OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // Show Alert Message
        alertDialog.show();
    }



    // Unregister this account/device pair within the server.
    public void unregister(final Context context, final String regId) {

        Log.i(GCMConfigORG.TAG, "unregistering device (regId = " + regId + ")");

        String serverUrl = GCMConfigORG.YOUR_SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);

        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = context.getString(R.string.server_unregistered);
            //ageOnScreen(context, message);
        } catch (IOException e) {

            // At this point the device is unregistered from GCM, but still
            // registered in the our server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.

            String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            //ageOnScreen(context, message);
        }
    }

    // Issue a POST request to the server.
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {

        URL url;
        try {

            url = new URL(endpoint);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();

        Log.v(GCMConfigORG.TAG, "Posting '" + body + "' to " + url);

        byte[] bytes = body.getBytes();

        HttpURLConnection conn = null;
        try {

            Log.e("URL", "> " + url);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();

            // If response is not success
            if (status != 200) {

                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }



    // Checking for all possible internet providers
    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    // Notifies UI to display a message.
    public void displayMessageOnScreen(Context context, String message) {

        Intent intent = new Intent(GCMConfigORG.DISPLAY_MESSAGE_ACTION);
        intent.putExtra(GCMConfigORG.EXTRA_MESSAGE, message);

        // Send Broadcast to Broadcast receiver with message
        context.sendBroadcast(intent);

    }
    private PowerManager.WakeLock wakeLock;

    public  void acquireWakeLock(Context context) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");

        wakeLock.acquire();
    }

    public  void releaseWakeLock() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }
}