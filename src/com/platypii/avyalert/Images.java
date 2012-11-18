package com.platypii.avyalert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


    /**
     * Downloads a bitmap. Synchronous (will block).
     */
    public static Bitmap fetchBitmap(String url) {
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
    
}
