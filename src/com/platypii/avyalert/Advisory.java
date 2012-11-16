package com.platypii.avyalert;

import com.platypii.avyalert.AvalancheRisk.Rating;


/**
 * Represents an Avalanche Advisory
 * @author platypii
 */
public class Advisory {

    public final String date;
    public final Rating rating;
    public final String details;
    public final String regionName;
    
    public boolean notified = false; // Has the user been notified of this advisory?
    
    
    public Advisory(String regionName, String date, Rating rating, String details) {
        this.regionName = regionName;
        this.date = date;
        this.rating = rating;
        this.details = details;
    }
    
}
