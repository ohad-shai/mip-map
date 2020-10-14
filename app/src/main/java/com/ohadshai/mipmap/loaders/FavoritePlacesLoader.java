package com.ohadshai.mipmap.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;

import java.util.ArrayList;

/**
 * Represents a loader for the favorite places list.
 * Created by Ohad on 1/7/2017.
 */
public class FavoritePlacesLoader extends AsyncTaskLoader<ArrayList<Place>> {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a loader for the favorite places list.
     *
     * @param context The context owner.
     */
    public FavoritePlacesLoader(Context context) {
        super(context);
        _repository = DBHandler.getInstance(context);
    }

    //endregion

    //region Loader Events

    @Override
    public ArrayList<Place> loadInBackground() {
        return _repository.places.getAllFavorites();
    }

    //endregion

}
