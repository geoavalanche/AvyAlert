package com.platypii.avyalert;

import java.io.IOException;
import com.platypii.avyalert.R;
import com.platypii.avyalert.regions.Region;
import com.platypii.avyalert.regions.Regions;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main application activity
 * @author platypii
 */
public class MainActivity extends Activity {

    private static Region currentRegion; // The current region
    private static Advisory currentAdvisory; // The advisory currently being displayed
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Load the latest advisory
        updateAdvisory();

    }

    
    private void updateAdvisory() {
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
                updateViews(advisory);
                loading.setVisibility(View.GONE);
                
                // Notify user
                Alerter.notifyUser(MainActivity.this, advisory);
            }
        }.execute();
        
    }
    
    /**
     * Updates the text views with the given advisory
     */
    @SuppressLint("NewApi")
    private void updateViews(Advisory advisory) {
        if(currentRegion != null) {
            if(android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
                ActionBar actionBar = getActionBar();
                actionBar.setSubtitle(currentRegion.getName());
            }
        }
        
        TextView ratingView = (TextView) findViewById(R.id.ratingView);
        ratingView.setText(advisory.rating.toString());
        ratingView.setTextColor(AvalancheRisk.getForegroundColor(advisory.rating));
        ratingView.setBackgroundColor(AvalancheRisk.getBackgroundColor(advisory.rating));

        TextView detailsView = (TextView) findViewById(R.id.detailsView);
        detailsView.setText(advisory.details);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_advisory:
                // Open url
                if(currentRegion != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRegion.getAdvisoryUrl()));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "Cannot determine your location. Please select a region in settings.", Toast.LENGTH_SHORT);
                }
            case R.id.menu_settings:
                // Open settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
