package com.platypii.avyalert.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


/**
 * Class for fetching images from the web
 * @author platypii
 */
public class Images {
    
    public static final int FG_COLOR = 0xffdddddd;
    public static final int BG_COLOR = 0x00000000;
    
    // private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up

    // Image cache directory
    private static File cacheDir = null;
    
    // Memory cache
    private static final int MAX_MEM_CACHE = 30;
    private static LRUCache<URL,Bitmap> memCache = new LRUCache<URL,Bitmap>(MAX_MEM_CACHE);


    /**
     * Initialize the disk cache
     * @param cacheDir should be the result of getApplicationContext().getCacheDir()
     */
    public static void initCache(File cacheDir) {
        Images.cacheDir = cacheDir;
    }

    /** Downloads a bitmap. Synchronous (will block). */
    public static Bitmap fetchBitmap(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(true); // Enable device caching
            InputStream is = connection.getInputStream();
            // InputStream is = url.openStream();
            if(is != null) {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                return bmp;
            }
        } catch(IOException e) {
            Log.i("Main", "Error getting bitmap", e);
        }
        return null;
    }
    
    /** Retrieves an image from the cache if available, otherwise fetches it and stores it in the cache */
    public static Bitmap fetchCachedBitmap(URL url) {
        if(memCache.containsKey(url)) {
            // Mem Cache
            Log.v("Images", "Mem cache hit: " + url);
            return memCache.get(url);
        }
        if(cacheDir == null) {
            // No disk cache, fetch bitmap
            Log.v("Images", "No disk cache, mem cache miss: " + url);
            Bitmap bmp = fetchBitmap(url);
            memCache.put(url, bmp);
            return bmp;
        }
        File file = new File(cacheDir, filenameOf(url.toString()));
        try {
            if(!file.exists()) {
                // Download to cache
                URLConnection connection = url.openConnection();
                connection.setUseCaches(true); // Enable device caching
                InputStream is = connection.getInputStream();
                // InputStream is = url.openStream(); // One step
                OutputStream os = new FileOutputStream(file);
                // Copy input stream to file
                byte[] buffer = new byte[1024];
                int len;
                while((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.close();
                Log.v("Images", "Cache miss: " + url);
            } else {
                Log.v("Images", "Disk cache hit: " + url);
            }
            InputStream is = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            memCache.put(url, bmp);
            return bmp;
        } catch(IOException e) {
            Log.i("Main", "Error getting bitmap", e);
            return null;
        }
    }
    
    /** Gets an InputStream for a given url */
    public static InputStream getInputStream(URL imageUrl) {
        try {
            if(imageUrl.toString().matches("^https:\\/\\/.*")) {
                // allow any/all https certificates
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch(GeneralSecurityException e) {
                    Log.i("Main", "Error setting TrustManager", e);
                }
                // Connect to https
                return imageUrl.openStream();
            } else {
                return imageUrl.openStream();
            }
        } catch(IOException e) {
            Log.i("Main", "Error getting InputStream for URL", e);
            return null;
        }
    }
    
    /**
     * Removes nasty characters from urls and replaces them with dashes for safety
     */
    private static String filenameOf(String url) {
        return url.replaceAll("[ :\\\\/=\\&@\\?\\.]+", "-");
    }

    /**
     * Returns a new bitmap in which foreground is replaced by FG_COLOR, and background is replaced by BG_COLOR
     * @param in
     * @return
     */
    public static Bitmap replaceColor(Bitmap in, int fg, int bg) {
        if(in == null) return null;
        Bitmap out = Bitmap.createBitmap(in.getWidth(), in.getHeight(), Bitmap.Config.ARGB_8888);
        for(int x = 0; x < in.getWidth(); x++) {
            for(int y = 0; y < in.getHeight(); y++) {
                int color = in.getPixel(x, y);
                if((color & 0xffffff) == (fg & 0xffffff)) {// compare RGB
                    int alpha = color & 0xff000000; // pixel alpha
                    int fg_rgb = FG_COLOR & 0x00ffffff; // Foreground RGB 
                    out.setPixel(x, y, alpha + fg_rgb);
                } else if((color & 0xffffff) == (bg & 0xffffff)) {
                    out.setPixel(x, y, BG_COLOR);
                } else {
                    out.setPixel(x, y, color);
                }
            }
        }
        return out;
    }
 
    /**
     * Just like fetchBitmap, but with a callback instead of blocking
     */
    public static void fetchBitmapAsync(final String urlStr, final Callback<Bitmap> callback) {
        if(urlStr != null && !urlStr.equals("")) {
            try {
                final URL url = new URL(urlStr);
                new AsyncTask<Void,Void,Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        return Images.fetchBitmap(url);
                    }
                    @Override
                    protected void onPostExecute(Bitmap result) {
                        callback.callback(result);
                    }
                }.execute();
            } catch(MalformedURLException e) {
                callback.callback(null);
            }
        } else {
            callback.callback(null);
        }
    }
    
    /**
     * Just like fetchCachedBitmap, but with a callback instead of blocking
     */
    public static void fetchCachedBitmapAsync(String urlStr, final Callback<Bitmap> callback) {
        if(urlStr != null && !urlStr.equals("")) {
            if(memCache.containsKey(urlStr)) {
                // Skip AsyncTask if there is a cache hit
                callback.callback(memCache.get(urlStr));
            } else {
                try {
                    final URL url = new URL(urlStr);
                    new AsyncTask<Void,Void,Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            return Images.fetchCachedBitmap(url);
                        }
                        @Override
                        protected void onPostExecute(Bitmap result) {
                            callback.callback(result);
                        }
                    }.execute();
                } catch(MalformedURLException e) {
                    callback.callback(null);
                }
            }
        } else {
            callback.callback(null);
        }
    }
    
    /** Accept any and all certificates */
    static TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            // No need to implement.
        }
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            // No need to implement.
        }
    }};
    
}
