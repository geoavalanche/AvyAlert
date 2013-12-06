package com.platypii.avyalert.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.platypii.avyalert.R;
import com.platypii.avyalert.data.AvalancheRisk.Rating;


public class Region {

    public String regionName;
    public String subregion;
    public String centerName;
    public String bannerUrl;
    public String advisoryUrl;
    public String advisoryLink;
    public String dateSelector;
    public String ratingSelector;
    public String roseSelector;
    public String roseForegroundColor;
    public String roseBackgroundColor;
    public List<String> imageSelectors;
    public String detailsSelector;
    public double latitude;
    public double longitude;
    
    
    /**
     * Returns the most recent Advisory for this region
     * @throws IOException If the download fails
     */
    public Advisory fetchAdvisory() throws IOException {
        Log.v(regionName, "Fetching advisory: " + advisoryUrl);

        // Download advisory
        final Document doc = Jsoup.connect(advisoryUrl).timeout(10000).get();
        
        Log.v(regionName, "Received advisory");
        
        // TODO: Parse date
        String date = null;
        if(dateSelector != null && !dateSelector.equals("")) {
            date = doc.select(dateSelector).text();
            date = date.replaceAll("[ \t\r\n]+", " "); // Remove redundant whitespace
            Log.v(regionName, "Date text: \""+date+"\"");
        }
        
        // Parse rating
        Rating rating = parseRating(doc, ratingSelector);
        
        // Parse rose
        String roseUrl = parseImageUrl(doc, roseSelector);
        
        // Parse extra images
        List<String> imageUrls = new ArrayList<String>();
        if(imageSelectors != null) {
            for(String imageSelector : imageSelectors) {
                final String imageUrl = parseImageUrl(doc, imageSelector);
                if(imageUrl != null)
                    imageUrls.add(imageUrl);
            }
        }
        
        // Parse details
        String details = null;
        if(detailsSelector != null && !detailsSelector.equals("")) {
            details = doc.select(detailsSelector).html();
            // Clean up some of the garbage. Edits related to style (a,img tags) should be removed later by the UI.
            details = details.replaceAll("(?si)<script.*?>.*?</script>", ""); // Remove scripts
            details = details.replaceAll("(?s)<!\\[CDATA\\[.*?\\]\\]>", ""); // Remove CDATA blocks
        }

        return new Advisory(this, date, rating, roseUrl, imageUrls, details);
    }

    private Rating parseRating(Document doc, String selector) {
        if(selector != null && !selector.equals("")) {
            final String html = doc.select(selector).toString();
            if(html.matches("(?s).*EXTREME.*")) return Rating.EXTREME;
            else if(html.matches("(?s).*HIGH.*")) return Rating.HIGH;
            else if(html.matches("(?s).*CONSIDERABLE.*")) return Rating.CONSIDERABLE;
            else if(html.matches("(?s).*MODERATE.*")) return Rating.MODERATE;
            else if(html.matches("(?s).*LOW.*")) return Rating.LOW;
            else if(html.matches("(?si).*Extreme.*")) return Rating.EXTREME;
            else if(html.matches("(?si).*High.*")) return Rating.HIGH;
            else if(html.matches("(?si).*Considerable.*")) return Rating.CONSIDERABLE;
            else if(html.matches("(?si).*Moderate.*")) return Rating.MODERATE;
            else if(html.matches("(?si).*Low.*")) return Rating.LOW;
            else {
                Log.d(regionName, "Unable to parse rating from: \"" + html + "\"");
            }
        }
        return Rating.NONE;
    }
    
    /** Searches the document for a given selector. From this, return the url to the first img tag's src */
    private String parseImageUrl(Document doc, String selector) {
        String url = null;
        try {
            if(selector != null && !selector.equals("")) {
                String html = doc.select(selector).toString();
                if(html.matches("(?si).*<img\\s.*?src=\".*?\".*?>.*")) {
                    // text contains at least one IMG tag with a SRC attribute in quotes
                    int start = html.toLowerCase(Locale.getDefault()).indexOf("src=\"") + 5;
                    int end = html.indexOf('"', start);
                    url = html.substring(start, end);
                    url = url.replaceAll("[ \t\r\n]*", ""); // Remove whitespace
                    Log.v(regionName, "image url: \""+url+"\"");
                    
                    // Handle relative paths
                    if(! url.matches("^https?://.*")) {
                        URL abs = new URL(new URL(advisoryUrl), url);
                        Log.v(regionName, "absolute image url: " + abs);
                        return abs.toString();
                    } else {
                        return url;
                    }
                } else {
                    Log.v(regionName, "Failed to parse url: \""+html+"\"");
                }
            }
        } catch(MalformedURLException e) {
            Log.i(regionName, "Malformed url", e);
        }
        return null;
    }
    
    /**
     * Loads the banner into the given view immediately if it can.
     * Will start an AsyncTask to download othewise, and will then callback
     * @param callback called if there is a Bitmap that needs to be loaded
     */
    public void fetchBannerImage(ImageView bannerView, final Callback<Bitmap> callback) {
        if(regionName.equals("Eastern Sierra, CA")) bannerView.setImageResource(R.drawable.banner_easternsierra);
        else if(regionName.equals("Lake Tahoe, CA")) bannerView.setImageResource(R.drawable.banner_tahoe);
        else if(regionName.equals("Mount Shasta, CA")) bannerView.setImageResource(R.drawable.banner_shasta);
        else if(regionName.equals("Bozeman, MT")) bannerView.setImageResource(R.drawable.banner_bozeman);
        else if(regionName.equals("Mount Rainier, WA")) bannerView.setImageResource(R.drawable.banner_rainier);
        else if(regionName.equals("Tetons, WY")) bannerView.setImageResource(R.drawable.banner_tetons);
        else if(regionName.equals("Mt Washington, NH")) bannerView.setImageResource(R.drawable.banner_tuckerman);
        else if(regionName.matches(".*, CO")) bannerView.setImageResource(R.drawable.banner_colorado);
        else {
            // Download from bannerUrl
            Images.fetchCachedBitmapAsync(getURL(bannerUrl), callback);
        }
    }
    
    /** Cleans up urls */
    private String getURL(String str) {
        try {
            URL url = new URL(str);
            // TODO: handle relative URLs
            return url.toString();
        } catch(MalformedURLException e) {
            Log.i(regionName, "Malformed url: " + str);
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Two regions with the same name are the same
        return (obj instanceof Region) &&
                (obj != null) &&
                (regionName.equals(((Region)obj).regionName));
    }
    
}
