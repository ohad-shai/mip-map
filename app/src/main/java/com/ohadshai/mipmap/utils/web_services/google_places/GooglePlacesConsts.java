package com.ohadshai.mipmap.utils.web_services.google_places;

/**
 * Holds all the constants for the "Google Places API" web service.
 * Created by Ohad on 12/25/2016.
 */
public interface GooglePlacesConsts {

    /**
     * Holds all the constants for the service urls.
     */
    public interface Urls {

        /**
         * Holds a constant for a service url for: getting a photo by a google reference.
         */
        public static final String GET_PHOTO_BY_REFERENCE = "https://maps.googleapis.com/maps/api/place/photo?key=" + GooglePlacesCredentials.AUTH_KEY + "&maxwidth=500&photoreference=";

        /**
         * Holds a constant for a service url for: searching places by a text.
         */
        public static final String SEARCH_PLACES_BY_TEXT = "https://maps.googleapis.com/maps/api/place/textsearch/json?key=" + GooglePlacesCredentials.AUTH_KEY + "&query=";

        /**
         * Holds a constant for a service url for: searching nearby places by a location.
         */
        public static final String SEARCH_NEARBY_LOCATION = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + GooglePlacesCredentials.AUTH_KEY + "&location=";

        /**
         * Holds a constant for a url for: displaying a place in google maps web display.
         */
        public static final String GOOGLE_PLACE_WEB_DISPLAY = "https://www.google.com/maps/place/";

    }

    /**
     * Holds all the constants for url query parameters.
     */
    public interface Params {

        /**
         * Holds a constant for a query append parameter: "Location" (location coordinates).
         */
        public static final String APPEND_LOCATION = "&location=";

        /**
         * Holds a constant for a query append parameter: "Radius" (radius of a location, in meters, like: 500).
         */
        public static final String APPEND_RADIUS = "&radius=";

        /**
         * Holds a constant for a query append parameter: "Type" (type of a place, like: restaurant).
         */
        public static final String APPEND_TYPE = "&type=";

        /**
         * Holds a constant for a query append parameter: "Keyword" (keyword of a place, like name, address, type...).
         */
        public static final String APPEND_KEYWORD = "&keyword=";

        /**
         * Holds a constant for a query append parameter: "Language" (language code to search and return the results).
         */
        public static final String APPEND_LANGUAGE = "&language=";

    }

    /**
     * Holds all the constants for the service response.
     */
    public interface Response {

        /**
         * Holds a list of excluded place types.
         */
        public static String[] excludedPlaceTypes = {"establishment", "point_of_interest"};

        /**
         * Holds a list of proper status values returned from a response.
         */
        public static String[] properStatusValues = {"OK", "ZERO_RESULTS"};

        /**
         * Holds a constant for a response JSON property: "status" (holds a String value).
         */
        public static final String STATUS_VALUE = "status";

        /**
         * Holds a constant for a response JSON property: "results" (holds a JSON array).
         */
        public static final String RESULTS_ARRAY = "results";

        /**
         * Holds a constant for a response JSON property: "place_id" (holds a String value).
         */
        public static final String PLACE_ID_VALUE = "place_id";

        /**
         * Holds a constant for a response JSON property: "name" (holds a String value).
         */
        public static final String NAME_VALUE = "name";

        /**
         * Holds a constant for a response JSON property: "formatted_address" (holds a String value).
         */
        public static final String ADDRESS_VALUE = "formatted_address";

        /**
         * Holds a constant for a response JSON property: "vicinity" (holds a String value).
         */
        public static final String VICINITY_VALUE = "vicinity";

        /**
         * Holds a constant for a response JSON property: "rating" (holds a Double value).
         */
        public static final String RATING_VALUE = "rating";

        /**
         * Holds a constant for a response JSON property: "geometry" (holds a JSON object).
         */
        public static final String GEOMETRY_OBJECT = "geometry";

        /**
         * Holds a constant for a response JSON property: "location" (holds a JSON object).
         */
        public static final String LOCATION_OBJECT = "location";

        /**
         * Holds a constant for a response JSON property: "lat" (holds a Double value).
         */
        public static final String LATITUDE_VALUE = "lat";

        /**
         * Holds a constant for a response JSON property: "lng" (holds a Double value).
         */
        public static final String LONGITUDE_VALUE = "lng";

        /**
         * Holds a constant for a response JSON property: "icon" (holds a String value).
         */
        public static final String ICON_VALUE = "icon";

        /**
         * Holds a constant for a response JSON property: "types" (holds a JSON array).
         */
        public static final String TYPES_ARRAY = "types";

        /**
         * Holds a constant for a response JSON property: "photos" (holds a JSON array).
         */
        public static final String PHOTOS_ARRAY = "photos";

        /**
         * Holds a constant for a response JSON property: "photo_reference" (holds a String value).
         */
        public static final String PHOTO_REFERENCE_VALUE = "photo_reference";

    }

}
