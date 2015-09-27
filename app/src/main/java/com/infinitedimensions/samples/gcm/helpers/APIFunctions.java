package com.infinitedimensions.samples.gcm.helpers;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.wordpress.android.BuildConfig;
import org.wordpress.android.WordPress;

import android.util.Log;

public class APIFunctions {

    private JSONParser jsonParser;
    private static String updateDeviceURL = BuildConfig.API_URL + "/users/edit_user_device/";
    private static String sendMessageUrl = BuildConfig.API_URL + "/users/send_message/";

    // constructor
    public APIFunctions(){
        jsonParser = new JSONParser();
    }

    public JSONObject updateUserDevice(String regId, String username) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("regId", regId));

        JSONObject json = jsonParser.getJSONFromUrl(updateDeviceURL, params);
        // return json
        return json;
    }
    public JSONObject sendMessage(String username, String message) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("message_string", message));
        Log.d("message", username + message + sendMessageUrl + ": params");
        JSONObject json = jsonParser.getJSONFromUrl(sendMessageUrl, params);
        // return json
        return json;
    }
}