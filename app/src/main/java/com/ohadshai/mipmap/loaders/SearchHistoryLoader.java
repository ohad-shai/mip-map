package com.ohadshai.mipmap.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Search;

import java.util.ArrayList;

/**
 * Represents a loader for the search history.
 * Created by Ohad on 1/7/2017.
 */
public class SearchHistoryLoader extends AsyncTaskLoader<ArrayList<Search>> {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a loader for the search history.
     *
     * @param context The context owner.
     */
    public SearchHistoryLoader(Context context) {
        super(context);
        _repository = DBHandler.getInstance(context);
    }

    //endregion

    //region Loader Events

    @Override
    public ArrayList<Search> loadInBackground() {
        return _repository.searchHistory.getAll();
    }

    //endregion

}
