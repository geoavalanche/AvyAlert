package com.platypii.avyalert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.regions.Region;


/**
 * Represents an Avalanche Advisory
 * @author platypii
 */
public class Advisory {

    public final Region region;
    public final String date;
    public final Rating rating;
    public final String roseUrl;
    public final String details;
    
    // public boolean notified = false; // Has the user been notified of this advisory?

    private static final int FG_COLOR = 0xffdddddd;
    private static final int BG_COLOR = 0x00000000;
    private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up
    
    
    public Advisory(Region region, String date, Rating rating, String roseUrl, String details) {
        this.region = region;
        this.date = date;
        this.rating = rating;
        this.roseUrl = roseUrl;
        this.details = details;
    }
    
    /**
     * Downloads the danger rose. Synchronous (will block).
     */
    public Bitmap fetchImage() {
        Bitmap bmp = fetchBitmap(roseUrl);
        if(bmp != null) {
            int fg = FG_COLOR;
            int bg = BG_COLOR;
            try {
                fg = Color.parseColor(region.roseForegroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            try {
                bg = Color.parseColor(region.roseBackgroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            bmp = replaceColor(bmp, fg, bg);
            // Scale up
            if(bmp.getHeight() < MIN_HEIGHT) {
                int width = MIN_HEIGHT * bmp.getWidth() / bmp.getHeight();
                bmp = Bitmap.createScaledBitmap(bmp, width, MIN_HEIGHT, true);
            }
        }
        return bmp;
    }

    /**
     * Downloads a bitmap. Synchronous (will block).
     */
    private static Bitmap fetchBitmap(String url) {
        Bitmap bmp = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            bmp = BitmapFactory.decodeStream(is);
            is.close();
       } catch(IOException e) {
           Log.e("Main", "Error getting bitmap", e);
       }
        return bmp;
    }
    
    /**
     * Returns a new bitmap in which foreground is replaced by white, and background is replaced by transparency
     * @param in
     * @return
     */
    private static Bitmap replaceColor(Bitmap in, int fg, int bg) {
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
    
}
