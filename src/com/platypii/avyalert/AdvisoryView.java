package com.platypii.avyalert;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.platypii.avyalert.data.Advisory;
import com.platypii.avyalert.data.AvalancheRisk;
import com.platypii.avyalert.data.AvalancheRisk.Rating;


/**
 * A class to translate an Advisory into a View.
 * An AdvisoryViewer is created for an advisory, and can then be attached and detached to a View.
 * @author platypii
 */
public class AdvisoryView {
    
    private final Advisory advisory;

    private boolean isAttached = false; // Has this advisory been attached to a view?
    private TextView dateLabel;
    private ImageView ratingIcon;
    private TextView ratingLabel;
    private ImageView roseView;
    private LinearLayout imagePanel;
    private List<ImageView> imageViews;
    private TextView detailsLabel;
    private View advisoryLink;
    private TextView centerLabel;

    private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up

    
    public AdvisoryView(Advisory advisory) {
        if(advisory == null) Log.w("AdvisoryViewer", "Advisory Viewer created with null advisory");
        this.advisory = advisory;
    }
    
    /**
     * Called when this advisory is brought into view
     * @param advisoryView a View containing all the advisory views
     */
    public void onAttach(Context context, View view) {
        if(isAttached == true) Log.w("Advisory", "["+advisory.region+"] Attaching advisory that was not detached");
        isAttached = true;
        // Find views
        dateLabel = (TextView) view.findViewById(R.id.dateLabel);
        ratingIcon = (ImageView) view.findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) view.findViewById(R.id.ratingLabel);
        roseView = (ImageView) view.findViewById(R.id.roseView);
        imagePanel = (LinearLayout) view.findViewById(R.id.imagePanel);
        imageViews = new ArrayList<ImageView>();
        for(@SuppressWarnings("unused") URL imageUrl : advisory.imageUrls) {
            // Create image views
            ImageView imgView = new ImageView(context);
            imgView.setAdjustViewBounds(true);
            imgView.setScaleType(ScaleType.CENTER_INSIDE);
            imageViews.add(imgView);
        }
        detailsLabel = (TextView) view.findViewById(R.id.detailsLabel);
        advisoryLink = view.findViewById(R.id.advisoryLink);
        centerLabel = (TextView) view.findViewById(R.id.centerLabel);
        // Update
        updateView();
    }
    
    public void updateView() {
        assert isAttached;
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
        int fg = Images.FG_COLOR;
        int bg = Images.BG_COLOR;
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
        detailsLabel.setText(formatHtml(advisory.details));
        detailsLabel.setVisibility(View.VISIBLE);
        // Link
        centerLabel.setText("from " + advisory.region.centerName);
        advisoryLink.setVisibility(View.VISIBLE);
    }
    
    /** Cleans the html and returns styled text ready for a TextView */
    private Spanned formatHtml(String html) {
        // html = html.replaceAll("(?si)</?(img|a).*?>", ""); // Remove image and link tags
        html = html.replaceAll("(?si)</?(img).*?>", ""); // Remove image tags
       return Html.fromHtml(html);
    }
    
    /** Detaches this Advisory from the View it is attached to */
    public void onDetach() {
        if(isAttached == true) Log.w("Advisory", "Detaching advisory that was not attached");
        isAttached = false;
        // Remove imageViews
        imagePanel.removeAllViews();
        // Clear the advisory
//        dateLabel.setVisibility(View.GONE);
//        ratingIcon.setVisibility(View.GONE);
//        ratingLabel.setVisibility(View.GONE);
//        roseView.setVisibility(View.GONE);
//        imagePanel.setVisibility(View.GONE);
//        detailsLabel.setText("");
//        advisoryLink.setVisibility(View.GONE);
    }
    
    /** Downloads an image and inserts into the given ImageView */
    private void fetchImage(URL imageUrl, final ImageView imageView) {
        imageView.setVisibility(View.GONE); // Hide for now, show when image is downloaded
        Images.fetchBitmapAsync(imageUrl, new Callback<Bitmap>() {
            @Override
            public void callback(Bitmap bmp) {
                if(isAttached) {
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
        Images.fetchBitmapAsync(advisory.roseUrl, new Callback<Bitmap>() {
            @Override
            public void callback(Bitmap bmp) {
                if(isAttached && bmp != null) {
                    // Advisory hasn't changed since we started fetching, so show the rose
                    // Replace foreground and background
                    bmp = Images.replaceColor(bmp, fg, bg);
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
