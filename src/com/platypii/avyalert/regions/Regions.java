package com.platypii.avyalert.regions;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Regions {

    public static List<Region> regions = new ArrayList<Region>();
    static {
        regions.add(new EasternSierra());
        regions.add(new LakeTahoe());
        regions.add(new MountShasta());
        regions.add(new LosAngeles());
    }
    

    public static Region getCurrentRegion(Context context) {
        
        // Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String regionName = prefs.getString("region", null);
     
        // Auto location
        if(regionName.equals("Auto")) {
//            autoRegion(context);
        } else {
            // Return selected region
            for(Region region : regions)
                if(region.getName().equals(regionName))
                    return region;
        }
        return null;

    }
    
//    private static Region autoRegion(Context context) {
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//        // Find Last Known Location
//        private static Location lastLocation = null;
//        Location lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if(lastGPSLocation == null && lastNetworkLocation == null)
//            lastLocation = null;
//        else if(lastGPSLocation == null)
//            lastLocation = lastNetworkLocation;
//        else
//            lastLocation = lastGPSLocation;
//
//        // Request fresh location
//        requestLocation();
//        
//        // Find closest region
//        return findClosestRegion(lastLocation);
//
//    }

    /**
     * Requests a location update
     */
//    private static void requestLocation() {
//      locationManager.requestSingleUpdate("GPS", new LocationListener() {
//      @Override
//      public void onLocationChanged(Location location) {
//          // TODO: Handle location update
//      }
//      @Override
//      public void onProviderDisabled(String provider) {}
//      @Override
//      public void onProviderEnabled(String provider) {}
//      @Override
//      public void onStatusChanged(String provider, int status, Bundle extras) {}
//      
//  }, null);
//    }

    /**
     * Finds the current location by GPS, and then determines the closest region in the database
     */
//    private static Region findClosestRegion(Location currentLocation) {
//        if(currentLocation == null) return null;
//        
//        Region bestRegion = null;
//        float bestDistance = Float.POSITIVE_INFINITY;
//        for(Region region : regions) {
//            float distance = region.getLocation().distanceTo(currentLocation);
//            if(distance < bestDistance) {
//                bestRegion = region;
//                bestDistance = distance;
//            }
//        }
//        return bestRegion;
//    }

    public static CharSequence[] getRegionNames() {
        CharSequence[] names = new CharSequence[regions.size()];
        for(int i = 0; i < regions.size(); i++)
            names[i] = regions.get(i).getName();
        return names;
    }

    /**
     * Returns the region with the given name
     */
    public static Region getRegion(CharSequence name) {
        for(Region region : regions)
            if(region.getName().equals(name))
                return region;
        return null;
    }
    
    public static int indexOf(String regionName) {
        for(int i = 0; i < regions.size(); i++)
            if(regions.get(i).getName().equals(regionName))
                return i;
        return -1;
    }
    
}
