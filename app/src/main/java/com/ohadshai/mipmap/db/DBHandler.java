package com.ohadshai.mipmap.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.PlaceIcon;
import com.ohadshai.mipmap.entities.PlaceType;
import com.ohadshai.mipmap.entities.Search;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Represents a handler for database interactions.
 * Created by Ohad on 9/23/2016.
 */
public class DBHandler {

    //region Private Members

    /**
     * Holds the instance of the DBHandler for all the application.
     */
    private static DBHandler _instance;

    /**
     * Holds the helper of the database interactions.
     */
    private DBHelper helper;

    //endregion

    //region (Database Interactions)

    //region Public Interactions

    /**
     * Holds the database interaction with: "Places".
     */
    public DBHandler.Places places;

    /**
     * Holds the database interaction with: "Search".
     */
    public DBHandler.SearchHistory searchHistory;

    //endregion

    //region Private Interactions

    /**
     * Holds the database interaction with: "PlaceIcons".
     */
    private DBHandler.PlaceIcons placeIcons;

    /**
     * Holds the database interaction with: "PlaceTypes".
     */
    private DBHandler.PlaceTypes placeTypes;

    //endregion

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a handler for database interactions.
     *
     * @param context The context of the handler owner.
     */
    private DBHandler(Context context) {
        helper = new DBHelper(context, DBConsts.DB_NAME, null, DBConsts.DB_VERSION);

        // Initializes the database interaction objects:
        this.places = new Places();
        this.searchHistory = new SearchHistory();
        this.placeIcons = new PlaceIcons();
        this.placeTypes = new PlaceTypes();
    }

    //endregion

    //region Public Static API

    /**
     * Gets the DBHandler instance of the application, or creates a new instance if null.
     *
     * @param context The context of the DBHandler owner.
     * @return Returns the DBHandler instance of the application.
     */
    public static DBHandler getInstance(Context context) {
        if (_instance == null)
            _instance = new DBHandler(context.getApplicationContext());

        return _instance;
    }

    //endregion

    //region Public API

    /**
     * Initializes the database.
     */
    public void initialize() {
        helper.getReadableDatabase();
    }

    //endregion

    //region Inner Classes

    /**
     * Represents the database interaction with: "Places".
     */
    public class Places {

        /**
         * Gets a place by the id.
         *
         * @param placeId The id of the place to get.
         * @return Returns a place object if found, otherwise null.
         */
        public Place getById(int placeId) {
            if (placeId < 1)
                throw new IllegalArgumentException("placeId");

            Place place = null;
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, DBConsts.Table_Places.COLUMN_id + "=" + placeId, null, null, null, null);

            if (cursor.moveToFirst())
                place = this.createPlaceFromCursor(cursor);

            return place;
        }

        /**
         * Gets a place by the google id.
         *
         * @param googlePlaceId The google id of the place to get.
         * @return Returns a place object if found, otherwise null.
         */
        public Place getByGoogleId(String googlePlaceId) {
            if (googlePlaceId == null || googlePlaceId.trim().equals(""))
                throw new IllegalArgumentException("googlePlaceId");

            Place place = null;
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, DBConsts.Table_Places.COLUMN_GooglePlaceId + "=?", new String[]{googlePlaceId}, null, null, null);

            if (cursor.moveToFirst())
                place = this.createPlaceFromCursor(cursor);

