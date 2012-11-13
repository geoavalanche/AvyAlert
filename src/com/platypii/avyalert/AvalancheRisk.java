package com.platypii.avyalert;

import android.util.Log;


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
        if(str.matches("Extreme")) {
            return Rating.EXTREME;
        } else if(str.matches("High")) {
            return Rating.HIGH;
        } else if(str.matches("Considerable")) {
            return Rating.CONSIDERABLE;
        } else if(str.matches("Moderate")) {
            return Rating.MODERATE;
        } else if(str.matches("Low")) {
            return Rating.LOW;
        } else {
            return Rating.NONE;
        }
    }
    
    public static int getColor(Rating level) {
        switch(level) {
            case NONE:
                return 0xffffff;
            case LOW:
                return 0xccff66;
            case MODERATE:
                return 0xffff00;
            case CONSIDERABLE:
                return 0xff9900;
            case HIGH:
                return 0xff5500;
            case EXTREME:
                return 0xdd0000;
            default:
                return 0xffffff;
        }
    }
}
