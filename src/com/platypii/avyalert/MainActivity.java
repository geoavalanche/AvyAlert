package com.platypii.avyalert;

import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.R;
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
                return new Advisory(appContext);
            }
            @Override
            protected void onPostExecute(Advisory advisory) {
                MainActivity.this.latestAdvisory = advisory;
                TextView levelView = (TextView) findViewById(R.id.levelView);
                TextView detailsView = (TextView) findViewById(R.id.detailsView);
                levelView.setText(advisory.toString());
                detailsView.setText(advisory.getDetails());
                loading.setVisibility(View.GONE);
                
                // Notification
                if(advisory.rating != Rating.NONE) {
                    Alerter.notifyUser(MainActivity.this, advisory);
                } else {
                    Log.i("MainActivity", "Avalanche rating unknown. Skipping notification.");
                }
            }
        }.execute();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
