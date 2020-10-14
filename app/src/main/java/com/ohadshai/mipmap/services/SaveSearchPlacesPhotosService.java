package com.ohadshai.mipmap.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;

import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Represents an {@link IntentService} for saving search places photos to internal storage.
 */
public class SaveSearchPlacesPhotosService extends IntentService {

    /**
     * Initializes a new instance of an {@link IntentService} for saving search places photos to internal storage.
     */
    public SaveSearchPlacesPhotosService() {
        super("SaveSearchPlacesPhotosService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets the list of places from the intent:
        ArrayList<Place> places = intent.getParcelableArrayListExtra(UIConsts.Intent.PLACES_LIST_KEY);
        if (places == null)
            throw new NullPointerException("places");

        for (Place place : places) {
            // Checks if there's a photo to save:
            if (place.getPhotoReference() != null) {
                try {
                    // Gets the photo bitmap and saves it to internal storage:
                    Bitmap bitmap = Picasso.with(getApplicationContext()).load(GooglePlacesConsts.Urls.GET_PHOTO_BY_REFERENCE + place.getPhotoReference()).get();
                    Utils.Image.saveToInternalStorage(bitmap, place.getPhotoReference(), getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Checks if there's a place type icon to save:
            if (place.getIcon() != null && place.getIcon().getIconUrl() != null) {
                File file = new File(Utils.Image.getInternalStoragePath(getApplicationContext()), place.getIcon().getName() + ".png");
                // Checks if the icon was not already saved:
                if (!file.exists()) {
                    try {
                        // Gets the icon bitmap and saves it to internal storage:
                        Bitmap bitmap = Picasso.with(getApplicationContext()).load(place.getIcon().getIconUrl()).get();
                        Utils.Image.saveIconToInternalStorage(bitmap, place.getIcon().getName(), getApplicationContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
