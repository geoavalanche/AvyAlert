package com.platypii.avyalert;

import java.io.IOException;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Main application activity
 * @author platypii
 */
public class MainActivity extends Activity {
    
    Advisory latestAdvisory;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final Context appContext = getApplicationContext();
        final ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
        
        new AsyncTask<Void, Void, Advisory>() {
            @Override
            protected void onPreExecute() {
                loading.setVisibility(View.VISIBLE);
            }
            @Override
            protected Advisory doInBackground(Void... params) {
                while(true) {
                    try {
                        return new Advisory(appContext);
                    } catch(IOException e) {
                        Log.w("MainActivity", "Failed to download advisory");
                    }
                    try {
                        Thread.sleep(8000);
                    } catch(InterruptedException e) {}
                }
            }
            @Override
            protected void onPostExecute(Advisory advisory) {
                
                advisory.rating = Rating.CONSIDERABLE; // TODO
                
                MainActivity.this.latestAdvisory = advisory;
                updateText(advisory);
                loading.setVisibility(View.GONE);
                
                // Notification
                Alerter.notifyUser(MainActivity.this, advisory);
            }
        }.execute();
        
    }
    
    /**
     * Updates the text views with the given advisory
     */
    private void updateText(Advisory advisory) {
        TextView ratingView = (TextView) findViewById(R.id.ratingView);
        TextView detailsView = (TextView) findViewById(R.id.detailsView);
        ratingView.setText(advisory.rating.toString());
        ratingView.setTextColor(0xff000000 + AvalancheRisk.getForegroundColor(advisory.rating));
        ratingView.setBackgroundColor(AvalancheRisk.getBackgroundColor(advisory.rating));
        detailsView.setText(advisory.getDetails());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
