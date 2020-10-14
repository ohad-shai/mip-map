package com.ohadshai.mipmap.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.PlaceType;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.activities.PlaceDisplayActivity;
import com.ohadshai.mipmap.ui.fragments.MapContainerFragment;
import com.ohadshai.mipmap.ui.views.FavoritesRevealHelper;
import com.ohadshai.mipmap.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.ohadshai.mipmap.R.id.chkSelect;
import static com.ohadshai.mipmap.R.id.layoutSelect;

/**
 * Represents an adapter for the favorite places list.
 * Created by Ohad on 1/6/2017.
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder> {

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
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the unit of length for the radius value of the places distance.
     */
    private int _unitOfLength;

    /**
     * Holds the user location coordinates.
     */
    private LatLng _userLocation;

    /**
     * Holds the selection mode interactions object.
     */
    private FavoritesRevealHelper.SelectionMode _selectionMode;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of an adapter for the favorite places list.
     *
     * @param places   The list of places to adapt.
     * @param activity The activity owner of the adapter.
     */
    public FavoritePlacesAdapter(ArrayList<Place> places, Activity activity) {
        this._places = places;
        this._activity = activity;
        this._repository = DBHandler.getInstance(activity);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_activity);
        this._unitOfLength = Integer.valueOf(sp.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
        this._userLocation = Utils.LatLng.parse(sp.getString(Utils.Preferences.Keys.LAST_LOCATION, null));
    }

    //endregion

    //region Events

    @Override
    public FavoritePlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new FavoritePlacesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoritePlacesAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bindViewHolder(_places.get(position)); // Gets the place by the position, and binds it to the view holder.
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        Picasso.with(_activity.getApplicationContext()).cancelRequest(holder._imgPlace);
        Picasso.with(_activity.getApplicationContext()).cancelRequest(holder._imgPlaceIcon);
    }

    @Override
    public int getItemCount() {
        return _places.size();
    }

    //endregion

    //region Public API

    /**
     * Sets the activity owner of the adapter.
     *
     * @param activity The activity owner of the adapter to set.
     */
    public void setActivity(@NonNull Activity activity) {
        this._activity = activity;
    }

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
     * Sets the selection mode object of the adapter.
     *
     * @param selectionMode The selection mode object of the adapter to set.
     */
    public void setSelectionMode(FavoritesRevealHelper.SelectionMode selectionMode) {
        this._selectionMode = selectionMode;
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
        private ImageView _imgPlaceIcon;
        private TextView _lblPlaceTypes;
        private LinearLayout _llPlaceDistance;

        private RelativeLayout _layoutSelect;
        private CheckBox _chkSelect;

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
                    Intent displayIntent = new Intent(_activity, PlaceDisplayActivity.class);
                    displayIntent.putExtra(UIConsts.Intent.PLACE_ID_KEY, _place.getId());

                    // Checks if can make an activity transition:
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(_activity, _imgPlace, "image");
                        _activity.startActivityForResult(displayIntent, MapContainerFragment.PLACE_DISPLAY_REQUEST_CODE, options.toBundle());
                    } else {
                        _activity.startActivityForResult(displayIntent, MapContainerFragment.PLACE_DISPLAY_REQUEST_CODE);
                    }
                }
            });
            _cardPlace.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Checks if not already in selection mode:
                    if (_selectionMode == null || !_selectionMode.isInSelectionMode()) {
                        // Enters the selection mode:
                        _selectionMode = FavoritesRevealHelper.with((AppCompatActivity) _activity).enterSelectionMode(FavoritePlacesAdapter.this);
                        select(true); // Selects this item that started the selection mode.
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            _imgPlace = (ImageView) itemView.findViewById(R.id.imgPlace);

            _lblPlaceName = (TextView) itemView.findViewById(R.id.lblPlaceName);

            _lblPlaceDistance = (TextView) itemView.findViewById(R.id.lblPlaceDistance);

            _lblPlaceAddress = (TextView) itemView.findViewById(R.id.lblPlaceAddress);

            _imgPlaceIcon = (ImageView) itemView.findViewById(R.id.imgPlaceIcon);

            _lblPlaceTypes = (TextView) itemView.findViewById(R.id.lblPlaceTypes);

            _llPlaceDistance = (LinearLayout) itemView.findViewById(R.id.llPlaceDistance);

            _layoutSelect = (RelativeLayout) itemView.findViewById(layoutSelect);
            _layoutSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _chkSelect.performClick();
                }
            });

            _chkSelect = (CheckBox) itemView.findViewById(chkSelect);
            _chkSelect.setChecked(false);
            _chkSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_chkSelect.isChecked())
                        select(true);
                    else
                        unselect(true);
                }
            });

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
                Picasso.with(_activity.getApplicationContext()).load(imgFile)
                        .placeholder(R.drawable.no_place_image)
                        .fit()
                        .centerCrop()
                        .into(_imgPlace);
            } else {
                // No photo to show:
                Picasso.with(_activity.getApplicationContext()).load(R.drawable.no_place_image).into(_imgPlace);
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

            // Checks if there's a place type icon to show:
            if (place.getIcon() != null) {
                File iconFile = new File(Utils.Image.getInternalStoragePath(_activity), place.getIcon().getName() + ".png");
                if (iconFile.exists())
                    Picasso.with(_activity.getApplicationContext()).load(iconFile).placeholder(R.drawable.no_place_type_icon).into(_imgPlaceIcon);
                else
                    Picasso.with(_activity.getApplicationContext()).load(place.getIcon().getIconUrl()).placeholder(R.drawable.no_place_type_icon).into(_imgPlaceIcon);
            } else {
                Picasso.with(_activity.getApplicationContext()).load(R.drawable.no_place_type_icon).into(_imgPlaceIcon);
            }

            _lblPlaceTypes.setText(PlaceType.listToString(place.getTypes()));

            //region Related to: Selection

            // Checks if currently in the selection mode:
            if (_selectionMode != null && _selectionMode.isInSelectionMode()) {
                // Checks if the item is selected in the selection list:
                if (_selectionMode.getSelection().contains(place))
                    this.select(false); // Selects the item (only UI selection).
                else
                    this.unselect(false); // Item is not selected, then unselects the item (only UI selection).
            } else {
                this.endSelection(); // Not in selection mode mode.
            }

            //endregion
        }

        /**
         * Selects the item.
         *
         * @param notifySelection An indicator indicating whether to notify the selection to the list or not.
         */
        void select(boolean notifySelection) {
            _chkSelect.setChecked(true);
            _layoutSelect.setBackgroundResource(R.drawable.selected_background_style);
            _layoutSelect.setVisibility(View.VISIBLE);

            if (notifySelection)
                _selectionMode.itemSelection(getLayoutPosition(), true);
        }

        /**
         * Removes the selection of the item.
         *
         * @param notifySelection An indicator indicating whether to notify the selection to the list or not.
         */
        void unselect(boolean notifySelection) {
            _chkSelect.setChecked(false);
            _layoutSelect.setBackgroundResource(R.drawable.select_background_style);
            _layoutSelect.setVisibility(View.VISIBLE);

            if (notifySelection)
                _selectionMode.itemSelection(getLayoutPosition(), false);
        }

        /**
         * Ends the selection of the item, whether it's selected or unselected.
         */
        void endSelection() {
            _layoutSelect.setBackgroundResource(R.drawable.select_background_style);
            _layoutSelect.setVisibility(View.GONE);
        }

        //endregion

    }

    //endregion

}
