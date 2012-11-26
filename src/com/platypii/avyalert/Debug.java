package com.platypii.avyalert;

import android.util.Log;


public class Debug {

    public static final boolean DEBUG = true; // TODO!! dev purposes only. disable in production!!
    public static final boolean DEBUG_NOTIFICATIONS = true; // disable billing for dev purposes
    
    static {
        if(DEBUG || DEBUG_NOTIFICATIONS) {
            Log.e("AvyAlert", "WARNING: Debug mode enabled!! Not for production!!");
        }
    }
    
}
