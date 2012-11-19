package com.platypii.avyalert;

import java.io.IOException;
import com.platypii.avyalert.R;
import com.platypii.avyalert.data.Advisory;
import com.platypii.avyalert.data.Region;
import com.platypii.avyalert.data.Regions;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;


/**
 * Main application activity
 * @author platypii
 */
public class MainActivity extends Activity {

    private static Region currentRegion = null; // The current region
    private static Advisory currentAdvisory = null; // The advisory currently being displayed

    // Views
    private ScrollView scrollView;
    private View mainPanel;
    
    // Region (banner)
    private ImageView regionView;

    // Advisory
    private View advisoryPanel;
    private View advisoryLink;
    
    // Overlays
    private int runningTasks = 0; // Number of AsyncTasks running the background
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
        mainPanel = findViewById(R.id.mainPanel);
        regionView = (ImageView) findViewById(R.id.regionView);
        advisoryPanel = findViewById(R.id.advisoryPanel);
        advisoryLink = findViewById(R.id.advisoryLink);
        loadingIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        refreshView = findViewById(R.id.refreshView);

        // Set Click Listeners
        regionView.setOnClickListener(regionListener);
        findViewById(R.id.ratingIcon).setOnClickListener(infoListener);
        findViewById(R.id.roseView).setOnClickListener(infoListener);
        refreshView.setOnClickListener(refreshListener);
        advisoryLink.setOnClickListener(linkListener);
        
        // Check if activity was opened with a specific region
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("com.platypii.avyalert.currrentRegion")) {
            String regionName = intent.getStringExtra("com.platypii.avyalert.currentRegion");
            setCurrentRegion(Regions.getRegion(regionName));
            Log.v("Main", "Opening main activity with region: " + regionName);
        } else {
            if(currentRegion == null) {
                // Load region from preferences
                String regionName = sharedPrefs.getString("currentRegion", null);
                setCurrentRegion(Regions.getRegion(regionName));
            } else {
                setCurrentRegion(currentRegion);
            }
            if(currentAdvisory != null && currentAdvisory.region.equals(currentRegion)) {
                // Region and advisory still exist from previous MainActivity instance
                // TODO: Check if the currentAdvisory is stale
                setCurrentAdvisory(currentAdvisory);
            } else {
                setCurrentAdvisory(null);
            }
        }

        if(currentRegion == null) {
            // Show region dialog
            showRegionDialog();
        }

        // Update region data
        fetchRegionData();
        
        // Enable notifications (if subscribed and enabled)
        Alerter.enableNotifications(this);

    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v("Main", "Main.onNewIntent("+intent+")");
        if(intent != null) {
            String regionName = intent.getStringExtra("com.platypii.avyalert.currentRegion");
            setCurrentRegion(Regions.getRegion(regionName));
            Log.v("Main", "Changing main activity to region: " + regionName);
        }
    }

    /**
     * Fetch region data, then update views accordingly
     */
    private void fetchRegionData() {
        Regions.fetchRegionData(sharedPrefs, new Callback<Boolean>() {
            @Override
            public void callback(Boolean result) {
                if(result) {
                    Log.v("Main", "New region data!");
                    currentRegion = (currentRegion == null)? null : Regions.getRegion(currentRegion.regionName);
                    if(currentRegion == null) {
                        // Current region no long exists
                        setCurrentRegion(null);
                        showRegionDialog();
                    } else {
                        // Fetch new advisory
                        fetchAdvisory();
                    }
                    // Region dialog
                    if(regionDialog != null) {
                        regionDialog.dismiss();
                        showRegionDialog();
                    }
                    updateRegion();
                }
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
                setCurrentRegion(Regions.getRegion(regionName));
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
    private void setCurrentRegion(Region newRegion) {
        if(newRegion == null) {
            Log.w("RegionDialog", "Null region, wtf?");
            currentRegion = null;
            setCurrentAdvisory(null);
            updateRegion();
        } else if(currentRegion == null || !currentRegion.equals(newRegion)) {
            // Change region
            currentRegion = newRegion;
            setCurrentAdvisory(null);
            updateRegion();
            // Fetch new Advisory
            fetchAdvisory();
        } else {
            // Same region
            updateRegion();
            // TODO: refresh here if stale
            // TODO: refresh here always?
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
            final String currentRegionName = currentRegion.regionName; // The name of the region being fetched 
            currentRegion.fetchBannerImage(regionView, new Callback<Bitmap>() {
                @Override
                public void callback(Bitmap result) {
                    // If the region hasn't changed since we started, load the image
                    if(currentRegionName.equals(currentRegion.regionName))
                        regionView.setImageBitmap(result);
                }
            });
            regionView.setContentDescription(currentRegion.regionName);
            regionView.setVisibility(View.VISIBLE);
        } else {
            regionView.setVisibility(View.GONE);
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
            runningTasks++;
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
                        setCurrentAdvisory(advisory);
                    }
                    runningTasks--;
                    if(runningTasks == 0)
                        loadingIcon.setVisibility(View.GONE);
                    
                    // TODO: Notify user
                    // Alerter.notifyUser(getApplicationContext(), advisory);
                }
            }.execute();
        }
    }
    
    /**
     * Loads an advisory into the current view
     */
    private void setCurrentAdvisory(Advisory advisory) {
        if(currentAdvisory != null && !currentAdvisory.equals(advisory)) {
            // Detach old advisory
            currentAdvisory.onDetach();
        }
        if(advisory == null) {
            // Clear the advisory
            currentAdvisory = null;
            advisoryPanel.setVisibility(View.GONE);
            advisoryLink.setVisibility(View.GONE);
        } else if(!advisory.equals(currentAdvisory)) {
            // Attach new advisory
            currentAdvisory = advisory;
            currentAdvisory.onAttach(this, mainPanel);
            advisoryPanel.setVisibility(View.VISIBLE);
        } else {
            Log.v("Main", "Loaded same advisory into view");
//            currentAdvisory = advisory;
//            currentAdvisory.updateView(mainPanel);
        }
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
        switch(item.getItemId()) {
            case R.id.menu_region:
                // Show region dialog
                showRegionDialog();
                return true;
//            case R.id.menu_refresh:
//                // Refresh Advisory & Region Data
//                fetchAdvisory();
//                fetchRegionData();
//                return true;
//            case R.id.menu_settings:
//                // Open settings
//                startActivity(new Intent(this, SettingsActivity.class));
//                return true;
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
            regionDialog = null;
        }
        if(currentAdvisory != null) {
            currentAdvisory.onDetach();
            currentAdvisory = null;
        }
        // TODO: Stop AsyncTasks?
        // GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

}
