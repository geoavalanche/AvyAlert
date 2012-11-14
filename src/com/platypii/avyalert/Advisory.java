package com.platypii.avyalert;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.platypii.avyalert.AvalancheRisk.Rating;
import android.content.Context;
import android.util.Log;


/**
 * Represents an ESAC Advisory. Pulls from http.
 * @author platypii
 */
public class Advisory {
    
    // private static final String url = "http://esavalanche.org/advisory";
    private static final String url = "http://platypiiindustries.com/esac/advisory";
    
    public Rating rating = Rating.NONE;
    private String details = "";
    
    // TODO: Download avalanche rose
    
    
    /**
     * Load the latest ESAC Advisory
     * @throws IOException 
     */
    public Advisory(Context context) throws IOException {
        Log.i("ESAC", "Connecting to esavalanche.org");

        Document doc = Jsoup.connect(url).get();
        Elements divAdvisory = doc.select("div.forecast-advisory");
        
        // Parse rating
        Elements divRating = divAdvisory.select(".rating div");
        rating = AvalancheRisk.parseRating(divRating.first().text()); // TODO: More robust selector
        
        // Parse details
        details = divAdvisory.text();
    }
    
    public String getDetails() {
        if(details == null)
            return "";
        else
            return details;
    }

    public String toString() {
        return rating.toString();
    }
    
}
