package com.platypii.avyalert.regions;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;


public class Regions {

    public static List<Region> regions = new ArrayList<Region>();
    static {
        regions.add(new TestRegion());
        regions.add(new EasternSierra());
        regions.add(new Tahoe());
    }
    
    private static Location lastLocation = null;
    

    public static Region getCurrentRegion(Context context) {
        
        // Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String regionName = prefs.getString("region", "Auto");
     
        // Auto location
        if(regionName.equals("Auto")) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Find Last Known Location
            Location lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(lastGPSLocation == null && lastNetworkLocation == null)
                lastLocation = null;
            else if(lastGPSLocation == null)
                lastLocation = lastNetworkLocation;
            else
                lastLocation = lastGPSLocation;

            // Request fresh location
            requestLocation();
            
            // Find closest region
            return findClosestRegion(lastLocation);

        } else {
            // Return selected region
            for(Region region : regions)
                if(region.getName().equals(regionName))
                    return region;
        }
        return null;
    }

    /**
     * Requests a location update
     */
    private static void requestLocation() {
//      locationManager.requestSingleUpdate("GPS", new LocationListener() {
//      @Override
//      public void onLocationChanged(Location location) {
//          // TODO Auto-generated method stub
//          
//      }
//      @Override
//      public void onProviderDisabled(String provider) {}
//      @Override
//      public void onProviderEnabled(String provider) {}
//      @Override
//      public void onStatusChanged(String provider, int status, Bundle extras) {}
//      
//  }, null);
    }

    /**
     * Finds the current location by GPS, and then determines the closest region in the database
     */
    private static Region findClosestRegion(Location currentLocation) {
        if(currentLocation == null) return null;
        
        Region bestRegion = null;
        float bestDistance = Float.POSITIVE_INFINITY;
        for(Region region : regions) {
            float distance = region.getLocation().distanceTo(currentLocation);
            if(distance < bestDistance) {
                bestRegion = region;
                bestDistance = distance;
            }
        }
        return bestRegion;
    }

    public static CharSequence[] getRegionNames() {
        CharSequence[] names = new CharSequence[regions.size() + 1];
        names[0] = "Auto";
        for(int i = 0; i < regions.size(); i++)
            names[i + 1] = regions.get(i).getName();
        return names;
    }
    
}
