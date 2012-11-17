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
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

    private static Region currentRegion = null; // The current region
    private static Advisory currentAdvisory = null; // The advisory currently being displayed

    // Views
    private ScrollView scrollView;
    
    // Region (banner)
    private ImageView regionView;

    // Advisory
    private ImageView ratingIcon;
    private TextView ratingLabel;
    private TextView dateLabel;
    private ImageView roseView;
    private TextView detailsLabel;

    // Link
    private View advisoryLink;
    private TextView centerLabel;
    
    // Overlays
    private ProgressBar loadingIcon;
    private View refreshView;
    
    private SharedPreferences sharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Shared Preferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Load regions
        Regions.initRegionData(getApplicationContext());

        // Find views
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        regionView = (ImageView) findViewById(R.id.regionView);
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        ratingIcon = (ImageView) findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        roseView = (ImageView) findViewById(R.id.roseView);
        detailsLabel = (TextView) findViewById(R.id.detailsLabel);
        advisoryLink = findViewById(R.id.advisoryLink);
        centerLabel = (TextView) findViewById(R.id.centerLabel);
        loadingIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        refreshView = findViewById(R.id.refreshView);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        ratingIcon.setOnClickListener(infoListener);
        roseView.setOnClickListener(infoListener);
        refreshView.setOnClickListener(refreshListener);
        advisoryLink.setOnClickListener(linkListener);
        
        if(currentRegion == null) {
            // Load region from preferences
            String regionName = sharedPrefs.getString("currentRegion", null);
            currentRegion = Regions.getRegion(regionName);
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
        Regions.fetchRegionData(sharedPrefs, new Callback<Void>() {
            @Override
            public void callback(Void result) {
                Log.v("Main", "New region data!");
                if(currentRegion != null) {
                    currentRegion = Regions.getRegion(currentRegion.regionName);
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
                // Region dialog
                if(regionDialog != null) {
                    regionDialog.dismiss();
                    showRegionDialog();
                }
                updateRegion();
            }
        });

    }

    /**
     * Prompt the user to choose their region
     */
    private void showRegionDialog() {
        final CharSequence[] regionNames = Regions.getRegionNames();
        final int index = currentRegion == null? -1 : Regions.indexOf(currentRegion.regionName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Avalanche Region");
        builder.setItems(regionNames, null);
        builder.setSingleChoiceItems(regionNames, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CharSequence regionName = regionNames[which];
                setCurrentRegion(regionName);
                dialog.cancel();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                regionDialog = null;
            }
        });
        regionDialog = builder.show();
    }
    private AlertDialog regionDialog;

    /**
     * Sets the current region, and updates the advisory if the region changed
     */
    private void setCurrentRegion(CharSequence regionName) {
        final Region newRegion = Regions.getRegion(regionName);
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
            advisoryLink.setVisibility(View.GONE);
        }
    }

    /**
     * Downloads the latest advisory asynchronously, and then updates the views
     */
    private void fetchAdvisory() {
        final Region region = currentRegion;
        if(region != null) {
            refreshView.setVisibility(View.GONE);
            loadingIcon.setVisibility(View.VISIBLE);
            new AsyncTask<Void, Void, Advisory>() {
                @Override
                protected void onPreExecute() {}
                @Override
                protected Advisory doInBackground(Void... params) {
                    try {
                        return region.fetchAdvisory();
                    } catch(IOException e) {
                        Log.w(region.regionName, "Failed to download advisory");
                        return null;
                    }
                }
                @Override
                protected void onPostExecute(Advisory advisory) {
                    if(currentAdvisory == null && advisory == null) {
                        // Failed to load any advisory, show refresh button
                        refreshView.setVisibility(View.VISIBLE);
                    } else if(advisory != null && advisory.region.equals(currentRegion)) {
                        currentAdvisory = advisory;
                        updateAdvisory();
                    }
                    loadingIcon.setVisibility(View.GONE);
                    
                    // Notify user
                    Alerter.notifyUser(getApplicationContext(), advisory);
                }
            }.execute();
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
            advisoryLink.setVisibility(View.VISIBLE);
        } else {
            ratingIcon.setVisibility(View.GONE);
            ratingLabel.setVisibility(View.GONE);
            dateLabel.setVisibility(View.GONE);
            roseView.setVisibility(View.GONE);
            detailsLabel.setVisibility(View.GONE);
            advisoryLink.setVisibility(View.GONE);
        }
    }
    
    /**
     * Removes img tags, anchor tags, etc
     */
    private static String cleanHtml(String details) {
        details = details.replaceAll("(?si)</?(img|a).*?>", ""); // Remove images and links
        return details;
    }

    // Listeners
    private View.OnClickListener regionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showRegionDialog();
        }
    };
    private View.OnClickListener linkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentRegion != null) {
                // Open Advisory URL
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRegion.advisoryUrl));
                startActivity(browserIntent);
            } else {
                Log.w("MainActivity", "User clicked link, but no region selected. WTF?");
            }
        }
    };
    private View.OnClickListener refreshListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshView.setVisibility(View.GONE);
            if(currentRegion != null) {
                fetchAdvisory();
            } else {
                showRegionDialog();
            }
        }
    };
    private View.OnClickListener infoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Open information
            startActivity(new Intent(MainActivity.this, InfoActivity.class));
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
        if(regionDialog != null) {
            regionDialog.cancel();
        }
        // TODO: Stop AsyncTasks?
        // GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

}
