package com.platypii.avyalert.regions;

import java.util.Date;
import android.location.Location;
import com.platypii.avyalert.Advisory;
import com.platypii.avyalert.R;
import com.platypii.avyalert.AvalancheRisk.Rating;


/**
 * Represents an Avalanche Region. Pulls from http.
 * @author platypii
 */
public class LosAngeles implements Region {
    private static final String region = "Los Angeles";
    
    private Rating rating = Rating.LOW;
    private String details = "Details...";
    

    @Override
    public Advisory getAdvisory() {
        return new Advisory(new Date().toString(), rating, details, this);
    }

    @Override
    public String getName() {
        return region;
    }

    @Override
    public Location getLocation() {
        // LA
        final Location loc = new Location("");
        loc.setLatitude(34.05);
        loc.setLongitude(-118.25);
        return loc;
    }

    @Override
    public String getAdvisoryUrl() {
        return "http://www.google.com/";
    }

    @Override
    public int getBanner() {
        return R.drawable.la;
    }

}