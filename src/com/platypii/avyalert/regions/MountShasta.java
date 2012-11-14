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
import com.platypii.avyalert.R;


/**
 * Represents an Avalanche Advisory. Pulls from http.
 * @author platypii
 */
public class MountShasta implements Region {
    private static final String region = "Mount Shasta";
    private static final String url = "http://shastaavalanche.org/advisories/advisories/avalanche-advisory";
    
    
    /**
     * Load the latest ESAC Advisory
     */
    @Override
    public Advisory getAdvisory() throws IOException {
        Log.i("ESAC", "Connecting to " + url);

        Document doc = Jsoup.connect(url).get();
        Elements divAdvisory = doc.select("table.contentpaneopen");
        
        // TODO: Parse date
        String date = "";
        
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
        // Mt Shasta
        Location loc = new Location("");
        loc.setLatitude(41.409196);
        loc.setLongitude(-122.194888);
        return loc;
    }

    @Override
    public String getAdvisoryUrl() {
        return url;
    }

    @Override
    public int getBanner() {
        return R.drawable.shasta;
    }

}
