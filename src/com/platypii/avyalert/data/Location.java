package com.platypii.avyalert.data;


public class Location {

//  private static Region autoRegion(Context context) {
//      LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//      // Find Last Known Location
//      private static Location lastLocation = null;
//      Location lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//      Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//      if(lastGPSLocation == null && lastNetworkLocation == null)
//          lastLocation = null;
//      else if(lastGPSLocation == null)
//          lastLocation = lastNetworkLocation;
//      else
//          lastLocation = lastGPSLocation;
//
//      // Request fresh location
//      requestLocation();
//      
//      // Find closest region
//      return findClosestRegion(lastLocation);
//
//  }

  /**
   * Requests a location update
   */
//  private static void requestLocation() {
//    locationManager.requestSingleUpdate("GPS", new LocationListener() {
//    @Override
//    public void onLocationChanged(Location location) {
//        // TODO: Handle location update
//    }
//    @Override
//    public void onProviderDisabled(String provider) {}
//    @Override
//    public void onProviderEnabled(String provider) {}
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {}
//    
//}, null);
//  }

  /**
   * Finds the current location by GPS, and then determines the closest region in the database
   */
//  private static Region findClosestRegion(Location currentLocation) {
//      if(currentLocation == null) return null;
//      
//      Region bestRegion = null;
//      float bestDistance = Float.POSITIVE_INFINITY;
//      for(Region region : regions) {
//          float distance = region.getLocation().distanceTo(currentLocation);
//          if(distance < bestDistance) {
//              bestRegion = region;
//              bestDistance = distance;
//          }
//      }
//      return bestRegion;
//  }

}
