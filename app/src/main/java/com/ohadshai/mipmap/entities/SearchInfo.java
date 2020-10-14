package com.ohadshai.mipmap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.utils.Utils;

/**
 * Represents a search information entity, which holds all the search fields.
 * Created by Ohad on 1/10/2017.
 */
public class SearchInfo implements Parcelable {

    //region Private Members

    /**
     * Holds the text for the search.
     */
    private String _text;

    /**
     * Holds the place type for the search.
     */
    private PlaceType _placeType;

    /**
     * Holds an indicator indicating whether the search is a nearby search or not.
     */
    private boolean _isNearby;

    /**
     * Holds the location for the search.
     */
    private LatLng _location;

    /**
     * Holds the radius for the search (in meters).
     */
    private int _radius;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a search information entity, which holds all the search fields.
     *
     * @param text      The text for the search.
     * @param placeType The place type for the search.
     * @param isNearby  An indicator indicating whether the search is a nearby search or not.
     * @param location  The location for the search.
     * @param radius    The radius for the search (in meters).
     */
    public SearchInfo(String text, PlaceType placeType, boolean isNearby, LatLng location, int radius) {
        this._text = text;
        this._placeType = placeType;
        this._isNearby = isNearby;
        this._location = location;
        this._radius = radius;
    }

    //endregion

    //region Public Static API

    /**
     * Creates a new SearchInfo object with default values.
     *
     * @param unitOfLength The unit of length to initial the radius default value.
     * @return Returns a new SearchInfo object with default values.
     */
    public static SearchInfo createDefault(int unitOfLength) {
        if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
            return new SearchInfo(null, null, true, null, 1000);
        if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
            return new SearchInfo(null, null, true, null, 1610);
        else
            throw new IllegalArgumentException("unitOfLength");
    }

    /**
     * Copies a search info object.
     *
     * @param searchInfo The search info object to copy.
     * @return Returns a new copied search info object.
     */
    public static SearchInfo copy(SearchInfo searchInfo) {
        return new SearchInfo(searchInfo._text,
                (searchInfo._placeType == null ? null : new PlaceType(searchInfo._placeType.getName(), searchInfo._placeType.getValue())),
                searchInfo._isNearby,
                (searchInfo._location == null ? null : new LatLng(searchInfo._location.latitude, searchInfo._location.longitude)),
                searchInfo._radius);
    }

    //endregion

    //region Public API

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SearchInfo)) {
            return false;
        } else {
            SearchInfo compareTo = (SearchInfo) obj;

            return (this._text.equals(compareTo.getText())) &&
                    ((this._placeType == null && compareTo.getPlaceType() == null) || (this._placeType != null && this._placeType.equals(compareTo.getPlaceType()))) &&
                    (this._isNearby == compareTo.isNearby()) &&
                    (this._location.equals(compareTo.getLocation())) &&
                    (this._radius == compareTo.getRadius());
        }
    }

    /**
     * Gets the text for the search.
     *
     * @return Returns the text for the search.
     */
    public String getText() {
        return _text;
    }

    /**
     * Sets the text for the search.
     *
     * @param text The text for the search to set.
     */
    public void setText(String text) {
        this._text = text;
    }

    /**
     * Gets the place type for the search.
     *
     * @return Returns the place type for the search.
     */
    public PlaceType getPlaceType() {
        return _placeType;
    }

    /**
     * Sets the place type for the search.
     *
     * @param placeType The place type for the search to set.
     */
    public void setPlaceType(PlaceType placeType) {
        this._placeType = placeType;
    }

    /**
     * Indicates whether the search is a nearby search or not.
     *
     * @return Returns true if the search is a nearby search, otherwise false.
     */
    public boolean isNearby() {
        return _isNearby;
    }

    /**
     * Sets an indicator indicating whether the search is a nearby search or not.
     *
     * @param isNearby An indicator indicating whether the search is a nearby search or not.
     */
    public void setIsNearby(boolean isNearby) {
        this._isNearby = isNearby;
    }

    /**
     * Gets the location for the search.
     *
     * @return Returns the location for the search.
     */
    public LatLng getLocation() {
        return _location;
    }

    /**
     * Sets the location for the search.
     *
     * @param location The location for the search to set.
     */
    public void setLocation(LatLng location) {
        this._location = location;
    }

    /**
     * Gets the radius for the search (in meters).
     *
     * @return Returns the radius for the search (in meters).
     */
    public int getRadius() {
        return _radius;
    }

    /**
     * Sets the radius for the search (in meters).
     *
     * @param radius The radius for the search to set (in meters).
     */
    public void setRadius(int radius) {
        this._radius = radius;
    }

    //endregion

    //region [Parcelable]

    protected SearchInfo(Parcel in) {
        _text = in.readString();
        _placeType = in.readParcelable(PlaceType.class.getClassLoader());
        _isNearby = in.readByte() != 0;
        _location = in.readParcelable(LatLng.class.getClassLoader());
        _radius = in.readInt();
    }

    public static final Creator<SearchInfo> CREATOR = new Creator<SearchInfo>() {
        @Override
        public SearchInfo createFromParcel(Parcel in) {
            return new SearchInfo(in);
        }

        @Override
        public SearchInfo[] newArray(int size) {
            return new SearchInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_text);
        dest.writeParcelable(_placeType, flags);
        dest.writeByte((byte) (_isNearby ? 1 : 0));
        dest.writeParcelable(_location, flags);
        dest.writeInt(_radius);
    }

    //endregion

}
