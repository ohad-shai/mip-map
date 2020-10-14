package com.ohadshai.mipmap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.utils.PlaceSynchronizationListener;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Represents an adapter for the last search places list.
 * Created by Ohad on 1/6/2017.
 */
public class LastSearchPlacesAdapter extends RecyclerView.Adapter<LastSearchPlacesAdapter.ViewHolder> {

    //region Private Members

    /**
     * Holds the list of places to adapt.
     */
    private ArrayList<Place> _places;

    /**
     * Holds the activity owner of the adapter.
     */
    private Activity _activity;

    /**
     * Holds the unit of length for the radius value of the places distance.
     */
    private int _unitOfLength;

    /**
     * Holds the user location coordinates.
     */
    private LatLng _userLocation;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of an adapter for the last search places list.
     *
     * @param places   The list of places to adapt.
     * @param activity The activity owner of the adapter.
     */
    public LastSearchPlacesAdapter(ArrayList<Place> places, Activity activity) {
        this._places = places;
        this._activity = activity;
    }

    //endregion

    //region Events

    @Override
    public LastSearchPlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_small, parent, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_activity);
        this._unitOfLength = Integer.valueOf(sp.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
        this._userLocation = Utils.LatLng.parse(sp.getString(Utils.Preferences.Keys.LAST_LOCATION, null));

        return new LastSearchPlacesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LastSearchPlacesAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bindViewHolder(_places.get(position)); // Gets the place by the position, and binds it to the view holder.
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        Picasso.with(_activity).cancelRequest(holder._imgPlace);
    }

    @Override
    public int getItemCount() {
        return _places.size();
    }

    //endregion

    //region Public API

    /**
     * Sets the list of places to adapt (Overrides the current list if exists).
     *
     * @param places The list of places to adapt.
     */
    public void setPlaces(ArrayList<Place> places) {
        this._places = places;
    }

    /**
     * Gets the list of all the place items in the adapter.
     *
     * @return Returns the list of all the place items in the adapter.
     */
    public ArrayList<Place> getPlaces() {
        return _places;
    }

    /**
     * Refreshes the list.
     */
    public void refreshList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_activity);
        this._unitOfLength = Integer.valueOf(sp.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
        this._userLocation = Utils.LatLng.parse(sp.getString(Utils.Preferences.Keys.LAST_LOCATION, null));
        notifyDataSetChanged();
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a view holder for an item in the adapter.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        //region Private Members

        /**
         * Holds the place object of the current position item.
         */
        private Place _place;

        private CardView _cardPlace;
        private ImageView _imgPlace;
        private TextView _lblPlaceName;
        private TextView _lblPlaceDistance;
        private TextView _lblPlaceAddress;
        private LinearLayout _llPlaceDistance;

        //endregion

        /**
         * C'tor
         * Initializes a new instance of a view holder for an item in the adapter.
         *
         * @param itemView The view of the item in the adapter.
         */
        ViewHolder(final View itemView) {
            super(itemView);

            _cardPlace = (CardView) itemView.findViewById(R.id.cardPlace);
            _cardPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PlaceSynchronizationListener) _activity).onPlaceShowOnMap(_places.get(getAdapterPosition()));
                }
            });

            _imgPlace = (ImageView) itemView.findViewById(R.id.imgPlace);

            _lblPlaceName = (TextView) itemView.findViewById(R.id.lblPlaceName);

            _lblPlaceDistance = (TextView) itemView.findViewById(R.id.lblPlaceDistance);

            _lblPlaceAddress = (TextView) itemView.findViewById(R.id.lblPlaceAddress);

            _llPlaceDistance = (LinearLayout) itemView.findViewById(R.id.llPlaceDistance);

        }

        //region Local Methods

        /**
         * Binds a place object to the view holder.
         *
         * @param place The place object to bind.
         */
        void bindViewHolder(Place place) {
            this._place = place;

            // Sets item views, based on the views and the data model:

            // Checks if there's a photo to show:
            if (place.getPhotoReference() != null) {
                File imgFile = new File(Utils.Image.getInternalStoragePath(_activity), place.getPhotoReference() + ".jpg");
                if (imgFile.exists()) {
                    Picasso.with(_activity).load(imgFile)
                            .placeholder(R.drawable.no_place_image)
                            .fit()
                            .centerCrop()
                            .into(_imgPlace);
                } else {
                    Picasso.with(_activity).load(GooglePlacesConsts.Urls.GET_PHOTO_BY_REFERENCE + place.getPhotoReference())
                            .placeholder(R.drawable.no_place_image)
                            .fit()
                            .centerCrop()
                            .into(_imgPlace);
                }
            } else {
                // No photo to show:
                Picasso.with(_activity).load(R.drawable.no_place_image).into(_imgPlace);
            }

            _lblPlaceName.setText(place.getName());

            // Checks if there's a user location:
            if (_userLocation != null) {
                if (_unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                    _lblPlaceDistance.setText(Utils.Units.displayMeters((int) Utils.LatLng.calculateDistanceInMeters(_userLocation, place.getLocation()), _activity));
                else if (_unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                    _lblPlaceDistance.setText(Utils.Units.displayYards((int) (Utils.LatLng.calculateDistanceInMeters(_userLocation, place.getLocation()) * Utils.Units.M_TO_YD), _activity));
            } else {
                // No user location:
                _llPlaceDistance.setVisibility(View.GONE);
            }

            if (place.getAddress() != null)
                _lblPlaceAddress.setText(place.getAddress());
            else if (place.getVicinity() != null)
                _lblPlaceAddress.setText(place.getVicinity());

        }

        //endregion

    }

    //endregion

}
