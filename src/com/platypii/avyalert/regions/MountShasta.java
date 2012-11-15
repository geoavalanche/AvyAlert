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

    /**
     * Load the latest ESAC Advisory
     */
    @Override
    public Advisory getAdvisory() throws IOException {
        Log.i("ESAC", "Connecting to " + getAdvisoryUrl());

        Document doc = Jsoup.connect(getAdvisoryUrl()).get();
        Elements divAdvisory = doc.select("table.contentpaneopen");
        
        // TODO: Parse date
        String date = "";
        
        // Parse rating
        Element divRating = divAdvisory.select(".rating div").first();
        Rating rating = divRating == null? Rating.NONE : AvalancheRisk.parseRating(divRating.text()); // TODO: More robust selector
        
        // Parse details
        String details = divAdvisory.html();

        // TODO: Download avalanche rose
        
        return new Advisory(date, rating, details, this);
    }

    @Override
    public String getName() {
        return "Mount Shasta";
    }
    @Override
    public String getCenterName() {
        return "Mount Shasta Avalanche Center";
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
        return "http://shastaavalanche.org/advisories/advisories/avalanche-advisory";
    }

    @Override
    public int getBanner() {
        return R.drawable.shasta;
    }

}
