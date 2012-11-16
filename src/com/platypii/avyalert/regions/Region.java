package com.platypii.avyalert.regions;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.os.AsyncTask;
import android.util.Log;
import com.platypii.avyalert.Advisory;
import com.platypii.avyalert.AvalancheRisk;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.Callback;


public class Region {

    public String regionName;
    public String centerName;
    public String bannerUrl;
    public String advisoryUrl;
    public String dateSelector;
    public String ratingSelector;
    public String detailsSelector;
    public double latitude;
    public double longitude;
    
    
    /**
     * Returns the most recent Advisory for this region
     * @throws IOException If the download fails
     */
    private Advisory getAdvisory() throws IOException {
        Log.i("Region", "Connecting to " + advisoryUrl);

        Document doc = Jsoup.connect(advisoryUrl).get();
        
        // TODO: Parse date
        String date = null;
        if(dateSelector != null && !dateSelector.equals("")) {
            date = doc.select(dateSelector).text();
        }
        
        // Parse rating
        Rating rating = Rating.NONE;
        if(ratingSelector != null && !ratingSelector.equals("")) {
            String ratingText = doc.select(ratingSelector).text();
            rating = AvalancheRisk.parseRating(ratingText);
        }
        
        // Parse details
        String details = null;
        if(ratingSelector != null && !ratingSelector.equals("")) {
            details = doc.select(detailsSelector).html();
        }

        // TODO: Download avalanche rose
        
        return new Advisory(regionName, date, rating, details);
    }
    
    public void fetchAdvisory(final Callback<Advisory> callback) {
        new AsyncTask<Void, Void, Advisory>() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Advisory doInBackground(Void... params) {
                try {
                    return getAdvisory();
                } catch(IOException e) {
                    Log.w(regionName, "Failed to download advisory");
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Advisory advisory) {
                callback.callback(advisory);
            }
        }.execute();

    }
    
    /**
     * Returns the resource id of the banner for this region 
     */
    public int getBannerImage() {
        // TODO: Use bannerUrl
        if(regionName.equals("Eastern Sierra"))
            return R.drawable.easternsierra;
        if(regionName.equals("Lake Tahoe"))
            return R.drawable.tahoe;
        if(regionName.equals("Mount Shasta"))
            return R.drawable.shasta;
        if(regionName.equals("Los Angeles"))
            return R.drawable.la;
        else
            return 0;
    }
    
}
