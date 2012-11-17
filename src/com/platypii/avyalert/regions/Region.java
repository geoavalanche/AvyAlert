package com.platypii.avyalert.regions;

import java.io.IOException;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.os.AsyncTask;
import android.util.Log;
import com.platypii.avyalert.Advisory;
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
    public String roseSelector;
    public String roseForegroundColor;
    public String roseBackgroundColor;
    public String detailsSelector;
    public double latitude;
    public double longitude;
    
    
    public void fetchAdvisory(final Callback<Advisory> callback) {
        Log.v(regionName, "Fetching advisory: " + advisoryUrl);
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
     * Returns the most recent Advisory for this region
     * @throws IOException If the download fails
     */
    private Advisory getAdvisory() throws IOException {
        // Download advisory
        Document doc = Jsoup.connect(advisoryUrl).get();
        
        Log.v(regionName, "Received advisory");
        
        // TODO: Parse date
        String date = null;
        if(dateSelector != null && !dateSelector.equals("")) {
            date = doc.select(dateSelector).text();
            Log.v(regionName, "Date text: \""+date+"\"");
        }
        
        // Parse rating
        Rating rating = Rating.NONE;
        if(ratingSelector != null && !ratingSelector.equals("")) {
            String ratingText = doc.select(ratingSelector).html();
            rating = parseRating(ratingText);
            if(rating == Rating.NONE)
                Log.v(regionName, "Failed to parse rating: \""+ratingText+"\"");
            else
                Log.v(regionName, "Rating: "+rating);
        }
        
        // Parse rose
        String roseUrl = null;
        if(roseSelector != null && !roseSelector.equals("")) {
            String roseText = doc.select(roseSelector).html();
            if(roseText.matches("(?si).*<img\\s.*?src=\".*?\".*?>.*")) {
                // text contains at least one IMG tag with a SRC attribute in quotes
                int start = roseText.toLowerCase(Locale.getDefault()).indexOf("src=\"") + 5;
                int end = roseText.indexOf('"', start);
                roseUrl = roseText.substring(start, end);
                roseUrl = roseUrl.replaceAll("[ \t\r\n]*", ""); // Remove whitespace
                Log.v(regionName, "Rose url: \""+roseUrl+"\"");
            } else {
                Log.v(regionName, "Failed to parse rose url: \""+roseText+"\"");
            }
        }
        
        // Parse details
        String details = null;
        if(detailsSelector != null && !detailsSelector.equals("")) {
            details = doc.select(detailsSelector).html();
        }

        return new Advisory(this, date, rating, roseUrl, details);
    }

    private static Rating parseRating(String str) {
        if(str.matches("(?si).*Extreme.*")) return Rating.EXTREME;
        else if(str.matches("(?si).*High.*")) return Rating.HIGH;
        else if(str.matches("(?si).*Considerable.*")) return Rating.CONSIDERABLE;
        else if(str.matches("(?si).*Moderate.*")) return Rating.MODERATE;
        else if(str.matches("(?si).*Low.*")) return Rating.LOW;
        else {
            Log.d("Region", "Unable to parse rating from: \"" + str + "\"");
            return Rating.NONE;
        }
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
    
    @Override
    public boolean equals(Object obj) {
        // Two regions with the same name are the same
        return (obj instanceof Region) &&
                (obj != null) &&
                (regionName.equals(((Region)obj).regionName));
    }
    
}
