package com.ohadshai.mipmap.db;

/**
 * Holds all the constants for the database.
 * Created by Ohad on 11/27/2016.
 */
public interface DBConsts {

    //region General Constants

    /**
     * Holds a constant for the database name.
     */
    public final static String DB_NAME = "MipMap_DB.db";

    /**
     * Holds a constant for the database version.
     */
    public final static int DB_VERSION = 1;

    //endregion

    /**
     * Holds all the constants for the "Places" table.
     */
    public interface Table_Places {

        /**
         * Holds a constant for the table name "Places".
         */
        public final static String TABLE_NAME = "[Places]";

        /**
         * Holds a constant for the [Place ID] column name, in the "Places" table.
         */
        public final static String COLUMN_id = "[_id]";

        /**
         * Holds a constant for the [Place Google Place ID] column name, in the "Places" table.
         */
        public final static String COLUMN_GooglePlaceId = "[GooglePlaceId]";

        /**
         * Holds a constant for the [Place Name] column name, in the "Places" table.
         */
        public final static String COLUMN_Name = "[Name]";

        /**
         * Holds a constant for the [Place Address] column name, in the "Places" table.
         */
        public final static String COLUMN_Address = "[Address]";

        /**
         * Holds a constant for the [Place Vicinity] column name, in the "Places" table.
         */
        public final static String COLUMN_Vicinity = "[Vicinity]";

        /**
         * Holds a constant for the [Place Rating] column name, in the "Places" table.
         */
        public final static String COLUMN_Rating = "[Rating]";

        /**
         * Holds a constant for the [Place Latitude] column name, in the "Places" table.
         */
        public final static String COLUMN_Latitude = "[Latitude]";

        /**
         * Holds a constant for the [Place Longitude] column name, in the "Places" table.
         */
        public final static String COLUMN_Longitude = "[Longitude]";

        /**
         * Holds a constant for the [Place Icon Id [FK]] column name, in the "Places" table.
         */
        public final static String COLUMN_PlaceIconId = "[PlaceIconId]";

        /**
         * Holds a constant for the [Photo Reference] column name, in the "Places" table.
         */
        public final static String COLUMN_PhotoReference = "[PhotoReference]";

        /**
         * Holds a constant for the [Place Is Is History] column name, in the "Places" table.
         */
        public final static String COLUMN_IsInHistory = "[IsInHistory]";

        /**
         * Holds a constant for the [Place Is Favorite] column name, in the "Places" table.
         */
        public final static String COLUMN_IsFavorite = "[IsFavorite]";

        /**
         * Holds a constant for the [Place Create Date] column name, in the "Places" table.
         */
        public final static String COLUMN_CreateDate = "[CreateDate]";

    }

    /**
     * Holds all the constants for the "Places Vs Place Types" relationship table.
     */
    public interface Table_PlacesVsPlaceTypes {

        /**
         * Holds a constant for the table name "Places Vs Place Types".
         */
        public final static String TABLE_NAME = "[PlacesVsPlaceTypes]";

        /**
         * Holds a constant for the [Place ID] column name, in the "Places Vs Place Types" table.
         */
        public final static String COLUMN_PlaceId = "[PlaceId]";

        /**
         * Holds a constant for the [Place Type Name] column name, in the "Places Vs Place Types" table.
         */
        public final static String COLUMN_Name = "[Name]";

        /**
         * Holds a constant for the [Place Type Value] column name, in the "Places Vs Place Types" table.
         */
        public final static String COLUMN_Value = "[Value]";

    }

    /**
     * Holds all the constants for the "Place Icons" table.
     */
    public interface Table_PlaceIcons {

        /**
         * Holds a constant for the table name "Place Icons".
         */
        public final static String TABLE_NAME = "[PlaceIcons]";

        /**
         * Holds a constant for the [Place Icon ID] column name, in the "Place Icons" table.
         */
        public final static String COLUMN_id = "[_id]";

        /**
         * Holds a constant for the [Place Icon Url] column name, in the "Place Icons" table.
         */
        public final static String COLUMN_IconUrl = "[IconUrl]";

        /**
         * Holds a constant for the [Place Icon Name] column name, in the "Place Icons" table.
         */
        public final static String COLUMN_Name = "[Name]";

    }

    /**
     * Holds all the constants for the "Search History" table.
     */
    public interface Table_SearchHistory {

        /**
         * Holds a constant for the table name "Search".
         */
        public final static String TABLE_NAME = "[Search]";

        /**
         * Holds a constant for the [Search ID] column name, in the "Search History" table.
         */
        public final static String COLUMN_id = "[_id]";

        /**
         * Holds a constant for the [Search Text] column name, in the "Search History" table.
         */
        public final static String COLUMN_Text = "[Text]";

        /**
         * Holds a constant for the [Search Date] column name, in the "Search History" table.
         */
        public final static String COLUMN_Date = "[Date]";

    }

}
