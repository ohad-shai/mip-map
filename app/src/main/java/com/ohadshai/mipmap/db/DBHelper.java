package com.ohadshai.mipmap.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Represents a helper for database interactions.
 * Created by Ohad on 9/23/2016.
 */
class DBHelper extends SQLiteOpenHelper {

    //region Private Members

    /**
     * Holds the context of the DBHelper.
     */
    private Context context;

    //endregion

    /**
     * C'tor
     * Initializes a new instance of a helper for database interactions.
     *
     * @param context The context of the DBHelper owner.
     * @param name    The name of the database.
     * @param factory
     * @param version The version of the database.
     */
    DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String tableSearchHistory = " CREATE TABLE " + DBConsts.Table_SearchHistory.TABLE_NAME + " (" +
                DBConsts.Table_SearchHistory.COLUMN_id + " INTEGER PRIMARY KEY, " +
                DBConsts.Table_SearchHistory.COLUMN_Text + " TEXT NOT NULL, " +
                DBConsts.Table_SearchHistory.COLUMN_Date + " INTEGER NOT NULL, " +
                " UNIQUE (" + DBConsts.Table_SearchHistory.COLUMN_Text + ") ON CONFLICT REPLACE " +
                "); ";

        final String tablePlaceIcons = " CREATE TABLE " + DBConsts.Table_PlaceIcons.TABLE_NAME + " (" +
                DBConsts.Table_PlaceIcons.COLUMN_id + " INTEGER PRIMARY KEY, " +
                DBConsts.Table_PlaceIcons.COLUMN_IconUrl + " TEXT NOT NULL, " +
                DBConsts.Table_PlaceIcons.COLUMN_Name + " TEXT NOT NULL, " +
                " UNIQUE (" + DBConsts.Table_PlaceIcons.COLUMN_IconUrl + ") ON CONFLICT REPLACE " +
                "); ";

        // Needs FK from: [PlaceIcons]:
        final String tablePlaces = " CREATE TABLE " + DBConsts.Table_Places.TABLE_NAME + " (" +
                DBConsts.Table_Places.COLUMN_id + " INTEGER PRIMARY KEY, " +
                DBConsts.Table_Places.COLUMN_GooglePlaceId + " TEXT NOT NULL, " +
                DBConsts.Table_Places.COLUMN_Name + " TEXT NOT NULL, " +
                DBConsts.Table_Places.COLUMN_Address + " TEXT, " +
                DBConsts.Table_Places.COLUMN_Vicinity + " TEXT, " +
                DBConsts.Table_Places.COLUMN_Rating + " REAL, " +
                DBConsts.Table_Places.COLUMN_Latitude + " REAL, " +
                DBConsts.Table_Places.COLUMN_Longitude + " REAL, " +
                DBConsts.Table_Places.COLUMN_PlaceIconId + " INTEGER, " +
                DBConsts.Table_Places.COLUMN_PhotoReference + " TEXT, " +
                DBConsts.Table_Places.COLUMN_IsInHistory + " INTEGER DEFAULT 0, " +
                DBConsts.Table_Places.COLUMN_IsFavorite + " INTEGER DEFAULT 0, " +
                DBConsts.Table_Places.COLUMN_CreateDate + " INTEGER NOT NULL, " +
                " UNIQUE (" + DBConsts.Table_Places.COLUMN_GooglePlaceId + ") ON CONFLICT REPLACE, " +
                " FOREIGN KEY (" + DBConsts.Table_Places.COLUMN_PlaceIconId + ") REFERENCES " + DBConsts.Table_PlaceIcons.TABLE_NAME + " (" + DBConsts.Table_PlaceIcons.COLUMN_id + ") ON UPDATE CASCADE " +
                "); ";

        // Needs FK from: [Places]:
        final String tablePlacesVsPlaceTypes = " CREATE TABLE " + DBConsts.Table_PlacesVsPlaceTypes.TABLE_NAME + " (" +
                DBConsts.Table_PlacesVsPlaceTypes.COLUMN_PlaceId + " INTEGER, " +
                DBConsts.Table_PlacesVsPlaceTypes.COLUMN_Name + " TEXT NOT NULL, " +
                DBConsts.Table_PlacesVsPlaceTypes.COLUMN_Value + " TEXT NOT NULL, " +
                " UNIQUE (" + DBConsts.Table_PlacesVsPlaceTypes.COLUMN_PlaceId + "," + DBConsts.Table_PlacesVsPlaceTypes.COLUMN_Value + ") ON CONFLICT REPLACE, " +
                " FOREIGN KEY (" + DBConsts.Table_PlacesVsPlaceTypes.COLUMN_PlaceId + ") REFERENCES " + DBConsts.Table_Places.TABLE_NAME + " (" + DBConsts.Table_Places.COLUMN_id + ") ON DELETE CASCADE " +
                "); ";

        // Executes the queries:
        sqLiteDatabase.execSQL(tableSearchHistory);
        sqLiteDatabase.execSQL(tablePlaceIcons);
        sqLiteDatabase.execSQL(tablePlaces);
        sqLiteDatabase.execSQL(tablePlacesVsPlaceTypes);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enables foreign key constraints.
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
