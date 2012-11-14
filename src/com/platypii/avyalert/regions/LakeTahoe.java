package com.platypii.avyalert.regions;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.location.Location;
import android.util.Log;
import com.platypii.avyalert.Advisory;
import com.platypii.avyalert.AvalancheRisk;
import com.platypii.avyalert.AvalancheRisk.Rating;


/**
 * Represents a Sierra Avalanche Advisory. Pulls from http.
 * @author platypii
 */
public class LakeTahoe implements Region {
    private static final String region = "Lake Tahoe";
    private static final String url = "http://www.sierraavalanchecenter.org/advisory";
    
    
    /**
     * Load the latest ESAC Advisory
     */
    @Override
    public Advisory getAdvisory() throws IOException {
        Log.i("ESAC", "Connecting to " + url);

        Document doc = Jsoup.connect(url).get();
        Elements divAdvisory = doc.select("div.content table");
        
        // Parse date
        Element dateElement = divAdvisory.select("strong").first();
        String date = dateElement == null? "" : dateElement.text();

        // Parse rating
        Element divRating = divAdvisory.select(".rating div").first();
        Rating rating = divRating == null? Rating.NONE : AvalancheRisk.parseRating(divRating.text()); // TODO: More robust selector
        
        // Parse details
        String details = divAdvisory.text();
        return new Advisory(date, rating, details, this);

        // TODO: Download avalanche rose
        
    }

    @Override
    public String getName() {
        return region;
    }

    @Override
    public Location getLocation() {
        // Lake Tahoe
        Location loc = new Location("");
        loc.setLatitude(39.091667);
        loc.setLongitude(-120.041667);
        return loc;
    }

    @Override
    public String getAdvisoryUrl() {
        return url;
    }

}
