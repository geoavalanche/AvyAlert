package com.platypii.avyalert;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.platypii.avyalert.data.Advisory;
import com.platypii.avyalert.data.AvalancheRisk;
import com.platypii.avyalert.data.Callback;
import com.platypii.avyalert.data.ImagesOLDDD;
import com.platypii.avyalert.data.AvalancheRisk.Rating;


/**
 * A class to translate an Advisory into a View.
 * An AdvisoryViewer is created for an advisory, and can then be attached and detached to a View.
 * @author platypii
 */
public class AdvisoryView extends RelativeLayout {

    private Context context;
    
    private Advisory advisory;

    private TextView dateLabel;
    private ImageView ratingIcon;
    private TextView ratingLabel;
    private ImageView roseView;
    private LinearLayout imagePanel;
    private List<ImageView> imageViews;
    private TextView detailsLabel;


    private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up

    
    public AdvisoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.advisory_view, this, true);
        // Find views
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        ratingIcon = (ImageView) findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) findViewById(R.id.ratingLabel);
        roseView = (ImageView) findViewById(R.id.roseView);
        imagePanel = (LinearLayout) findViewById(R.id.imagePanel);
        detailsLabel = (TextView) findViewById(R.id.detailsLabel);
    }

    /**
     * Called when this advisory is brought into view
     */
    public void setAdvisory(Advisory advisory) {
        if(advisory == null)
            Log.i("AdvisoryViewer", "Attached null advisory to AdvisoryViewer");
        this.advisory = advisory;

        // Build image views list
        imageViews = new ArrayList<ImageView>();
        if(advisory != null) {
            for(@SuppressWarnings("unused") URL imageUrl : advisory.imageUrls) {
                // Create image views
                ImageView imgView = new ImageView(context);
                imgView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
                imgView.setAdjustViewBounds(true);
                imgView.setScaleType(ScaleType.CENTER_INSIDE);
                imageViews.add(imgView);
            }
        }
        // Update
        updateView();
    }
    
    public void updateView() {
        if(advisory != null) {
            // Load advisory into views
            ratingLabel.setBackgroundColor(AvalancheRisk.getBackgroundColor(advisory.rating));
            if(advisory.rating != Rating.NONE) {
                ratingIcon.setImageResource(AvalancheRisk.getImage(advisory.rating));
                ratingLabel.setText(advisory.rating.toString());
                ratingLabel.setTextColor(AvalancheRisk.getForegroundColor(advisory.rating));
                ratingIcon.setVisibility(View.VISIBLE);
                ratingLabel.setVisibility(View.VISIBLE);
            } else {
                ratingIcon.setVisibility(View.GONE);
                ratingLabel.setVisibility(View.GONE);
            }
            if(advisory.date == null || advisory.date.equals("")) {
                dateLabel.setVisibility(View.GONE);
            } else {
                dateLabel.setText("Date: " + advisory.date);
                dateLabel.setVisibility(View.VISIBLE);
            }
            
            // Rose
            // Color replacement
            int fg = ImagesOLDDD.FG_COLOR;
            int bg = ImagesOLDDD.BG_COLOR;
            try {
                fg = Color.parseColor(advisory.region.roseForegroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            try {
                bg = Color.parseColor(advisory.region.roseBackgroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            fetchImageReplaceColor(advisory.roseUrl, roseView, fg, bg);
    
            // Extra images
            imagePanel.removeAllViews();
            assert imageViews.size() == advisory.imageUrls.size();
            for(int i = 0; i < imageViews.size(); i++) {
                ImageView imageView = imageViews.get(i);
                URL imageUrl = advisory.imageUrls.get(i);
                imagePanel.addView(imageView);
                fetchImage(imageUrl, imageView);
            }
    
            // Details
            try {
                detailsLabel.setText(formatHtml(advisory.details));
            } catch(IndexOutOfBoundsException e) {
                Log.e("AdvisoryView", "Error rendering details", e);
            }
            detailsLabel.setVisibility(View.VISIBLE);

            this.setVisibility(View.VISIBLE);
        } else {
            // Null advisory
            this.setVisibility(View.INVISIBLE);
            imagePanel.removeAllViews();
            dateLabel.setVisibility(View.GONE);
            ratingIcon.setVisibility(View.GONE);
            ratingLabel.setVisibility(View.GONE);
            roseView.setVisibility(View.GONE);
            imagePanel.setVisibility(View.GONE);
            detailsLabel.setText("");
        }
    }
    
    /** Cleans the html and returns styled text ready for a TextView */
    private Spanned formatHtml(String html) {
        // html = html.replaceAll("(?si)</?(img|a).*?>", ""); // Remove image and link tags
        html = html.replaceAll("(?si)</?(img).*?>", ""); // Remove image tags
       return Html.fromHtml(html);
    }
    
    /** Downloads an image and inserts into the given ImageView */
    private void fetchImage(URL imageUrl, final ImageView imageView) {
        imageView.setVisibility(View.GONE); // Hide for now, show when image is downloaded
        final Advisory fetchedAdvisory = advisory; // Save to check if its changed
        ImagesOLDDD.fetchBitmapAsync(imageUrl, new Callback<Bitmap>() {
            @Override
            public void callback(Bitmap bmp) {
                if(Util.eq(advisory, fetchedAdvisory)) {
                    // Advisory hasn't changed since we started fetching, so show the rose
                    imageView.setImageBitmap(bmp);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
        
    /** Special method for the rose to do color replacement */
    private void fetchImageReplaceColor(URL imageUrl, final ImageView imageView, final int fg, final int bg) {
        imageView.setVisibility(View.GONE); // Hide for now, show when image is downloaded
        // Fetch image
        final Advisory fetchedAdvisory = advisory; // Save to check if its changed
        ImagesOLDDD.fetchBitmapAsync(advisory.roseUrl, new Callback<Bitmap>() {
            @Override
            public void callback(Bitmap bmp) {
                if(Util.eq(advisory, fetchedAdvisory) && bmp != null) {
                    // Advisory hasn't changed since we started fetching, so show the rose
                    // Replace foreground and background
                    bmp = ImagesOLDDD.replaceColor(bmp, fg, bg);
                    if(bmp.getHeight() < MIN_HEIGHT) {
                        // Scale up if the image is too small
                        int width = MIN_HEIGHT * bmp.getWidth() / bmp.getHeight();
                        bmp = Bitmap.createScaledBitmap(bmp, width, MIN_HEIGHT, true);
                    }
                    imageView.setImageBitmap(bmp);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}
