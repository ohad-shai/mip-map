package com.ohadshai.mipmap.ui;

/**
 * Holds all the constants for the UI.
 * Created by Ohad on 10/12/2016.
 */
public interface UIConsts {

    /**
     * Holds a constant for the application package name.
     */
    public static final String APP_PACKAGE_NAME = "com.ohadshai.mipmap";

    /**
     * Holds all the constants for the Fragments.
     */
    public interface Fragments {

        /**
         * Holds a constant for the "Map Container" fragment tag.
         */
        public static final String MAP_CONTAINER_FRAGMENT_TAG = "map_container_frag_tag";

        /**
         * Holds a constant for the "Last Search Places" fragment tag.
         */
        public static final String LAST_SEARCH_PLACES_FRAGMENT_TAG = "last_search_places_frag_tag";

        /**
         * Holds a constant for the "Map" fragment tag.
         */
        public static final String MAP_FRAGMENT_TAG = "map_frag_tag";

        /**
         * Holds a constant for the "Search History" fragment tag.
         */
        public static final String SEARCH_HISTORY_FRAGMENT_TAG = "search_history_frag_tag";

        /**
         * Holds a constant for the "Search Places" fragment tag.
         */
        public static final String SEARCH_PLACES_FRAGMENT_TAG = "search_places_frag_tag";

        /**
         * Holds a constant for the "Favorite Places" fragment tag.
         */
        public static final String FAVORITE_PLACES_FRAGMENT_TAG = "favorite_places_frag_tag";

        /**
         * Holds a constant for the "Search Radius" dialog fragment tag.
         */
        public static final String SEARCH_RADIUS_DIALOG_TAG = "search_radius_dialog_tag";

        /**
         * Holds a constant for the "Search Place Type Select" dialog fragment tag.
         */
        public static final String SEARCH_PLACE_TYPE_SELECT_DIALOG_TAG = "search_place_type_select_dialog_tag";

        /**
         * Holds a constant for the "About" dialog fragment tag.
         */
        public static final String ABOUT_DIALOG_TAG = "about_dialog_tag";

        /**
         * Holds a constant for the "Permission Explanation" dialog fragment tag.
         */
        public static final String PERMISSION_EXPLANATION_DIALOG_TAG = "permission_explanation_dialog_tag";

    }

    /**
     * Holds all the constants for Intent - Data Extras.
     */
    public interface Intent {

        /**
         * Holds a constant for the "IP Location" key name (intended to hold an "IPLocation" object).
         */
        public static final String IP_LOCATION_KEY = "ip_location_key";

        /**
         * Holds a constant for the "Places List" key name (intended to hold a list of "Place" object).
         */
        public static final String PLACES_LIST_KEY = "places_list_key";

        /**
         * Holds a constant for the "Search Information" key name (intended to hold a "SearchInfo" object).
         */
        public static final String SEARCH_INFO_KEY = "search_info_key";

        /**
         * Holds a constant for the "Search History" key name (intended to hold a "Search" object).
         */
        public static final String SEARCH_HISTORY_KEY = "search_history_key";

        /**
         * Holds a constant for the "Place" key name (intended to hold a "Place" object).
         */
        public static final String PLACE_KEY = "place_key";

        /**
         * Holds a constant for the "Place Id" key name (intended to hold an id of a "Place" object).
         */
        public static final String PLACE_ID_KEY = "place_id_key";

        /**
         * Holds a constant for the "Google Place Id" key name (intended to hold a google id of a "Place" object).
         */
        public static final String GOOGLE_PLACE_ID_KEY = "google_place_id_key";

        /**
         * Holds all the constants that indicates a mode (like add, read, update, remove, select, etc...).
         */
        public interface MODE {

            /**
             * Holds a constant for the key name.
             */
            public static final String KEY_NAME = "mode";

            /**
             * Holds a constant for the "Mode" value: "Unspecified".
             */
            public static final int UNSPECIFIED = 0;

            /**
             * Holds a constant for the "Mode" value: "Create".
             */
            public static final int CREATE = 1;

            /**
             * Holds a constant for the "Mode" value: "Read".
             */
            public static final int READ = 2;

            /**
             * Holds a constant for the "Mode" value: "Update".
             */
            public static final int UPDATE = 3;

            /**
             * Holds a constant for the "Mode" value: "Delete".
             */
            public static final int DELETE = 4;

            /**
             * Holds a constant for the "Mode" value: "Select".
             */
            public static final int SELECT = 5;

        }

    }

    /**
     * Holds all the constants for Bundles (saved instance states).
     */
    public interface Bundles {

        /**
         * Holds a constant for a bundle key, which holds a value of a search radius.
         */
        public static final String SEARCH_RADIUS = "search_radius_value";

        /**
         * Holds a constant for a bundle key, which holds an object of a place type.
         */
        public static final String PLACE_TYPE = "place_type_object";

        /**
         * Holds a constant for a bundle key, which holds an array of marked places on the map.
         */
        public static final String MARKED_PLACES_LIST = "marked_places_list";

    }

}
