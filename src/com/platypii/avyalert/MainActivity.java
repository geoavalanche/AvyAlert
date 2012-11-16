package com.platypii.avyalert;

import com.google.android.gcm.GCMRegistrar;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.regions.Region;
import com.platypii.avyalert.regions.Regions;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Main application activity
 * @author platypii
 */
public class MainActivity extends Activity {

    static Regions regions; // The avalanche reporting regions
    private static Region currentRegion; // The current region
    private static Advisory currentAdvisory; // The advisory currently being displayed

    // Views
    private ProgressBar loading;
    private ImageView regionView;
    private View advisoryView;
    private View linkView;
    private TextView centerLabel;
    
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Load regions
        if(regions == null)
            regions = new Regions(getApplicationContext());

        // Find views
        loading = (ProgressBar) findViewById(R.id.loading);
        regionView = (ImageView) findViewById(R.id.regionView);
        advisoryView = findViewById(R.id.advisoryView);
        linkView = findViewById(R.id.linkView);
        centerLabel = (TextView) findViewById(R.id.centerLabel);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        linkView.setOnClickListener(linkListener);
        
        // Push notification
        try {
            GCMRegistrar.checkDevice(this);
            GCMRegistrar.checkManifest(this);
            final String regId = GCMRegistrar.getRegistrationId(this);
            if(regId.equals("")) {
                GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
            } else {
                Log.v("Push", "Already registered for push notifications");
            }
        } catch(UnsupportedOperationException e) {
            Log.w("Push", "Push notifications not supported");
        }
        
        // Load region from preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String regionName = prefs.getString("currentRegion", null);
        if(regionName != null) {
            setRegion(regionName);
        } else {
            showRegionDialog();
        }

    }

    /**
     * Sets the current region to the given region, and fetches a new advisory
     */
    @SuppressLint("NewApi")
    private void setRegion(CharSequence regionName) {
        // Change region
        currentRegion = regions.getRegion(regionName);
        setAdvisory(null);
        if(currentRegion != null) {
            // Update view
            if(android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
                ActionBar actionBar = getActionBar();
                actionBar.setSubtitle(currentRegion.regionName);
            }
            regionView.setImageResource(currentRegion.getBannerImage());
            regionView.setContentDescription(currentRegion.regionName);
            regionView.setVisibility(View.VISIBLE);
            centerLabel.setText("from " + currentRegion.centerName);
            
            // Update Advisory
            fetchAdvisory();
        } else {
            regionView.setVisibility(View.GONE);
            linkView.setVisibility(View.GONE);
        }
        // Store to preferences
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("currentRegion", regionName.toString());
        prefsEditor.commit();
    }

    /**
     * Prompt the user to choose their region
     */
    private void showRegionDialog() {
        final CharSequence[] regionNames = regions.getRegionNames();
        int index = currentRegion == null? -1 : regions.indexOf(currentRegion.regionName);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Avalanche Region");
        builder.setItems(regionNames, null);
        builder.setSingleChoiceItems(regionNames, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CharSequence regionName = regionNames[which];
                // Update
                setRegion(regionName);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Downloads the latest advisory asynchronously, and then updates the views
     */
    private void fetchAdvisory() {
        if(currentRegion != null) {
            loading.setVisibility(View.VISIBLE);
            currentRegion.fetchAdvisory(new Callback<Advisory>() {
                @Override
                public void callback(Advisory advisory) {
                    setAdvisory(advisory);
                    loading.setVisibility(View.GONE);
                    
                    // Notify user
                    Alerter.notifyUser(MainActivity.this, advisory);
                }
            });
        }
    }
    
    /**
     * Loads an advisory into the current view
     */
    private void setAdvisory(Advisory advisory) {
        currentAdvisory = advisory;
        ImageView ratingIcon = (ImageView) findViewById(R.id.ratingIcon);
        TextView ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        TextView dateLabel = (TextView) findViewById(R.id.dateLabel);
        TextView detailsLabel = (TextView) findViewById(R.id.detailsLabel);
        
        if(currentAdvisory != null) {
            ratingLabel.setBackgroundColor(AvalancheRisk.getBackgroundColor(currentAdvisory.rating));
            if(currentAdvisory.rating != Rating.NONE) {
                ratingIcon.setImageResource(AvalancheRisk.getImage(currentAdvisory.rating));
                ratingLabel.setText(currentAdvisory.rating.toString());
                ratingLabel.setTextColor(AvalancheRisk.getForegroundColor(currentAdvisory.rating));
                ratingIcon.setVisibility(View.VISIBLE);
                ratingLabel.setVisibility(View.VISIBLE);
            } else {
                ratingIcon.setVisibility(View.GONE);
                ratingLabel.setVisibility(View.GONE);
            }
            if(currentAdvisory.date == null || currentAdvisory.date.equals("")) {
                dateLabel.setVisibility(View.GONE);
            } else {
                dateLabel.setText("Date: " + currentAdvisory.date);
                dateLabel.setVisibility(View.VISIBLE);
            }
            // Details
            String html = cleanHtml(currentAdvisory.details);
            detailsLabel.setText(Html.fromHtml(html));
            detailsLabel.setVisibility(View.VISIBLE);
            // Link
            advisoryView.setVisibility(View.VISIBLE);
            linkView.setVisibility(View.VISIBLE);
        } else {
            advisoryView.setVisibility(View.GONE);
            linkView.setVisibility(View.GONE);
        }
    }

    /**
     * Removes img tags, anchor tags, etc
     */
    private static String cleanHtml(String details) {
        StringBuffer buf = new StringBuffer();
        details = details.replaceAll("<(img|IMG)", "<img"); // Handle img or IMG
        details = details.replaceAll("</?[aA].*>", ""); // Handle img or IMG
        int i = 0;
        while(i != -1 && i < details.length()) {
            int j = details.indexOf("<img", i + 1);
            if(j == -1) j = details.length();
            buf.append(details.substring(i, j));
            i = details.indexOf(">", j);
            if(i != -1) i++;
        }
        return buf.toString();
    }

    private View.OnClickListener regionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Show region dialog
            showRegionDialog();
        }
    };

    private View.OnClickListener linkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Open Advisory URL
            if(currentRegion != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRegion.advisoryUrl));
                startActivity(browserIntent);
            } else {
                Log.w("MainActivity", "User clicked link, but no region selected. WTF?");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_region:
                // Show region dialog
                showRegionDialog();
                return true;
            case R.id.menu_settings:
                // Open settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
