package com.ohadshai.mipmap.utils;

import com.ohadshai.mipmap.entities.Place;

import java.util.ArrayList;

/**
 * Represents an interface for synchronizing the places list with the map.
 * Created by Ohad on 2/8/2017.
 */
public interface PlaceSynchronizationListener {

    /**
     * Occurs when a list of places is added from the search.
     *
     * @param places The list of places added from the search.
     */
    public void onSearchListAdd(ArrayList<Place> places);

    /**
     * Occurs when a list of places from the search is requested to be shown on the map.
     *
     * @param places The list of places requested to be shown on the map.
     */
    public void onSearchListShowOnMap(ArrayList<Place> places);

    /**
     * Occurs when a place is requested to be shown on the map.
     *
     * @param place The place to show on the map.
     */
    public void onPlaceShowOnMap(Place place);

}
