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
    private static Region currentRegion = null; // The current region
    private static Advisory currentAdvisory = null; // The advisory currently being displayed

    // Views
    private ProgressBar loading;
    
    // Region (banner)
    private ImageView regionView;

    // Advisory
    private View advisoryView;
    ImageView ratingIcon;
    TextView ratingLabel;
    TextView dateLabel;
    TextView detailsLabel;

    // Link
    private View linkView;
    private TextView centerLabel;
    
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Shared Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Load regions
        if(regions == null)
            regions = new Regions(getApplicationContext());

        // Find views
        loading = (ProgressBar) findViewById(R.id.loading);
        regionView = (ImageView) findViewById(R.id.regionView);
        advisoryView = findViewById(R.id.advisoryView);
        linkView = findViewById(R.id.linkView);
        centerLabel = (TextView) findViewById(R.id.centerLabel);
        ratingIcon = (ImageView) findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        detailsLabel = (TextView) findViewById(R.id.detailsLabel);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        linkView.setOnClickListener(linkListener);
        
        // Push notifications
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
        
        if(currentRegion == null) {
            // Load region from preferences
            String regionName = prefs.getString("currentRegion", null);
            currentRegion = regions.getRegion(regionName);
            currentAdvisory = null;
        }
        updateRegion();
        updateAdvisory();

        if(currentRegion == null) {
            // Show region dialog
            showRegionDialog();
        }

        // Fetch the latest advisory
        fetchAdvisory();
    }

    /**
     * Loads the region into the current view
     */
    @SuppressLint("NewApi")
    private void updateRegion() {
        // Update UI
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
        } else {
            regionView.setVisibility(View.GONE);
            linkView.setVisibility(View.GONE);
        }
    }

    /**
     * Prompt the user to choose their region
     */
    private void showRegionDialog() {
        final CharSequence[] regionNames = regions.getRegionNames();
        final int index = currentRegion == null? -1 : regions.indexOf(currentRegion.regionName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Avalanche Region");
        builder.setItems(regionNames, null);
        builder.setSingleChoiceItems(regionNames, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CharSequence regionName = regionNames[which];
                final Region newRegion = regions.getRegion(regionName);
                if(newRegion == null) Log.w("RegionDialog", "Null region, wtf?");
                if(currentRegion == null || !currentRegion.regionName.equals(regionName)) {
                    // Change region
                    currentRegion = regions.getRegion(regionName);
                    currentAdvisory = null;
                    updateRegion();
                    updateAdvisory();
                    // Fetch new Advisory
                    fetchAdvisory();
                    // Store to preferences
                    final SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putString("currentRegion", regionName.toString());
                    prefsEditor.commit();
                }
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
                    if(advisory != null && advisory.regionName.equals(currentRegion.regionName)) {
                        currentAdvisory = advisory;
                        updateAdvisory();
                    }
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
    private void updateAdvisory() {
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
        details = details.replaceAll("(?si)</?(img|a).*?>", ""); // Remove images and links
        return details;
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
