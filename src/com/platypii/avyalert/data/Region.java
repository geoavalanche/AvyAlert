package com.platypii.avyalert.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.util.Log;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;


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
        Document doc = Jsoup.connect(advisoryUrl).get();
        
        Log.v(regionName, "Received advisory");
        
        // TODO: Parse date
        String date = null;
        if(dateSelector != null && !dateSelector.equals("")) {
            date = doc.select(dateSelector).text();
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
            details = details.replaceAll("(?si)</?(img|a).*?>", ""); // Remove images and links
        }

        return new Advisory(this, date, rating, roseUrl, imageUrls, details);
    }

    private Rating parseRating(Document doc, String selector) {
        if(selector != null && !selector.equals("")) {
            String html = doc.select(selector).html();
            if(html.matches("(?si).*Extreme.*")) return Rating.EXTREME;
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
    
    private String parseImageUrl(Document doc, String selector) {
        String imageUrl = null;
        if(selector != null && !selector.equals("")) {
            String html = doc.select(roseSelector).html();
            if(html.matches("(?si).*<img\\s.*?src=\".*?\".*?>.*")) {
                // text contains at least one IMG tag with a SRC attribute in quotes
                int start = html.toLowerCase(Locale.getDefault()).indexOf("src=\"") + 5;
                int end = html.indexOf('"', start);
                imageUrl = html.substring(start, end);
                imageUrl = imageUrl.replaceAll("[ \t\r\n]*", ""); // Remove whitespace
                Log.v(regionName, "image url: \""+imageUrl+"\"");
                return imageUrl;
            } else {
                Log.v(regionName, "Failed to parse url: \""+html+"\"");
            }
        }
        return null;
    }
    
    /**
     * Returns the resource id of the banner for this region 
     */
    public int getBannerImage() {
        // TODO: Use bannerUrl
        if(regionName.equals("Eastern Sierra, CA")) return R.drawable.easternsierra;
        if(regionName.equals("Lake Tahoe, CA")) return R.drawable.tahoe;
        if(regionName.equals("Mount Shasta, CA")) return R.drawable.shasta;
        if(regionName.equals("Bozeman, MT")) return R.drawable.bozeman;
        if(regionName.equals("Los Angeles, CA")) return R.drawable.la;
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
