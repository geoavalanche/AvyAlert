package com.platypii.avyalert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


/**
 * Class for fetching images from the web
 * TODO: Cache the images
 * @author platypii
 */
public class Images {
    
    public static final int FG_COLOR = 0xffdddddd;
    public static final int BG_COLOR = 0x00000000;
    
    // private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up

    // Image cache directory
    private static File cacheDir = null;


    /**
     * Initialize the image cache
     * @param cacheDir should be the result of Context.getCacheDir()
     */
    public static void initCache(File cacheDir) {
        Images.cacheDir = cacheDir;
    }

    /**
     * Downloads a bitmap. Synchronous (will block).
     */
    public static Bitmap fetchBitmap(String url) {
        Bitmap bmp = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch(IOException e) {
            Log.i("Main", "Error getting bitmap", e);
        }
        return bmp;
    }
    
    /**
     * Retrieves an image from the cache if available, otherwise fetches it and stores it in the cache
     */
    public static Bitmap fetchCachedBitmap(String url) {
        if(cacheDir == null) {
            // No cache
            return fetchBitmap(url);
        }
        File file = new File(cacheDir, filenameOf(url));
        try {
            if(!file.exists()) {
                // Download to cache
                URLConnection conn = new URL(url).openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
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
                Log.v("Images", "Cache hit: " + url);
            }
            InputStream is = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            is.close();
            return bmp;
        } catch(IOException e) {
            Log.i("Main", "Error getting bitmap", e);
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
    public static void fetchBitmapAsync(final String url, final Callback<Bitmap> callback) {
        if(url != null && !url.equals("")) {
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
        } else {
            callback.callback(null);
        }
    }
    
    /**
     * Just like fetchCachedBitmap, but with a callback instead of blocking
     */
    public static void fetchCachedBitmapAsync(final String url, final Callback<Bitmap> callback) {
        // TODO: Skip AsyncTask if there is a cache hit?
        if(url != null && !url.equals("")) {
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
        } else {
            callback.callback(null);
        }
    }
}
