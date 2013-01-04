package com.platypii.avyalert;

import android.util.Log;


public class Debug {

    public static final boolean DEBUG = false; // TODO!! dev purposes only. disable in production!!
    public static final boolean ENABLE_NOTIFICATIONS = false; // disable billing for dev purposes
    
    static {
        if(DEBUG) {
            Log.e("AvyAlert", "WARNING: Debug mode enabled!! Not for production!!");
        }
    }
    
}
