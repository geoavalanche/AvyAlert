package com.platypii.avyalert;

import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.regions.Region;


/**
 * Represents an Avalanche Advisory
 * @author platypii
 */
public class Advisory {

    public final String date;
    public final Rating rating;
    public final String details;
    public final Region region;
    
    public boolean notified = false; // Has the user been notified of this advisory?
    
    
    public Advisory(String date, Rating rating, String details, Region region) {
        this.date = date;
        this.rating = rating;
        this.details = details;
        this.region = region;
    }
    
}
