package com.platypii.avyalert.billing;

import com.platypii.avyalert.Alerter;
import com.platypii.avyalert.R;
import com.platypii.avyalert.SettingsActivity;
import com.platypii.avyalert.billing.BillingService.RequestPurchase;
import com.platypii.avyalert.billing.BillingService.RestoreTransactions;
import com.platypii.avyalert.billing.Consts.PurchaseState;
import com.platypii.avyalert.billing.Consts.ResponseCode;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class BillingActivity extends Activity {
    
    private BillingService billingService;
    private PushPurchaseObserver pushPurchaseObserver;
    private Handler handler;

    private String payloadContents = null;

    // Views
    private Button buyButton;

    
    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase);
        
        // Views
        buyButton = (Button) findViewById(R.id.buyButton);
        
        // Enable action bar navigation
        if(android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(true);
        }

        // Set-up Billing
        handler = new Handler();
        pushPurchaseObserver = new PushPurchaseObserver(this, handler);
        billingService = new BillingService();
        billingService.setContext(this);

        // Check if billing is supported.
        ResponseHandler.register(pushPurchaseObserver);
        if(!billingService.checkBillingSupported(Consts.ITEM_TYPE_SUBSCRIPTION)) {
            Log.w("Billing", "In-App Subscription Billing not supported");
        }

        // Ready for purchase...
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do purchase
                Log.d("Billing", "Buying...");
                boolean success = billingService.requestPurchase(Consts.ITEM_PUSH_SUBSCRIPTION, Consts.ITEM_TYPE_SUBSCRIPTION, payloadContents);
                if(!success) {
                    Log.w("Billing", "Purchase failed");
                }
            }
        });
    }

    
    /**
     * Called when this activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(pushPurchaseObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billingService.unbind();
    }
    
    
    /**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends messages to this application so that we
     * can update the UI.
     */
    class PushPurchaseObserver extends PurchaseObserver {

        private static final String TAG = "PushNotificationPurchaseObserver";
        private static final String BILLING_INITIALIZED = "billingInitialized";

        private Activity activity;
        
        
        public PushPurchaseObserver(Activity activity, Handler handler) {
            super(activity, handler);
            this.activity = activity;
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
            if(Consts.DEBUG) {
                Log.i(TAG, "supported: " + supported);
            }
            if(supported) {
                restoreDatabase();
                buyButton.setEnabled(true);
                Alerter.enableNotifications(activity);
                // if(type.equals(Consts.ITEM_TYPE_SUBSCRIPTION)) subscriptionsSupport = true;
            } else {
                Log.i(TAG, "Billing not available");
            }
        }

        /**
         * If the database has not been initialized, we send a
         * RESTORE_TRANSACTIONS request to Android Market to get the list of purchased items
         * for this user. This happens if the application has just been installed
         * or the user wiped data. We do not want to do this on every startup, rather, we want to do
         * only when the database needs to be initialized.
         */
        private void restoreDatabase() {
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            boolean initialized = prefs.getBoolean("billingInitialized", false);
            if(!initialized) {
                Log.i("Billing", "Restoring transactions");
                billingService.restoreTransactions();
            }
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload) {
            if(Consts.DEBUG) {
                Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            }

            if(purchaseState == PurchaseState.PURCHASED) {
                if(itemId.equals("pushNotifications")) {
                    // TODO: Enable push notifications
                    Alerter.enableNotifications(activity);
                }
            }
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
            if(Consts.DEBUG) {
                Log.d(TAG, request.mProductId + ": " + responseCode);
            }
            if(responseCode == ResponseCode.RESULT_OK) {
                if(Consts.DEBUG) {
                    Log.i(TAG, "purchase was successfully sent to server");
                }
            } else if(responseCode == ResponseCode.RESULT_USER_CANCELED) {
                if(Consts.DEBUG) {
                    Log.i(TAG, "user canceled purchase");
                }
            } else {
                if(Consts.DEBUG) {
                    Log.i(TAG, "purchase failed");
                }
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
            if(responseCode == ResponseCode.RESULT_OK) {
                if(Consts.DEBUG) {
                    Log.d(TAG, "completed RestoreTransactions request");
                }
                // Update the shared preferences so that we don't perform a RestoreTransactions again.
                SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(BILLING_INITIALIZED, true);
                edit.commit();
            } else {
                if(Consts.DEBUG) {
                    Log.d(TAG, "RestoreTransactions error: " + responseCode);
                }
            }
        }
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                // Go back
                startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
