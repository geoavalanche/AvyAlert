package com.platypii.avyalert;

import com.google.android.gcm.GCMBaseIntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Receives push notification events
 * @author platypii
 */
public class GCMIntentService extends GCMBaseIntentService {
    
    public static final String SENDER_ID = "381193425580"; // Google Project ID


    @Override
    protected void onRegistered(Context context, String regId) {
        Log.d("Push", "onRegistered(" + regId + ")");
        ServerUtilities.register(context, regId);
    }
    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.d("Push", "onUnregistered(" + regId + ")");
        ServerUtilities.unregister(context, regId);
    }
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d("Push", "onMessage(" + intent + ")");
        // TODO: Update avalanche advisory
        // Alerter.notifyUser(context, advisory);
    }
    @Override
    protected void onError(Context context, String errorId) {
        Log.d("Push", "onError(" + errorId + ")");
    }
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.d("Push", "onRecoverableError(" + errorId + ")");
        return false;
    }
    
    
}
