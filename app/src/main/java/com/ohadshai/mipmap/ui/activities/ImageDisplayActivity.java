package com.ohadshai.mipmap.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;
import com.squareup.picasso.Picasso;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageDisplayActivity extends AppCompatActivity {

    //region Constants

    /**
     * Some older devices needs a small delay between UI widget updates and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    //endregion

    //region Private Members

    /**
     * Holds an indicator indicating whether the system UI & controls are visible or not.
     */
    private boolean _isControlsVisible;

    /**
     * Holds the FrameLayout of the main content.
     */
    private FrameLayout _flContent;

    private final Handler _hideHandler = new Handler();

    private final Runnable _hidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            _flContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    /**
     * Shows the controls.
     */
    private final Runnable _showPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    private final Runnable _hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Holds the photo view attacher control.
     */
    private PhotoViewAttacher _photoViewAttacher;

    //endregion

    //region Activity Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // Enables activity transition:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        Place place = getIntent().getParcelableExtra(UIConsts.Intent.PLACE_KEY);
        if (place == null)
            throw new NullPointerException("PLACE_KEY not provided.");

        _isControlsVisible = true;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(place.getName());
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));
        }

        _flContent = (FrameLayout) findViewById(R.id.flContent);
        _flContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle(); // Click on the content layout toggles the system UI & controls.
            }
        });

        ImageView imgImage = (ImageView) findViewById(R.id.imgImage);
        if (place.getPhotoReference() != null) {
            File imgFile = new File(Utils.Image.getInternalStoragePath(this), place.getPhotoReference() + ".jpg");
            if (imgFile.exists())
                Picasso.with(this).load(imgFile).placeholder(R.drawable.no_place_image).into(imgImage);
            else
                Picasso.with(this).load(GooglePlacesConsts.Urls.GET_PHOTO_BY_REFERENCE + place.getPhotoReference()).placeholder(R.drawable.no_place_image).into(imgImage);
        } else {
            Picasso.with(this).load(R.drawable.no_place_image).into(imgImage);
        }
        _photoViewAttacher = new PhotoViewAttacher(imgImage);
        _photoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                toggle(); // Click on the content layout toggles the system UI & controls.
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        this.show();
        this.delayedHide(500); // Hides the controls shortly after the activity has been created, to briefly hint to the user that controls are available.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (_photoViewAttacher != null)
            _photoViewAttacher.cleanup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (_photoViewAttacher != null)
            _photoViewAttacher.cleanup();
    }

    //endregion

    //region Private Methods

    /**
     * Toggles to show / hide the system UI & controls.
     */
    private void toggle() {
        if (_isControlsVisible)
            hide();
        else
            show();
    }

    /**
     * Shows the system UI & controls.
     */
    @SuppressLint("InlinedApi")
    private void show() {
        // Shows the system bar:
        _flContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        _isControlsVisible = true;

        // Schedules a runnable to display UI elements after a delay:
        _hideHandler.removeCallbacks(_hidePart2Runnable);
        _hideHandler.postDelayed(_showPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Hides the system UI & controls.
     */
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        _isControlsVisible = false;

        // Schedules a runnable to remove the status and navigation bar after a delay:
        _hideHandler.removeCallbacks(_showPart2Runnable);
        _hideHandler.postDelayed(_hidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        _hideHandler.removeCallbacks(_hideRunnable);
        _hideHandler.postDelayed(_hideRunnable, delayMillis);
    }

    //endregion

}
