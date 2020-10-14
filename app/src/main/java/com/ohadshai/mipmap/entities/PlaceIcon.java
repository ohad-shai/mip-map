package com.ohadshai.mipmap.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a place icon entity.
 * Created by Ohad on 12/26/2016.
 */
public class PlaceIcon implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the place icon.
     */
    private int id;

    /**
     * Holds the url of the place icon.
     */
    private String iconUrl;

    /**
     * Holds the name of the place icon.
     */
    private String name;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of a place icon entity.
     */
    public PlaceIcon() {
    }

    /**
     * Initializes a new instance of a place icon entity.
     *
     * @param iconUrl The url of the place icon.
     * @param name    The name of the place icon.
     */
    public PlaceIcon(String iconUrl, String name) {
        if (iconUrl == null)
            throw new NullPointerException("iconUrl");
        if (name == null)
            throw new NullPointerException("name");

        this.iconUrl = iconUrl;
        this.name = name;
    }

    /**
     * Initializes a new instance of a place icon entity.
     *
     * @param id      The id of the place icon.
     * @param iconUrl The url of the place icon.
     * @param name    The name of the place icon.
     */
    public PlaceIcon(int id, String iconUrl, String name) {
        if (id < 1)
            throw new IllegalArgumentException("id");
        if (iconUrl == null)
            throw new NullPointerException("iconUrl");

        this.id = id;
        this.iconUrl = iconUrl;
        this.name = name;
    }

    //endregion

    //region Public API

    /**
     * Gets the id of the place icon.
     *
     * @return Returns the id of the place icon.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the place icon.
     *
     * @param id The id of the place icon to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the url of the place icon.
     *
     * @return Returns the url of the place icon.
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Sets the url of the place icon.
     *
     * @param iconUrl The url of the place icon to set.
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * Gets the name of the place icon.
     *
     * @return Returns the name of the place icon.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the place icon.
     *
     * @param name The name of the place icon to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    //endregion

    //region [Parcelable]

    protected PlaceIcon(Parcel in) {
        id = in.readInt();
        iconUrl = in.readString();
        name = in.readString();
    }

    public static final Creator<PlaceIcon> CREATOR = new Creator<PlaceIcon>() {
        @Override
        public PlaceIcon createFromParcel(Parcel in) {
            return new PlaceIcon(in);
        }

        @Override
        public PlaceIcon[] newArray(int size) {
            return new PlaceIcon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(iconUrl);
        dest.writeString(name);
    }

    //endregion

}
