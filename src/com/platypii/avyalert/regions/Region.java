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
     * Returns the avalanche center responsible for advisories (eg- "Eastern Sierra Avalanche Center")
     */
    public String getCenterName();

    /**
     * Returns the region banner resource ID
     */
    public int getBanner();

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
