package com.ohadshai.mipmap.services;

import android.app.IntentService;
import android.content.Intent;

import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;

import java.util.ArrayList;

/**
 * Represents an {@link IntentService} for deleting search places photos from internal storage.
 */
public class DeleteLastSearchPlacesPhotosService extends IntentService {

    /**
     * Initializes a new instance of an {@link IntentService} for deleting search places photos from internal storage.
     */
    public DeleteLastSearchPlacesPhotosService() {
        super("DeleteLastSearchPlacesPhotosService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets the list of places from the intent:
        ArrayList<Place> places = intent.getParcelableArrayListExtra(UIConsts.Intent.PLACES_LIST_KEY);
        if (places == null)
            throw new NullPointerException("places");

        // Deletes each place photo of the last search from the internal storage:
        for (Place place : places)
            if (place.getPhotoReference() != null)
                Utils.Image.deleteFromInternalStorage(place.getPhotoReference(), getApplicationContext());
    }

}
