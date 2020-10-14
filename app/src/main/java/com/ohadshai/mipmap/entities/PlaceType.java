package com.ohadshai.mipmap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Represents a place type entity.
 * Created by Ohad on 11/27/2016.
 */
public class PlaceType implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the place type.
     */
    private int id;

    /**
     * Holds the name of the place type.
     */
    private String name;

    /**
     * Holds the value of the place type.
     */
    private String value;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of a place type entity.
     */
    public PlaceType() {
    }

    /**
     * Initializes a new instance of a place type entity.
     *
     * @param name The name of the place type.
     */
    public PlaceType(String name) {
        if (name == null || name.trim().equals(""))
            throw new NullPointerException("name");

        this.name = name;
    }

    /**
     * Initializes a new instance of a place type entity.
     *
     * @param name  The name of the place type.
     * @param value The value of the place type.
     */
    public PlaceType(String name, String value) {
        if (name == null || name.trim().equals(""))
            throw new NullPointerException("name");
        if (value == null || value.trim().equals(""))
            throw new NullPointerException("value");

        this.name = name;
        this.value = value;
    }

    /**
     * Initializes a new instance of a place type entity.
     *
     * @param id    The id of the place type.
     * @param name  The name of the place type.
     * @param value The value of the place type.
     */
    public PlaceType(int id, String name, String value) {
        if (id < 1)
            throw new IllegalArgumentException("id");
        if (name == null || name.trim().equals(""))
            throw new NullPointerException("name");
        if (value == null || value.trim().equals(""))
            throw new NullPointerException("value");

        this.id = id;
        this.name = name;
        this.value = value;
    }

    //endregion

    //region Public Static API

    /**
     * Creates a string representation of a list of place types.
     *
     * @param placeTypes The list of place types to return it's string representation.
     * @return Returns a string representation of a list of place types.
     */
    public static String listToString(ArrayList<PlaceType> placeTypes) {
        if (placeTypes == null)
            throw new NullPointerException("placeTypes");

        if (placeTypes.size() < 1)
            return "";

        StringBuilder sb = new StringBuilder();

        for (PlaceType type : placeTypes)
            sb.append(type.getName()).append(" | ");

        return sb.substring(0, sb.length() - 3);
    }

    //endregion

    //region Public API

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PlaceType)) {
            return false;
        } else {
            PlaceType compareTo = (PlaceType) obj;
            return this.id == compareTo.id && this.name.equals(compareTo.name) && this.value.equals(compareTo.value);
        }
    }

    /**
     * Gets the id of the place type.
     *
     * @return Returns the id of the place type.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the place type.
     *
     * @param id The id of the place type to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the place type.
     *
     * @return Returns the name of the place type.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the place type.
     *
     * @param name The name of the place type to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the place type.
     *
     * @return Returns the value of the place type.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the place type.
     *
     * @param value The value of the place type to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    //endregion

    //region [Parcelable]

    protected PlaceType(Parcel in) {
        id = in.readInt();
        name = in.readString();
        value = in.readString();
    }

    public static final Creator<PlaceType> CREATOR = new Creator<PlaceType>() {
        @Override
        public PlaceType createFromParcel(Parcel in) {
            return new PlaceType(in);
        }

        @Override
        public PlaceType[] newArray(int size) {
            return new PlaceType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(value);
    }

    //endregion

}
