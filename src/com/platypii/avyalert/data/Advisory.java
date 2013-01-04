package com.platypii.avyalert.data;

import java.net.URL;
import java.util.List;
import com.platypii.avyalert.Util;
import com.platypii.avyalert.data.AvalancheRisk.Rating;


/**
 * Represents an Avalanche Advisory
 * @author platypii
 */
public class Advisory {

    public final Region region;
    public final String date;
    public final Rating rating;
    public final URL roseUrl;
    public final List<URL> imageUrls;
    public final String details;

    // public boolean notified = false; // Has the user been notified of this advisory?

    
    public Advisory(Region region, String date, Rating rating, URL roseUrl, List<URL> imageUrls, String details) {
        this.region = region;
        this.date = date;
        this.rating = rating;
        this.roseUrl = roseUrl;
        this.imageUrls = imageUrls;
        this.details = details;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Advisory && obj != null) {
            Advisory other = (Advisory) obj;
            if(!region.equals(other.region)) return false;
            if(!Util.eq(date, other.date)) return false;
            if(rating != other.rating) return false;
            if(!Util.eq(roseUrl, other.roseUrl)) return false;
            if(!Util.eq(imageUrls, other.imageUrls)) return false;
            if(!Util.eq(details, other.details)) return false;
            return true;
        }
        return false;
    }

}
