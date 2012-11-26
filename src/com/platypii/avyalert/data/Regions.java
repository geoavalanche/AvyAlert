package com.platypii.avyalert.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.platypii.avyalert.Callback;
import com.platypii.avyalert.Debug;
import com.platypii.avyalert.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


public class Regions {
    
    // URL to get updates to region definitions
    private static final String regionDataUrl = Debug.DEBUG? 
            "http://platypiiindustries.com/avalanche/regions-dev"
          : "http://platypiiindustries.com/avalanche/regions";

    // JSON region data string
    private static String regionData;

    // Regions
    public static List<Region> regions = null;
    
    // Regions grouped by state
    public static Map<String,List<Region>> subregions = null;

    
    /**
     * Loads region data from preferences or from package
     */
    public static void initRegionData(Context context) {
        if(regionDataUrl.matches("\\-dev$")) Log.e("Regions", "WARNING: Using -dev regions. Not for production!!");
        
        if(regions == null) {
            regions = new ArrayList<Region>();
            subregions = new HashMap<String,List<Region>>();

            // Load region data from preferences
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            regionData = prefs.getString("regionData", null);
            
            if(regionData == null) {
                // Load default region data from package
                regionData = readRawTextResource(context, R.raw.regions);
            }
            
            // Parse region data
            parseRegionData(regionData);
        }
    }

    /** Reads a text file from /res/raw/whatever into a String */
    private static String readRawTextResource(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        try {
            return IOUtils.toString(is);
        } catch(IOException e) {
            Log.w("Regions", "Failed to read default region data");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse region data from a JSON string
     * @param regionData a JSON string representing the region data
     */
    private static void parseRegionData(String regionData) {
        if(regionData != null) {
            try {
                regions = new Gson().fromJson(regionData, new TypeToken<List<Region>>(){}.getType());
            } catch(JsonSyntaxException e) {
                Log.w("Regions", "Failed to parse region data");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Fetches new region data from the internet
     * @param callback callback to notify of new region data
     */
    public static void fetchRegionData(final SharedPreferences prefs, final Callback<Boolean> callback) {
        Log.v("Regions", "Fetching region data: " + regionDataUrl);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected String doInBackground(Void... params) {
                BufferedReader in = null;
                try {
                    // http request
                    final HttpClient client = new DefaultHttpClient();
                    final HttpGet request = new HttpGet();
                    request.setURI(new URI(regionDataUrl));
                    final HttpResponse response = client.execute(request);
                    
                    // Read body
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    final StringBuffer data = new StringBuffer("");
                    String line;
                    while((line = in.readLine()) != null) {
                        data.append(line);
                        data.append('\n');
                    }
                    return data.toString();
                } catch(URISyntaxException e) {
                    Log.w("Regions", "Error parsing region data url");
                } catch(IOException e) {
                    Log.w("Regions", "Failed to download new region data");
                } finally {
                    if(in != null) {
                        try {
                            in.close();
                        } catch(IOException e) {}
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(String regionData) {
                if(regionData == null || regionData.equals("")) {
                    Log.v("Regions", "Empty region data");
                } else if(regionData.equals(Regions.regionData)) {
                    Log.v("Regions", "Region data already up to date");
                } else {
                    Log.i("Regions", "Received new region data, updating.");
                    // New region data
                    Regions.regionData = regionData;
                    parseRegionData(regionData);
                    // Store to preferences
                    final SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putString("regionData", regionData);
                    prefsEditor.commit();
                    callback.callback(true);
                }
                callback.callback(false); // Region data unchanged
            }
        }.execute();
    }
    
    /**
     * Returns a list of region names
     */
    public static CharSequence[] getRegionNames() {
        CharSequence[] names = new CharSequence[regions.size()];
        for(int i = 0; i < regions.size(); i++)
            names[i] = regions.get(i).regionName;
        return names;
    }

    /**
     * Returns the region with the given name
     */
    public static Region getRegion(CharSequence name) {
        for(Region region : regions)
            if(region.regionName.equals(name))
                return region;
        return null;
    }
    
    /**
     * Returns the index of the given regionName. Useful for ui lists.
     */
    public static int indexOf(String regionName) {
        for(int i = 0; i < regions.size(); i++)
            if(regions.get(i).regionName.equals(regionName))
                return i;
        return -1;
    }
    
}
