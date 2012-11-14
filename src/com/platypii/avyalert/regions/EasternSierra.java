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
 * Represents an ESAC Advisory. Pulls from http.
 * @author platypii
 */
public class EasternSierra implements Region {
    private static final String region = "Eastern Sierra";
    
    private static final String url = "http://esavalanche.org/advisory";
    // private static final String url = "http://platypiiindustries.com/esac/advisory";
    
    
    /**
     * Load the latest ESAC Advisory
     */
    @Override
    public Advisory getAdvisory() throws IOException {
        Log.i("ESAC", "Connecting to " + url);

        Document doc = Jsoup.connect(url).get();
        Elements divAdvisory = doc.select("div.forecast-advisory");

        // Parse date
        Element dateElement = divAdvisory.select("strong").first();
        String date = dateElement == null? "" : dateElement.text();
        
        // Parse rating
        Element ratingElement = divAdvisory.select(".rating div").first();
        Rating rating = ratingElement == null? Rating.NONE : AvalancheRisk.parseRating(ratingElement.text()); // TODO: More robust selector
        
        // Parse details
        String details = divAdvisory.html();

        // TODO: Download avalanche rose
        
        return new Advisory(date, rating, details, this);
    }

    @Override
    public String getName() {
        return region;
    }

    @Override
    public Location getLocation() {
        // Mammoth
        final Location loc = new Location("");
        loc.setLatitude(37.630628);
        loc.setLongitude(-119.032625);
        return loc;
    }

    @Override
    public String getAdvisoryUrl() {
        return url;
    }

}
