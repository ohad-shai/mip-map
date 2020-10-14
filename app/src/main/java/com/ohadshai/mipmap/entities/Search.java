package com.ohadshai.mipmap.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Represents a search entity.
 * Created by Ohad on 11/27/2016.
 */
public class Search implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the search.
     */
    private int id;

    /**
     * Holds the text of the search.
     */
    private String text;

    /**
     * Holds the date of the search.
     */
    private Calendar date;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of a search entity.
     */
    public Search() {
    }

    /**
     * Initializes a new instance of a search entity.
     *
     * @param id   The id of the search.
     * @param text The text of the search.
     * @param date The date of the search.
     */
    public Search(int id, String text, Calendar date) {
        if (id < 1)
            throw new IllegalArgumentException("id");
        if (text == null || text.trim().equals(""))
            throw new NullPointerException("text");

        this.id = id;
        this.text = text;
        this.date = date;
    }

    //endregion

    //region Public API

    /**
     * Gets the id of the search.
     *
     * @return Returns the id of the search.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the search.
     *
     * @param id The id of the search to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the text of the search.
     *
     * @return Returns the text of the search.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the search.
     *
     * @param text The text of the search to set.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the date of the search.
     *
     * @return Returns the date of the search.
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Sets the date of the search.
     *
     * @param date The date of the search to set.
     */
    public void setDate(Calendar date) {
        this.date = date;
    }

    //endregion

    //region [Parcelable]

    protected Search(Parcel in) {
        id = in.readInt();
        text = in.readString();
    }

    public static final Creator<Search> CREATOR = new Creator<Search>() {
        @Override
        public Search createFromParcel(Parcel in) {
            return new Search(in);
        }

        @Override
        public Search[] newArray(int size) {
            return new Search[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
    }

    //endregion

}
