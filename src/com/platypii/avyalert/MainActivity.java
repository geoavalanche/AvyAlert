package com.platypii.avyalert;

import java.io.IOException;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.regions.Region;
import com.platypii.avyalert.regions.Regions;
import android.net.Uri;
import android.os.AsyncTask;
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

    private static Region currentRegion; // The current region
    private static Advisory currentAdvisory; // The advisory currently being displayed

    // Views
    private ImageView regionView;
    private View advisoryView;
    private View linkView;
    private TextView centerLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Find views
        regionView = (ImageView) findViewById(R.id.regionView);
        advisoryView = findViewById(R.id.advisoryView);
        linkView = findViewById(R.id.linkView);
        centerLabel = (TextView) findViewById(R.id.centerLabel);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        linkView.setOnClickListener(linkListener);
        
        // Load region from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String regionName = prefs.getString("region", null);
        
        if(regionName != null) {
            currentRegion = Regions.getRegion(regionName);
        } else {
            showRegionDialog();
        }

        // Set the region
        updateRegion();
        
        // Load the latest advisory
        fetchAdvisory();

    }

    
    /**
     * Updates the region banner for the current region
     */
    @SuppressLint("NewApi")
    private void updateRegion() {
        if(currentRegion != null) {
            if(android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
                ActionBar actionBar = getActionBar();
                actionBar.setSubtitle(currentRegion.getName());
            }
            regionView.setImageResource(currentRegion.getBanner());
            regionView.setContentDescription(currentRegion.getName());
            regionView.setVisibility(View.VISIBLE);
            centerLabel.setText(currentRegion.getCenterName());
        } else {
            regionView.setVisibility(View.GONE);
            linkView.setVisibility(View.GONE);
        }
    }

    /**
     * Prompt the user to choose their region
     */
    private void showRegionDialog() {
        final CharSequence[] regions = Regions.getRegionNames();
        int index = currentRegion == null? -1 : Regions.indexOf(currentRegion.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Avalanche Region");
        builder.setItems(regions, null);
        builder.setSingleChoiceItems(regions, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CharSequence regionName = regions[which];
                // Store to preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("region", regionName.toString());
                prefsEditor.commit();
                // Update
                currentRegion = Regions.getRegion(regionName);
                currentAdvisory = null;
                updateRegion();
                updateAdvisory();
                fetchAdvisory();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Downloads the latest advisory as an AsyncTask, and then updates the views
     */
    private void fetchAdvisory() {
        if(currentRegion != null) {
            final ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
            new AsyncTask<Void, Void, Advisory>() {
                @Override
                protected void onPreExecute() {
                    loading.setVisibility(View.VISIBLE);
                }
                @Override
                protected Advisory doInBackground(Void... params) {
                    // Keep retrying
                    while(true) {
                        try {
                            currentRegion = Regions.getCurrentRegion(getApplicationContext());
                            return currentRegion.getAdvisory();
                        } catch(IOException e) {
                            Log.w("MainActivity", "Failed to download advisory");
                        }
                        // Sleep
                        try {
                            Thread.sleep(8000);
                        } catch(InterruptedException e) {}
                    }
                }
                @Override
                protected void onPostExecute(Advisory advisory) {
                    MainActivity.currentAdvisory = advisory;
                    updateAdvisory();
                    loading.setVisibility(View.GONE);
                    
                    // Notify user
                    Alerter.notifyUser(MainActivity.this, advisory);
                }
            }.execute();
        } else {
            currentAdvisory = null;
        }
    }
    
    /**
     * Updates the text views for the current advisory
     */
    private void updateAdvisory() {
        TextView dangerLabel = (TextView) findViewById(R.id.dangerLabel);
        TextView ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        TextView dateLabel = (TextView) findViewById(R.id.dateLabel);
        TextView detailsLabel = (TextView) findViewById(R.id.detailsLabel);
        
        if(currentAdvisory != null) {
            ratingLabel.setBackgroundColor(AvalancheRisk.getBackgroundColor(currentAdvisory.rating));
            if(currentAdvisory.rating != Rating.NONE) {
                ratingLabel.setText(currentAdvisory.rating.toString());
                ratingLabel.setTextColor(AvalancheRisk.getForegroundColor(currentAdvisory.rating));
                dangerLabel.setVisibility(View.VISIBLE);
                ratingLabel.setVisibility(View.VISIBLE);
            } else {
                dangerLabel.setVisibility(View.GONE);
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
     * Removes img tags from a String
     */
    private String cleanHtml(String details) {
        StringBuffer buf = new StringBuffer();
        details = details.replaceAll("<(img|IMG)", "<img"); // Handle img or IMG
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRegion.getAdvisoryUrl()));
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
