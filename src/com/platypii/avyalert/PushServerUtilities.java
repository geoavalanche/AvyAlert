/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.platypii.avyalert;

import com.google.android.gcm.GCMRegistrar;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Helper class used to communicate with the push server.
 */
public final class PushServerUtilities {

    // Base URL of PlatypiiIndustries Server
    private static final String SERVER_URL = Debug.DEBUG? "http://localhost:2048/avalanche" : "http://platypiiindustries.com:2048/avalanche";
    

    // Google API project id registered to use GCM.
    private static final String SENDER_ID = "381193425580";


    /**
     * Register this account/device pair with platypiiindustries.
     * DO NOT CALL FROM UI THREAD!
     */
    static void register(final Context context, final String regId) {
        Log.i("PushUtilities", "registering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        // Once GCM returns a registration id, we need to register with PlatypiiIndustries
        try {
            Log.d("PushUtilities", "Trying to register device");
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, true);
            Log.d("PushUtilities", "Server added device");
            return;
        } catch(IOException e) {
            // Here we are simplifying and retrying on any error; in a real application,
            // it should retry only on recoverable errors (like HTTP error code 503).
            Log.e("PushUtilities", "Failed to register with PlatypiiIndustries.");
            // TODO: Try again later
        }
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Log.i("PushUtilities", "unregistering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            Log.d("PushUtilities", "Server removed device");
        } catch(IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            Log.d("PushUtilities", "Failed to unregister device");
        }
    }

    /**
     * Issue a POST request to the server.
     * 
     * @param endpoint POST address.
     * @param params request parameters.
     * 
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params) throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while(iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if(iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v("PushUtilities", "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if(status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }
}
