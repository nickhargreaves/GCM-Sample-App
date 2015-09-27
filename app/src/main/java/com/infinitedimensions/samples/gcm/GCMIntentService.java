package com.infinitedimensions.samples.gcm;

import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.infinitedimensions.samples.gcm.models.Message;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";

    private GCMApp aController = null;

    private int messageType;
    private String message;
    private String assignmentID;
    private String assignmentDeadline;
    private String user;
    private String post_id;
    private String receipt;
    private String payment_id;
    private String pay_amount;

    private Bitmap iconFromUrl;
    public GCMIntentService() {
        // Call extended class Constructor GCMBaseIntentService
        super(GCMConfigORG.GCM_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {

        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        if(aController == null)
            aController = (GCMApp) getApplicationContext();

        Log.i(TAG, "Device registered: regId = " + registrationId);
        aController.displayMessageOnScreen(context, "Your device registred with GCM");
        aController.register(context, registrationId);
    }

    /**
     * Method called on device unregistred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if(aController == null)
            aController = (GCMApp) getApplicationContext();
        Log.i(TAG, "Device unregistered");
        aController.displayMessageOnScreen(context, getString(R.string.gcm_unregistered));
        aController.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message from GCM server
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {

        if(aController == null)
            aController = (GCMApp) getApplicationContext();

        Log.i(TAG, "Received message");

        //compose message depending on type
        if(intent.hasExtra("chat")){
            messageType = 2;
            message = intent.getExtras().getString("chat");
            user = intent.getExtras().getString("user");

            iconFromUrl = null;//iconFromUrl(intent.getExtras().getString("icon_url"));

            generateChatNotification();
        }

        aController.displayMessageOnScreen(context, message);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {

        if(aController == null)
            aController = (GCMApp) getApplicationContext();

        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        aController.displayMessageOnScreen(context, message);
        // notifies user
        //generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {

        if(aController == null)
            aController = (GCMApp) getApplicationContext();

        Log.i(TAG, "Received error: " + errorId);
        aController.displayMessageOnScreen(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {

        if(aController == null)
            aController = (GCMApp) getApplicationContext();

        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        aController.displayMessageOnScreen(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }


    private void generateChatNotification() {
        if(iconFromUrl==null){
            iconFromUrl = BitmapFactory.decodeResource(getResources(), R.drawable.me_icon_support);
        }
        //insert to db
        Message chat = new Message();
        chat.setMessage(message.trim());
        chat.setIsMine("2");
        GCMApp.wpDB.addMessage(chat);

        //create intent
        Intent chatIntent1 = new Intent(getApplicationContext(), ChatActivity.class);
        PendingIntent chatIntent = PendingIntent.getActivity(this, 0, chatIntent1, 0);

        //build notification
        Notification notif = new Notification.Builder(getApplicationContext())
                .setContentTitle(user)
                .setContentText(message)
                .setSmallIcon(R.drawable.me_icon_support)
                .setLargeIcon(iconFromUrl)
                .setStyle(new Notification.InboxStyle()
                        .addLine(message)
                        .setBigContentTitle(user)/*
                        .setSummaryText(assignmentDeadline)*/)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.mipmap.ic_reply, "Reply", chatIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(56, notif);
    }


    public static void clearNotificationsMap() {
    }

    public static int PUSH_NOTIFICATION_ID=0;

    public boolean shouldCircularizeNoteIcon(String type) {
        return false;
    }
}