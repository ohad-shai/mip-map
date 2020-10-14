package com.ohadshai.mipmap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Represents a place entity.
 * Created by Ohad on 11/27/2016.
 */
public class Place implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the place.
     */
    private int id;

    /**
     * Holds the google place id of the place.
     */
    private String googlePlaceId;

    /**
     * Holds the name of the place.
     */
    private String name;

    /**
     * Holds the address of the place.
     */
    private String address;

    /**
     * Holds the vicinity of the place.
     */
    private String vicinity;

    /**
     * Holds the rating of the place.
     */
    private float rating;

    /**
     * Holds the location of the place (latitude & longitude).
     */
    private LatLng location;

    /**
     * Holds the icon of the place.
     */
    private PlaceIcon icon;

    /**
     * Holds the google reference for the photo of the place.
     */
    private String photoReference;

    /**
     * Holds a list of all the types of the place.
     */
    private ArrayList<PlaceType> types;

    /**
     * Holds an indicator indicating whether the place is in history or not.
     */
    private boolean isInHistory;

    /**
     * Holds an indicator indicating whether the place is favorite or not.
     */
    private boolean isFavorite;

    /**
     * Holds the date the place created.
     */
    private Calendar createDate;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of a place entity.
     */
    public Place() {
        types = new ArrayList<>();
    }

    /**
     * Initializes a new instance of a place entity.
     *
     * @param id             The id of the place.
     * @param googlePlaceId  The google place id of the place.
     * @param name           The name of the place.
     * @param address        The address of the place.
     * @param vicinity       The vicinity of the place.
     * @param rating         The rating of the place.
     * @param location       The location of the place (latitude & longitude).
     * @param icon           The icon of the place.
     * @param photoReference The google reference for the photo of the place.
     * @param createDate     The date the place created.
     * @param types          The list of all the types of the place.
     * @param isInHistory    An indicator indicating whether the place is in history or not.
     * @param isFavorite     An indicator indicating whether the place is favorite or not.
     */
    public Place(int id, String googlePlaceId, String name, String address, String vicinity, float rating, LatLng location, PlaceIcon icon, String photoReference, Calendar createDate, ArrayList<PlaceType> types, boolean isInHistory, boolean isFavorite) {
        if (id < 1)
            throw new IllegalArgumentException("id");
        if (googlePlaceId == null || googlePlaceId.trim().equals(""))
            throw new NullPointerException("googlePlaceId");
        if (name == null || name.trim().equals(""))
            throw new NullPointerException("name");

        this.id = id;
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.address = address;
        this.vicinity = vicinity;
        this.rating = rating;
        this.location = location;
        this.icon = icon;
        this.photoReference = photoReference;
        this.isInHistory = isInHistory;
        this.isFavorite = isFavorite;
        this.createDate = createDate;

        if (types == null)
            this.types = new ArrayList<>();
        else
            this.types = types;
    }

    //endregion

    //region Public Static API

    /**
     * Parses a JSON string to a list of places.
     *
     * @param json The JSON string to parse.
     * @return Returns a list of places parsed from the JSON.
     */
    public static ArrayList<Place> parseJsonToList(String json) throws JSONException, IOException {
        ArrayList<Place> places = new ArrayList<>();
        JSONObject jo = new JSONObject(json);

        // Checks if the status value from the service is proper:
        if (!Arrays.asList(GooglePlacesConsts.Response.properStatusValues).contains(jo.getString(GooglePlacesConsts.Response.STATUS_VALUE)))
            throw new IllegalStateException("Improper status value returned from the service response.");

        JSONArray ja = jo.getJSONArray(GooglePlacesConsts.Response.RESULTS_ARRAY);

        for (int i = 0; i < ja.length(); i++) {
            JSONObject placeJSON = ja.getJSONObject(i);
            Place place = new Place();
            place.setGooglePlaceId(placeJSON.getString(GooglePlacesConsts.Response.PLACE_ID_VALUE));
            place.setName(placeJSON.getString(GooglePlacesConsts.Response.NAME_VALUE));
            if (!placeJSON.isNull(GooglePlacesConsts.Response.ADDRESS_VALUE))
                place.setAddress(placeJSON.getString(GooglePlacesConsts.Response.ADDRESS_VALUE));
            if (!placeJSON.isNull(GooglePlacesConsts.Response.VICINITY_VALUE))
                place.setVicinity(placeJSON.getString(GooglePlacesConsts.Response.VICINITY_VALUE));
            if (!placeJSON.isNull(GooglePlacesConsts.Response.RATING_VALUE))
                place.setRating((float) placeJSON.getDouble(GooglePlacesConsts.Response.RATING_VALUE));

            JSONObject locationJSON = placeJSON.getJSONObject(GooglePlacesConsts.Response.GEOMETRY_OBJECT).getJSONObject(GooglePlacesConsts.Response.LOCATION_OBJECT);
            place.setLocation(new LatLng(locationJSON.getDouble(GooglePlacesConsts.Response.LATITUDE_VALUE), locationJSON.getDouble(GooglePlacesConsts.Response.LONGITUDE_VALUE)));

            if (!placeJSON.isNull(GooglePlacesConsts.Response.ICON_VALUE)) {
                String iconUrl = placeJSON.getString(GooglePlacesConsts.Response.ICON_VALUE);
                place.setIcon(new PlaceIcon(iconUrl, Utils.Strings.getFileNameFromUrl(iconUrl)));
            }

            if (!placeJSON.isNull(GooglePlacesConsts.Response.PHOTOS_ARRAY)) {
                JSONArray photosJSON = placeJSON.getJSONArray(GooglePlacesConsts.Response.PHOTOS_ARRAY);
                if (photosJSON.length() > 0)
                    place.setPhotoReference(photosJSON.getJSONObject(0).getString(GooglePlacesConsts.Response.PHOTO_REFERENCE_VALUE));
            }

            place.setTypes(new ArrayList<PlaceType>());
            if (!placeJSON.isNull(GooglePlacesConsts.Response.TYPES_ARRAY)) {
                JSONArray typesJSON = placeJSON.getJSONArray(GooglePlacesConsts.Response.TYPES_ARRAY);
                for (int t = 0; t < typesJSON.length(); t++) {
                    String value = typesJSON.getString(t);
                    // Checks if the place type value is not an excluded one:
                    if (!Arrays.asList(GooglePlacesConsts.Response.excludedPlaceTypes).contains(value))
                        place.getTypes().add(new PlaceType(Utils.Strings.toTitle(value), value));
                }
            }

            place.setCreateDate(Calendar.getInstance());
            places.add(place);
        }

        return places;
    }

    //endregion

    //region Public API

    /**
     * Gets the id of the place.
     *
     * @return Returns the id of the place.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the place.
     *
     * @param id The id of the place to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the google place id of the place.
     *
     * @return Returns the google place id of the place.
     */
    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    /**
     * Sets the google place id of the place.
     *
     * @param googlePlaceId The google place id of the place to set.
     */
    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    /**
     * Gets the name of the place.
     *
     * @return Returns the name of the place.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the place.
     *
     * @param name The name of the place to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the address of the place.
     *
     * @return Returns the address of the place.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the place.
     *
     * @param address The address of the place to set.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the vicinity of the place.
     *
     * @return Returns the vicinity of the place.
     */
    public String getVicinity() {
        return vicinity;
    }

    /**
     * Sets the vicinity of the place.
     *
     * @param vicinity The vicinity of the place to set.
     */
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    /**
     * Gets the rating of the place.
     *
     * @return Returns the rating of the place.
     */
    public float getRating() {
        return rating;
    }

    /**
     * Sets the rating of the place.
     *
     * @param rating The rating of the place to set.
     */
    public void setRating(float rating) {
        this.rating = rating;
    }

    /**
     * Gets the location of the place (latitude & longitude).
     *
     * @return Returns the location of the place (latitude & longitude).
     */
    public LatLng getLocation() {
        return location;
    }

    /**
     * Sets the location of the place (latitude & longitude).
     *
     * @param location The location of the place (latitude & longitude) to set.
     */
    public void setLocation(LatLng location) {
        this.location = location;
    }

    /**
     * Gets the icon of the place.
     *
     * @return Returns the icon of the place.
     */
    public PlaceIcon getIcon() {
        return icon;
    }

    /**
     * Sets the icon of the place.
     *
     * @param icon The icon of the place to set.
     */
    public void setIcon(PlaceIcon icon) {
        this.icon = icon;
    }

    /**
     * Gets the google reference for the photo of the place.
     *
     * @return Returns the google reference for the photo of the place.
     */
    public String getPhotoReference() {
        return photoReference;
    }

    /**
     * Sets the google reference for the photo of the place.
     *
     * @param photoReference The google reference for the photo of the place to set.
     */
    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    /**
     * Gets a list of all the types of the place.
     *
     * @return Returns a list of all the types of the place.
     */
    public ArrayList<PlaceType> getTypes() {
        return types;
    }

    /**
     * Sets a list of all the types of the place.
     *
     * @param types The list of all the types of the place to set.
     */
    public void setTypes(ArrayList<PlaceType> types) {
        this.types = types;
    }

    /**
     * Indicates whether the place is in search history or not.
     *
     * @return Returns tru if the place is in search history, otherwise false.
     */
    public boolean isInHistory() {
        return isInHistory;
    }

    /**
     * Sets an indicator indicating whether the place is in the search history or not.
     *
     * @param inHistory Returns true if the place is in the search history, otherwise false.
     */
    public void setInHistory(boolean inHistory) {
        isInHistory = inHistory;
    }

    /**
     * Indicates whether the place is favorite or not.
     *
     * @return Returns true if the place is favorite, otherwise false.
     */
    public boolean isFavorite() {
        return isFavorite;
    }

    /**
     * Sets an indicator indicating whether the place is favorite or not.
     *
     * @param favorite An indicator indicating whether the place is favorite or not.
     */
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * Gets the date the place created.
     *
     * @return Returns the date the place created.
     */
    public Calendar getCreateDate() {
        return createDate;
    }

    /**
     * Sets the date the place created.
     *
     * @param createDate The date the place created to set.
     */
    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    //endregion

    //region [Parcelable]

    protected Place(Parcel in) {
        id = in.readInt();
        googlePlaceId = in.readString();
        name = in.readString();
        address = in.readString();
        vicinity = in.readString();
        rating = in.readFloat();
        location = in.readParcelable(LatLng.class.getClassLoader());
        icon = in.readParcelable(PlaceIcon.class.getClassLoader());
        photoReference = in.readString();
        types = in.createTypedArrayList(PlaceType.CREATOR);
        isInHistory = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        createDate = Calendar.getInstance();
        createDate.setTimeInMillis(in.readLong());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(googlePlaceId);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(vicinity);
        dest.writeFloat(rating);
        dest.writeParcelable(location, flags);
        dest.writeParcelable(icon, flags);
        dest.writeString(photoReference);
        dest.writeTypedList(types);
        dest.writeByte((byte) (isInHistory ? 1 : 0));
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeLong(createDate.getTimeInMillis());
    }

    //endregion

}
