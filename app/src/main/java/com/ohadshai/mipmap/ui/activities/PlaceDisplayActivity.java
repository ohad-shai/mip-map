package com.ohadshai.mipmap.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.PlaceType;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.ohadshai.mipmap.R.id.actionShare;
import static com.ohadshai.mipmap.R.id.fabFavorite;

public class PlaceDisplayActivity extends AppCompatActivity {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the place object to display.
     */
    private Place _place;

    /**
     * Holds the FloatingActionButton control for the favorite action.
     */
    private FloatingActionButton _fabFavorite;

    //endregion

    //region Activity Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enables activity transition:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setSharedElementsUseOverlay(false);

            Transition enter = new Explode();
            getWindow().setEnterTransition(enter);

            Transition exit = new Slide();
            getWindow().setExitTransition(exit);
        }
        setContentView(R.layout.activity_place_display);

        //region Controls initialization...

        _repository = DBHandler.getInstance(this);

        int placeId = getIntent().getIntExtra(UIConsts.Intent.PLACE_ID_KEY, -1);
        String googlePlaceId = getIntent().getStringExtra(UIConsts.Intent.GOOGLE_PLACE_ID_KEY);

        if (placeId > 0)
            _place = _repository.places.getById(placeId); // Gets the place by the id.
        else if (googlePlaceId != null)
            _place = _repository.places.getByGoogleId(googlePlaceId); // Gets the place by the google id.
        else
            throw new IllegalStateException("PLACE_ID_KEY or GOOGLE_PLACE_ID_KEY not provided.");

        //region Place Image

        final ImageView imgPlace = (ImageView) findViewById(R.id.imgPlace);
        imgPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent displayIntent = new Intent(PlaceDisplayActivity.this, ImageDisplayActivity.class);
                displayIntent.putExtra(UIConsts.Intent.PLACE_KEY, _place);

                // Checks if can make an activity transition:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(PlaceDisplayActivity.this, imgPlace, "image");
                    startActivity(displayIntent, options.toBundle());
                } else {
                    startActivity(displayIntent);
                }
            }
        });
        if (_place.getPhotoReference() != null) {
            File imgFile = new File(Utils.Image.getInternalStoragePath(this), _place.getPhotoReference() + ".jpg");
            if (imgFile.exists())
                Picasso.with(this).load(imgFile).into(imgPlace);
            else
                Picasso.with(this).load(GooglePlacesConsts.Urls.GET_PHOTO_BY_REFERENCE + _place.getPhotoReference()).into(imgPlace);
        } else {
            Picasso.with(this).load(R.drawable.no_place_image).into(imgPlace);
        }

        //endregion

        //region FAB Favorite

        _fabFavorite = (FloatingActionButton) findViewById(fabFavorite);
        _fabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_place.isFavorite()) {
                    _fabFavorite.setColorFilter(Color.parseColor("#444444"));
                    _repository.places.unfavorite(_place.getId());
                    _place.setFavorite(false);
                } else {
                    _fabFavorite.setColorFilter(Color.RED);
                    _repository.places.favorite(_place.getId());
                    _place.setFavorite(true);
                }
            }
        });
        if (_place.isFavorite()) {
            _fabFavorite.setColorFilter(Color.RED);
        } else
            _fabFavorite.setColorFilter(Color.parseColor("#444444"));
        // Animates the fab on start:
        Utils.UI.showFabWithAnimation(_fabFavorite, 350);

        //endregion

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(_place.getName());

        TextView lblPlaceName = (TextView) findViewById(R.id.lblPlaceName);
        lblPlaceName.setText(_place.getName());

        TextView lblPlaceAddress = (TextView) findViewById(R.id.lblPlaceAddress);
        if (_place.getAddress() != null)
            lblPlaceAddress.setText(_place.getAddress());
        else if (_place.getVicinity() != null)
            lblPlaceAddress.setText(_place.getVicinity());

        TextView lblPlaceTypes = (TextView) findViewById(R.id.lblPlaceTypes);
        lblPlaceTypes.setText(PlaceType.listToString(_place.getTypes()));

        ImageView imgPlaceIcon = (ImageView) findViewById(R.id.imgPlaceIcon);
        if (_place.getIcon() != null) {
            File iconFile = new File(Utils.Image.getInternalStoragePath(this), _place.getIcon().getName() + ".png");
            if (iconFile.exists())
                Picasso.with(this).load(iconFile).placeholder(R.drawable.no_place_type_icon).into(imgPlaceIcon);
            else
                Picasso.with(this).load(_place.getIcon().getIconUrl()).placeholder(R.drawable.no_place_type_icon).into(imgPlaceIcon);
        } else {
            Picasso.with(this).load(R.drawable.no_place_type_icon).into(imgPlaceIcon);
        }

        // Checks if there's a rating for the place:
        if (_place.getRating() > 0) {
            RatingBar ratingPlace = (RatingBar) findViewById(R.id.ratingPlace);
            ratingPlace.setRating(_place.getRating());

            TextView lblPlaceRating = (TextView) findViewById(R.id.lblPlaceRating);
            lblPlaceRating.setText(String.valueOf(_place.getRating()));
        }

        Button btnShowOnMap = (Button) findViewById(R.id.btnShowOnMap);
        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(UIConsts.Intent.PLACE_KEY, _place);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        //endregion

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case actionShare:
                this.actionShare();
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.UI.showFabWithAnimation(_fabFavorite, 350);
    }

    //endregion

    //region Private Methods

    /**
     * Method procedure for menu action: "Share".
     */
    private void actionShare() {
        StringBuilder share = new StringBuilder();
        share.append(_place.getName())
                .append("\n")
                .append(_place.getAddress() != null ? _place.getAddress() : (_place.getVicinity() != null ? _place.getVicinity() : ""))
                .append("\n")
                .append(GooglePlacesConsts.Urls.GOOGLE_PLACE_WEB_DISPLAY).append(Uri.encode(_place.getName())).append("/@").append(_place.getLocation().latitude).append(",").append(_place.getLocation().longitude).append(",18z/")
                .append("\n\n");

        // Starts the share intent:
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, share.substring(0, share.length() - 2));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.general_msg_share_places)));
    }

    //endregion

}