            return place;
        }

        /**
         * Gets all the places.
         *
         * @return Returns a list of all the places.
         */
        public ArrayList<Place> getAll() {
            ArrayList<Place> list = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, null, null, null, null, null);

            while (cursor.moveToNext())
                list.add(createPlaceFromCursor(cursor));

            return list;
        }

        //region Last Search Places Related

        /**
         * Adds a list of places from the last search, in the database.
         *
         * @param places The list of places from the last search to add.
         */
        public void addLastSearch(ArrayList<Place> places) {
            if (places == null)
                throw new NullPointerException("places");

            ContentValues values;
            SQLiteDatabase db = helper.getWritableDatabase();

            db.beginTransaction();
            for (Place place : places) {

                //region Gets / Inserts the place icon...

                // Gets the icon if exists in the database:
                int iconId = -1;
                Cursor cursor = db.query(DBConsts.Table_PlaceIcons.TABLE_NAME, new String[]{DBConsts.Table_PlaceIcons.COLUMN_id}, DBConsts.Table_PlaceIcons.COLUMN_IconUrl + "=?", new String[]{place.getIcon().getIconUrl()}, null, null, null);
                if (cursor.moveToFirst())
                    iconId = cursor.getInt(0);
                cursor.close();

                // Checks if no icon found in the database, inserts a new row:
                if (iconId < 1) {
                    values = new ContentValues();
                    values.put(DBConsts.Table_PlaceIcons.COLUMN_IconUrl, place.getIcon().getIconUrl());
                    values.put(DBConsts.Table_PlaceIcons.COLUMN_Name, place.getIcon().getName());
                    iconId = (int) db.insert(DBConsts.Table_PlaceIcons.TABLE_NAME, null, values);
                }

                //endregion

                //region Inserts the place...

                values = new ContentValues();
                values.put(DBConsts.Table_Places.COLUMN_GooglePlaceId, place.getGooglePlaceId());
                values.put(DBConsts.Table_Places.COLUMN_Name, place.getName());
                values.put(DBConsts.Table_Places.COLUMN_Address, place.getAddress());
                values.put(DBConsts.Table_Places.COLUMN_Vicinity, place.getVicinity());
                values.put(DBConsts.Table_Places.COLUMN_Rating, place.getRating());
                values.put(DBConsts.Table_Places.COLUMN_Latitude, place.getLocation().latitude);
                values.put(DBConsts.Table_Places.COLUMN_Longitude, place.getLocation().longitude);
                values.put(DBConsts.Table_Places.COLUMN_PlaceIconId, iconId);
                values.put(DBConsts.Table_Places.COLUMN_PhotoReference, place.getPhotoReference());
                values.put(DBConsts.Table_Places.COLUMN_IsInHistory, true);
                values.put(DBConsts.Table_Places.COLUMN_IsFavorite, isFavorite(place.getGooglePlaceId()));
                values.put(DBConsts.Table_Places.COLUMN_CreateDate, Calendar.getInstance().getTimeInMillis());
                int placeId = (int) db.insert(DBConsts.Table_Places.TABLE_NAME, null, values);

                //endregion

                //region Inserts the place types...

                for (PlaceType type : place.getTypes()) {
                    values = new ContentValues();
                    values.put(DBConsts.Table_PlacesVsPlaceTypes.COLUMN_PlaceId, placeId);
                    values.put(DBConsts.Table_PlacesVsPlaceTypes.COLUMN_Name, type.getName());
                    values.put(DBConsts.Table_PlacesVsPlaceTypes.COLUMN_Value, type.getValue());
                    db.insert(DBConsts.Table_PlacesVsPlaceTypes.TABLE_NAME, null, values);
                }

                //endregion
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Removes all the places of the last search from the database.
         */
        public void removeLastSearchPlaces() {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            // Deletes all the places from the last search, but not in favorites:
            db.delete(DBConsts.Table_Places.TABLE_NAME, DBConsts.Table_Places.COLUMN_IsInHistory + "=1 AND " + DBConsts.Table_Places.COLUMN_IsFavorite + "=0", null);

            // Updates all the places left (favorites), to be removed from the last search places, if there are any:
            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsInHistory, false);
            db.update(DBConsts.Table_Places.TABLE_NAME, values, null, null);

            // Deletes all the places that not in favorites and not in the last search::
            db.delete(DBConsts.Table_Places.TABLE_NAME, DBConsts.Table_Places.COLUMN_IsInHistory + "=0 AND " + DBConsts.Table_Places.COLUMN_IsFavorite + "=0", null);

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Gets all the places from the last search.
         *
         * @return Returns a list of all the places from the last search.
         */
        public ArrayList<Place> getLastSearchPlaces() {
            ArrayList<Place> list = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, DBConsts.Table_Places.COLUMN_IsInHistory + "=1", null, null, null, null);

            while (cursor.moveToNext())
                list.add(createPlaceFromCursor(cursor));

            return list;
        }

        /**
         * Gets all the places from the last search and not in favorites.
         *
         * @return Returns a list of all the places from the last search and not in favorites.
         */
        public ArrayList<Place> getLastSearchPlacesNotFavorite() {
            ArrayList<Place> list = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, DBConsts.Table_Places.COLUMN_IsInHistory + "=1 AND " + DBConsts.Table_Places.COLUMN_IsFavorite + "=0", null, null, null, null);

            while (cursor.moveToNext())
                list.add(createPlaceFromCursor(cursor));

            return list;
        }

        //endregion

        //region Favorite Places Related

        /**
         * Favorites a place by the place id.
         *
         * @param placeId The id of the place to favorite.
         */
        public void favorite(int placeId) {
            if (placeId < 1)
                throw new IllegalStateException("placeId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsFavorite, true);

            db.update(DBConsts.Table_Places.TABLE_NAME, values, DBConsts.Table_Places.COLUMN_id + "=" + placeId, null);
        }

        /**
         * Favorites a place by the google place id.
         *
         * @param googlePlaceId The google id of the place to favorite.
         */
        public void favorite(String googlePlaceId) {
            if (googlePlaceId == null)
                throw new NullPointerException("googlePlaceId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsFavorite, true);

            db.update(DBConsts.Table_Places.TABLE_NAME, values, DBConsts.Table_Places.COLUMN_GooglePlaceId + "=?", new String[]{googlePlaceId});
        }

        /**
         * Unfavorites a place by the place id.
         *
         * @param placeId The id of the place to unfavorite.
         */
        public void unfavorite(int placeId) {
            if (placeId < 1)
                throw new IllegalArgumentException("placeId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsFavorite, false);

            db.update(DBConsts.Table_Places.TABLE_NAME, values, DBConsts.Table_Places.COLUMN_id + "=" + placeId, null);
        }

        /**
         * Unfavorites a place by the google place id.
         *
         * @param googlePlaceId The google id of the place to unfavorites.
         */
        public void unfavorite(String googlePlaceId) {
            if (googlePlaceId == null)
                throw new NullPointerException("googlePlaceId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsFavorite, false);

            db.update(DBConsts.Table_Places.TABLE_NAME, values, DBConsts.Table_Places.COLUMN_GooglePlaceId + "=?", new String[]{googlePlaceId});
        }

        /**
         * Gets all the favorite places list.
         *
         * @return Returns a list of all the favorite places.
         */
        public ArrayList<Place> getAllFavorites() {
            ArrayList<Place> list = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, null, DBConsts.Table_Places.COLUMN_IsFavorite + "=1", null, null, null, DBConsts.Table_Places.COLUMN_CreateDate + " DESC");

            while (cursor.moveToNext())
                list.add(createPlaceFromCursor(cursor));

            return list;
        }

        /**
         * Removes all the favorite places from the database.
         */
        public void removeAllFavorites() {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            // Removes all the favorites that not in the last search:
            db.delete(DBConsts.Table_Places.TABLE_NAME, DBConsts.Table_Places.COLUMN_IsInHistory + "=0 AND " + DBConsts.Table_Places.COLUMN_IsFavorite + "=1", null);

            // Updates all the places left (in history), to be removed from the favorites, if there are any:
            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Places.COLUMN_IsFavorite, false);

            db.update(DBConsts.Table_Places.TABLE_NAME, values, null, null);

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Indicates whether a place is in favorites or not, by it's google place id.
         *
         * @param googlePlaceId The google place id to check if the place is in favorites.
         * @return Returns true if the place is in favorites, otherwise false.
         */
        public boolean isFavorite(String googlePlaceId) {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Places.TABLE_NAME, new String[]{DBConsts.Table_Places.COLUMN_IsFavorite}, DBConsts.Table_Places.COLUMN_GooglePlaceId + "=?", new String[]{googlePlaceId}, null, null, null);

            if (cursor.moveToFirst())
                return cursor.getInt(0) > 0;

            return false;
        }

        //endregion

        //region Private Methods

        /**
         * Creates a place object from the cursor.
         *
         * @param cursor The cursor to get the place values from.
         * @return Returns the place object created from the cursor.
         */
        private Place createPlaceFromCursor(Cursor cursor) {
            int id = cursor.getInt(0);

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(cursor.getInt(12));

            return new Place(id,
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getFloat(5),
                    new LatLng(cursor.getDouble(6), cursor.getDouble(7)),
                    placeIcons.getById(cursor.getInt(8)),
                    cursor.getString(9),
                    date,
                    placeTypes.getAllForPlaceId(id),
                    cursor.getInt(10) > 0,
                    cursor.getInt(11) > 0);
        }

        //endregion

    }

    /**
     * Represents the database interaction with: "Search".
     */
    public class SearchHistory {

        /**
         * Saves a new search to the history in the database.
         *
         * @param text The text of the search to save in the history.
         * @return Returns the id of the created search history in the database.
         */
        public int save(String text) {
            if (text == null || text.trim().equals(""))
                throw new NullPointerException("text");

            SQLiteDatabase db = helper.getWritableDatabase();
            SearchHistory search = new SearchHistory();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_SearchHistory.COLUMN_Text, text);
            values.put(DBConsts.Table_SearchHistory.COLUMN_Date, Calendar.getInstance().getTimeInMillis());

            return (int) db.insert(DBConsts.Table_SearchHistory.TABLE_NAME, null, values);
        }

        /**
         * Deletes a search from the history.
         *
         * @param id The id of the search to remove.
         */
        public void delete(int id) {
            if (id < 1)
                throw new IllegalArgumentException("id");

            SQLiteDatabase db = helper.getWritableDatabase();

            db.delete(DBConsts.Table_SearchHistory.TABLE_NAME, DBConsts.Table_SearchHistory.COLUMN_id + "=" + id, null);
        }

        /**
         * Deletes all the search history from the database.
         */
        public void deleteAll() {
            SQLiteDatabase db = helper.getWritableDatabase();

            db.delete(DBConsts.Table_SearchHistory.TABLE_NAME, null, null);
        }

        /**
         * Gets all the search history from the database.
         *
         * @return Returns a list of search history.
         */
        public ArrayList<Search> getAll() {
            ArrayList<Search> search = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_SearchHistory.TABLE_NAME, null, null, null, null, null, DBConsts.Table_SearchHistory.COLUMN_Date + " DESC");

            while (cursor.moveToNext())
                search.add(this.createSearchFromCursor(cursor));

            return search;
        }

        /**
         * Gets the last search history from the database (3 records).
         *
         * @return Returns a list of the last search history (3 items).
         */
        public ArrayList<Search> getLastSearchHistory() {
            ArrayList<Search> search = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + DBConsts.Table_SearchHistory.TABLE_NAME + " ORDER BY " + DBConsts.Table_SearchHistory.COLUMN_Date + " DESC LIMIT 3", null);

            while (cursor.moveToNext())
                search.add(this.createSearchFromCursor(cursor));

            return search;
        }

        /**
         * Gets the search history by the query text from the database.
         *
         * @param queryText The query text of the search to get the search record from the database.
         * @return Returns the search object from the database.
         */
        public Search getByText(String queryText) {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_SearchHistory.TABLE_NAME, null, DBConsts.Table_SearchHistory.COLUMN_Text + "=?", new String[]{queryText}, null, null, null);

            if (cursor.moveToNext())
                return this.createSearchFromCursor(cursor);
            else
                return null;
        }

        /**
         * Gets the last search from the database (1 record).
         *
         * @return Returns the last search record.
         */
        public Search getLastSearch() {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + DBConsts.Table_SearchHistory.TABLE_NAME + " ORDER BY " + DBConsts.Table_SearchHistory.COLUMN_Date + " DESC LIMIT 1", null);

            if (cursor.moveToNext())
                return this.createSearchFromCursor(cursor);
            else
                return null;
        }

        //region Private Methods

        /**
         * Creates a search object from the cursor.
         *
         * @param cursor The cursor to get the search values from.
         * @return Returns the search object created from the cursor.
         */
        private Search createSearchFromCursor(Cursor cursor) {
            int id = cursor.getInt(0);

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(cursor.getInt(2));

            return new Search(id, cursor.getString(1), date);
        }

        //endregion

    }

    /**
     * Represents the database interaction with: "PlaceIcons".
     */
    private class PlaceIcons {

        /**
         * Gets a place icon by the id.
         *
         * @param iconId The id of the place icon to get.
         * @return Returns a place icon object if found, otherwise null.
         */
        public PlaceIcon getById(int iconId) {
            if (iconId < 1)
                throw new IllegalArgumentException("iconId");

            PlaceIcon icon = null;
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_PlaceIcons.TABLE_NAME, null, DBConsts.Table_PlaceIcons.COLUMN_id + "=" + iconId, null, null, null, null);

            if (cursor.moveToFirst())
                icon = this.createPlaceIconFromCursor(cursor);

            return icon;
        }

        //region Local Methods

        /**
         * Creates a place icon object from the cursor.
         *
         * @param cursor The cursor to get the place icon values from.
         * @return Returns the place icon object created from the cursor.
         */
        PlaceIcon createPlaceIconFromCursor(Cursor cursor) {
            int id = cursor.getInt(0);
            return new PlaceIcon(id, cursor.getString(1), cursor.getString(2));
        }

        //endregion

    }

    /**
     * Represents the database interaction with: "PlaceTypes".
     */
    private class PlaceTypes {

        /**
         * Gets a list of all the place types of a place by the place id.
         *
         * @param placeId The id of the place to get the list of place types.
         * @return Returns a list of all the place types of a place if found, otherwise null.
         */
        public ArrayList<PlaceType> getAllForPlaceId(int placeId) {
            if (placeId < 1)
                throw new IllegalArgumentException("placeId");

            ArrayList<PlaceType> types = new ArrayList<>();
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_PlacesVsPlaceTypes.TABLE_NAME, null, DBConsts.Table_PlacesVsPlaceTypes.COLUMN_PlaceId + "=" + placeId, null, null, null, null);

            while (cursor.moveToNext())
                types.add(this.createPlaceTypeFromCursor(cursor));

            return types;
        }

        //region Local Methods

        /**
         * Creates a place type object from the cursor.
         *
         * @param cursor The cursor to get the place type values from.
         * @return Returns the place type object created from the cursor.
         */
        PlaceType createPlaceTypeFromCursor(Cursor cursor) {
            int id = cursor.getInt(0);
            return new PlaceType(id, cursor.getString(1), cursor.getString(2));
        }

        //endregion

    }

    //endregion

}
