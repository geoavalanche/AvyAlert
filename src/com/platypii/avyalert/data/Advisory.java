package com.platypii.avyalert.data;

import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.platypii.avyalert.AvalancheRisk.Rating;
import com.platypii.avyalert.AvalancheRisk;
import com.platypii.avyalert.Images;
import com.platypii.avyalert.R;


/**
 * Represents an Avalanche Advisory
 * @author platypii
 */
public class Advisory {

    public final Region region;
    public final String date;
    public final Rating rating;
    public final String roseUrl;
    public final List<String> imageUrls;
    public final String details;

    private boolean isAttached = false; // Has this advisory been attached to a view?
    private TextView dateLabel;
    private ImageView ratingIcon;
    private TextView ratingLabel;
    private ImageView roseView;
    private LinearLayout imagePanel;
    private TextView detailsLabel;
    private View advisoryLink;
    private TextView centerLabel;
    
    // public boolean notified = false; // Has the user been notified of this advisory?

    private static final int MIN_HEIGHT = 300; // Minimum image height, otherwise scale up

    
    public Advisory(Region region, String date, Rating rating, String roseUrl, List<String> imageUrls, String details) {
        this.region = region;
        this.date = date;
        this.rating = rating;
        this.roseUrl = roseUrl;
        this.imageUrls = imageUrls;
        this.details = details;
    }
    
    /**
     * Called when this advisory is brought into view
     * @param advisoryView a View containing all the advisory views
     */
    public void onAttach(View view) {
        if(isAttached == true) Log.w("Advisory", "Attaching advisory that was not detached");
        isAttached = true;
        // Find views
        dateLabel = (TextView) view.findViewById(R.id.dateLabel);
        ratingIcon = (ImageView) view.findViewById(R.id.ratingIcon);
        ratingLabel = (TextView) view.findViewById(R.id.ratingLabel);
        roseView = (ImageView) view.findViewById(R.id.roseView);
        imagePanel = (LinearLayout) view.findViewById(R.id.imagePanel);
        detailsLabel = (TextView) view.findViewById(R.id.detailsLabel);
        advisoryLink = view.findViewById(R.id.advisoryLink);
        centerLabel = (TextView) view.findViewById(R.id.centerLabel);
        // Update
        updateView();
    }
    
    public void updateView() {
        assert isAttached;
        // Load advisory into views
        ratingLabel.setBackgroundColor(AvalancheRisk.getBackgroundColor(rating));
        if(rating != Rating.NONE) {
            ratingIcon.setImageResource(AvalancheRisk.getImage(rating));
            ratingLabel.setText(rating.toString());
            ratingLabel.setTextColor(AvalancheRisk.getForegroundColor(rating));
            ratingIcon.setVisibility(View.VISIBLE);
            ratingLabel.setVisibility(View.VISIBLE);
        } else {
            ratingIcon.setVisibility(View.GONE);
            ratingLabel.setVisibility(View.GONE);
        }
        if(date == null || date.equals("")) {
            dateLabel.setVisibility(View.GONE);
        } else {
            dateLabel.setText("Date: " + date);
            dateLabel.setVisibility(View.VISIBLE);
        }
        fetchImage(roseView, roseUrl);

        // Details
        detailsLabel.setText(Html.fromHtml(details));
        detailsLabel.setVisibility(View.VISIBLE);
        // Link
        centerLabel.setText("from " + region.regionName);
        advisoryLink.setVisibility(View.VISIBLE);
    }
    
    public void onDetach() {
        if(isAttached == true) Log.w("Advisory", "Dettaching advisory that was not attached");
        isAttached = false;
        // Clear the advisory
//        view.findViewById(R.id.dateLabel).setVisibility(View.GONE);
//        view.findViewById(R.id.ratingIcon).setVisibility(View.GONE);
//        view.findViewById(R.id.ratingLabel).setVisibility(View.GONE);
//        view.findViewById(R.id.roseView).setVisibility(View.GONE);
//        view.findViewById(R.id.imagePanel).setVisibility(View.GONE);
//        ((TextView) view.findViewById(R.id.detailsLabel)).setText("");
//        view.findViewById(R.id.advisoryLink).setVisibility(View.GONE);
    }
    
    private void fetchImage(final ImageView imageView, final String url) {
        imageView.setVisibility(View.GONE); // Hide for now, show when image is downloaded
        if(url != null && !url.equals("")) {
            // Asynchronously get image
            new AsyncTask<Void,Void,Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return fetchRose();
                }
                @Override
                protected void onPostExecute(Bitmap result) {
                    if(isAttached) {
                        // Advisory hasn't changed since we started fetching, so show the rose
                        imageView.setImageBitmap(result);
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }.execute();
        }
    }

    /**
     * Downloads the danger rose. Synchronous (will block).
     */
    public Bitmap fetchRose() {
        Bitmap bmp = Images.fetchBitmap(roseUrl);
        if(bmp != null) {
            int fg = Images.FG_COLOR;
            int bg = Images.BG_COLOR;
            try {
                fg = Color.parseColor(region.roseForegroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            try {
                bg = Color.parseColor(region.roseBackgroundColor);
            } catch(IllegalArgumentException e) {
            } catch(NullPointerException e) {}
            bmp = Images.replaceColor(bmp, fg, bg);
            if(bmp.getHeight() < MIN_HEIGHT) {
                // Scale up if the image is too small
                int width = MIN_HEIGHT * bmp.getWidth() / bmp.getHeight();
                bmp = Bitmap.createScaledBitmap(bmp, width, MIN_HEIGHT, true);
            }
        }
        return bmp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Advisory && obj != null) {
            Advisory other = (Advisory) obj;
            if(!region.equals(other.region)) return false;
            if(!eq(date, other.date)) return false;
            if(rating != other.rating) return false;
            if(!eq(roseUrl, other.roseUrl)) return false;
            if(!eq(details, other.details)) return false;
            // TODO: imageUrls
            return true;
        }
        return false;
    }
    private static boolean eq(String a, String b) {
        return (a == null && b == null) || a.equals(b);
    }

}
