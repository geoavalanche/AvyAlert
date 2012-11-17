package com.platypii.avyalert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private ScrollView scrollView;
    
    // Region (banner)
    private ImageView regionView;

    // Advisory
    private View advisoryView;
    ImageView ratingIcon;
    TextView ratingLabel;
    TextView dateLabel;
    ImageView roseView;
    TextView detailsLabel;

    // Link
    private View linkView;
    private TextView centerLabel;
    
    private SharedPreferences sharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Shared Preferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Load regions
        if(regions == null)
            regions = new Regions(getApplicationContext());

        // Find views
        loading = (ProgressBar) findViewById(R.id.loading);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        regionView = (ImageView) findViewById(R.id.regionView);
        advisoryView = findViewById(R.id.advisoryView);
        linkView = findViewById(R.id.linkView);
        centerLabel = (TextView) findViewById(R.id.centerLabel);
        ratingIcon = (ImageView) findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        roseView = (ImageView) findViewById(R.id.roseView);
        detailsLabel = (TextView) findViewById(R.id.detailsLabel);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        linkView.setOnClickListener(linkListener);
        
        if(currentRegion == null) {
            // Load region from preferences
            String regionName = sharedPrefs.getString("currentRegion", null);
            currentRegion = regions.getRegion(regionName);
            currentAdvisory = null;
        }
        updateRegion();
        updateAdvisory();

        if(currentRegion == null) {
            // Show region dialog
            showRegionDialog();
        }

        // TODO: Also check if the currentAdvisory is stale
        if(currentAdvisory == null) {
            // Fetch the latest advisory
            fetchAdvisory();
        }
        
        // Update region data
        fetchRegionData();
        
        // Enable notifications (if subscribed and enabled)
        Alerter.enableNotifications(this);

    }

    /**
     * Fetch region data, then update views accordingly
     */
    private void fetchRegionData() {
        regions.fetchRegionData(new Callback<Regions>() {
            @Override
            public void callback(Regions result) {
                Log.v("Main", "New region data!");
                if(result != null && currentRegion != null) {
                    currentRegion = regions.getRegion(currentRegion.regionName);
                    if(currentRegion == null) {
                        // Current region no long exists
                        currentAdvisory = null;
                        updateAdvisory();
                        showRegionDialog();
                    } else {
                        // Fetch new advisory
                        fetchAdvisory();
                    }
                }
                updateRegion();
            }
        });

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
                setCurrentRegion(regionName);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Sets the current region, and updates the advisory if the region changed
     */
    private void setCurrentRegion(CharSequence regionName) {
        final Region newRegion = regions.getRegion(regionName);
        if(newRegion == null) {
            Log.w("RegionDialog", "Null region, wtf?");
            currentRegion = null;
            currentAdvisory = null;
            updateRegion();
            updateAdvisory();
        } else if(currentRegion == null || !currentRegion.regionName.equals(newRegion.regionName)) {
            // Change region
            currentRegion = newRegion;
            currentAdvisory = null;
            updateRegion();
            updateAdvisory();
            // Fetch new Advisory
            fetchAdvisory();
        } else {
            // Same region
            // TODO: refresh here?
            if(currentAdvisory == null) {
                fetchAdvisory();
            }
        }
        // Store to preferences
        final SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString("currentRegion", currentRegion==null? null : currentRegion.regionName);
        prefsEditor.commit();
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
     * Downloads the latest advisory asynchronously, and then updates the views
     */
    private void fetchAdvisory() {
        if(currentRegion != null) {
            loading.setVisibility(View.VISIBLE);
            currentRegion.fetchAdvisory(new Callback<Advisory>() {
                @Override
                public void callback(Advisory advisory) {
                    if(advisory != null && advisory.region.equals(currentRegion)) {
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
            roseView.setVisibility(View.GONE); // Hide for now, show when image is downloaded
            if(currentAdvisory.roseUrl != null && !currentAdvisory.roseUrl.equals("")) {
                final String roseUrl = currentAdvisory.roseUrl;
                // Asynchronously get image
                new AsyncTask<Void,Void,Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        return currentAdvisory.fetchImage();
                    }
                    @Override
                    protected void onPostExecute(Bitmap result) {
                        if(roseUrl.equals(currentAdvisory.roseUrl)) {
                            roseView.setImageBitmap(result);
                            roseView.setVisibility(View.VISIBLE);
                        }
                    }
                }.execute();
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
    
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SCROLL_X", scrollView.getScrollX());
        outState.putInt("SCROLL_Y", scrollView.getScrollY());
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int x = savedInstanceState.getInt("SCROLL_X", 0);
        final int y = savedInstanceState.getInt("SCROLL_Y", 0);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(x, y);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        // TODO: Stop AsyncTasks
        // GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

}
