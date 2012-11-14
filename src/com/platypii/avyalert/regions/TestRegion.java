package com.platypii.avyalert.regions;

import android.location.Location;
import com.platypii.avyalert.Advisory;
import com.platypii.avyalert.AvalancheRisk.Rating;


/**
 * Represents an ESAC Advisory. Pulls from http.
 * @author platypii
 */
public class TestRegion implements Region {
    private static final String region = "Test Region";
    
    private Rating rating = Rating.CONSIDERABLE;
    private String details = "Details...";
    

    @Override
    public Advisory getAdvisory() {
        return new Advisory(rating, details, this);
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
        return null;
    }

}
