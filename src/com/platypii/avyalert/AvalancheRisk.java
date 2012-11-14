package com.platypii.avyalert;

import android.util.Log;


/**
 * This class contains general knowledge about avalanche risk
 * @author platypii
 */
public class AvalancheRisk {

    public enum Rating {NONE, LOW, MODERATE, CONSIDERABLE, HIGH, EXTREME}

    // Taken from U.S. Avalanche Danger Descriptors 
    public String desc[] = new String[] {"Natural avalanches very unlikely. Skier-triggered avalanches unlikely.  Generally stable snow. Isolated areas of instability.  Travel is generally safe. Normal caution advised.",
                                          "Natural avalanches unlikely. Skier-triggered avalanches possible.  Unstable slabs possible on steep terrain.  Use caution in steeper terrain on certain aspects (defined in accompanying statement).",
                                          "Natural avalanches possible. Skier-triggered avalanches probable.  Unstable slabs probable on steep terrain.  Be increasingly cautious in steeper terrain.",
                                          "Natural and human triggered avalanches likely.  Unstable slabs likely on a variety of aspects and slope angles.  Travel in avalanche terrain is not recommended. Safest travel on windward ridges of lower angle slopes without steeper terrain above.",
                                          "Widespread natural or human triggered avalanches certain.  Extremely unstable slabs certain on most aspects and slope angles. Large destructive avalanches possible.  Travel in avalanche terrain should be avoided and travel confined to low angle terrain well away from avalanche path run-outs."};
    
    public static Rating parseRating(String str) {
        Log.i("AvalancheRisk", "parsing: \"" + str + "\"");
        if(str.matches("Extreme")) return Rating.EXTREME;
        else if(str.matches("High")) return Rating.HIGH;
        else if(str.matches("Considerable")) return Rating.CONSIDERABLE;
        else if(str.matches("Moderate")) return Rating.MODERATE;
        else if(str.matches("Low")) return Rating.LOW;
        else return Rating.NONE;
    }
    
    /**
     * Returns the RGB color representing the given hazard rating
     */
    public static int getColor(Rating rating) {
        switch(rating) {
            case NONE: return 0xffffff;
            case LOW: return 0x00ff00;
            case MODERATE: return 0xffff00;
            case CONSIDERABLE: return 0xffff00;
            case HIGH: return 0xee1100;
            case EXTREME: return 0xcc0000;
            default: return 0xffffff;
        }
    }

    /**
     * Returns the ARGB color representing the given hazard rating
     */
    public static int getForegroundColor(Rating rating) {
        switch(rating) {
            case NONE: return 0xffffffff;
            case LOW: return 0xff44bb44;
            case MODERATE: return 0xffffff00;
            case CONSIDERABLE: return 0xffff9900;
            case HIGH: return 0xffee0000;
            case EXTREME: return 0xff000000;
            default: return 0xffffffff;
        }
    }

    /**
     * Returns the ARGB color representing the given hazard rating
     */
    public static int getBackgroundColor(Rating rating) {
        switch(rating) {
            case EXTREME: return 0xffdd0000;
            default: return 0x00000000;
        }
    }
    
    public static int getImage(Rating rating) {
        switch(rating) {
            case NONE: return R.drawable.danger0;
            case LOW: return R.drawable.danger1;
            case MODERATE: return R.drawable.danger2;
            case CONSIDERABLE: return R.drawable.danger3;
            case HIGH: return R.drawable.danger4;
            case EXTREME: return R.drawable.danger5;
            default: return R.drawable.danger0;
        }
    }

}
