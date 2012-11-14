package com.platypii.avyalert.regions;

import java.io.IOException;
import android.location.Location;
import com.platypii.avyalert.Advisory;


public interface Region {

    /**
     * Returns the region name (eg- "Eastern Sierra")
     */
    public String getName();

    /**
     * Returns the region location
     */
    public Location getLocation();

    /**
     * Returns the URL to read the full advisory
     */
    public String getAdvisoryUrl();

    /**
     * Returns the most recent Advisory for this region
     * @throws IOException If the download fails
     */
    public Advisory getAdvisory() throws IOException;
    
}
