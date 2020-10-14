package com.ohadshai.mipmap.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ohadshai.mipmap.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents utilities and general helpers.
 * Created by Ohad on 12/14/2016.
 */
public class Utils {

    /**
     * Represents utilities and helpers for the UI.
     */
    public static final class UI {

        /**
         * Displays a dialog that user has no internet connection.
         *
         * @param activity The activity owner of the dialog.
         */
        public static void showNoConnectionDialog(final Activity activity) {
            if (activity == null)
                throw new NullPointerException("activity");

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(true);
            builder.setTitle(R.string.dialog_network_error_title);
            builder.setMessage(R.string.dialog_network_error_message);
            builder.setPositiveButton(R.string.general_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            });
            builder.setNegativeButton(R.string.general_btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        /**
         * Displays a dialog to notify the user to enable network connection in order to display a map (load the map resources).
         *
         * @param context The context owner of the dialog.
         */
        public static void showNoConnectionDialogForMap(final Context context) {
            if (context == null)
                throw new NullPointerException("context");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle(R.string.dialog_network_error_title);
            builder.setMessage(R.string.dialog_network_error_message_for_map);
            builder.setPositiveButton(R.string.general_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            });
            builder.setNegativeButton(R.string.general_btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        /**
         * Displays a dialog to inform the service is temporarily unavailable.
         *
         * @param activity The activity owner of the dialog.
         */
        public static void showServiceErrorDialog(final Activity activity) {
            if (activity == null)
                throw new NullPointerException("activity");

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(true);
            builder.setTitle(R.string.dialog_service_error_title);
            builder.setMessage(R.string.dialog_service_error_message);
            builder.setNegativeButton(R.string.general_btn_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        /**
         * Shows a FloatingActionButton with an animation, after the delay provided.
         *
         * @param fab   The FloatingActionButton to show.
         * @param delay The amount of delay till the fab will be shown (in millis).
         */
        public static void showFabWithAnimation(final FloatingActionButton fab, final int delay) {
            fab.setVisibility(View.INVISIBLE);
            fab.setScaleX(0.0F);
            fab.setScaleY(0.0F);
            fab.setAlpha(0.0F);
            fab.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    fab.getViewTreeObserver().removeOnPreDrawListener(this);
                    fab.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.show();
                        }
                    }, delay);
                    return true;
                }
            });
        }

    }

    /**
     * Represents utilities and helpers for networking.
     */
    public static final class Networking {

        /**
         * Holds a constant for holding a string result for "no connection".
         */
        public static final java.lang.String NO_CONNECTION_RESULT = "NO_CONNECTION_RESULT";

        /**
         * Sends an HTTP request to the url provided.
         *
         * @param urlString The url to send the HTTP request.
         * @param context   The context.
         * @return Returns the response from the HTTP request.
         * @throws NoNetworkException Throws a NoNetworkException when there's no internet connection.
         */
        public static java.lang.String sendHttpRequest(java.lang.String urlString, Context context) throws NoNetworkException {
            if (urlString == null || urlString.trim().equals(""))
                throw new NullPointerException("urlString");

            HttpURLConnection httpCon = null;
            InputStream input_stream = null;
            InputStreamReader input_stream_reader = null;
            BufferedReader input = null;
            StringBuilder response = new StringBuilder();
            try {
                // Checks if there's no network connection:
                if (!isNetworkAvailable(context))
                    throw new NoNetworkException();

                URL url = new URL(urlString);
                httpCon = (HttpURLConnection) url.openConnection();

                if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("TAG", "Cannot Connect to : " + urlString);
                    return null;
                }

                input_stream = httpCon.getInputStream();
                input_stream_reader = new InputStreamReader(input_stream);
                input = new BufferedReader(input_stream_reader);
                java.lang.String line;
                while ((line = input.readLine()) != null) {
                    response.append(line).append("\n");
                }
            } catch (NoNetworkException e) {
                throw e;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input_stream_reader.close();
                        input_stream.close();
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (httpCon != null) {
                        httpCon.disconnect();
                    }
                }
            }
            return response.toString();
        }

        /**
         * Detects if a network connection is available in the device.
         *
         * @param context The context.
         * @return Returns true if a network connection is available, otherwise false.
         */
        public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        /**
         * Creates a bitmap of an image from a url.
         *
         * @param url The url to get the image from.
         * @return Returns the image bitmap.
         * @throws IOException
         */
        public static Bitmap bitmapFromUrl(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }

    }

    /**
     * Represents utilities and helpers for imaging.
     */
    public static final class Image {

        //region Private Members

        /**
         * Holds the internal storage path.
         */
        private static String _internalStoragePath = null;

        //endregion

        /**
         * Gets the internal images storage path.
         *
         * @param context The context owner.
         * @return Returns the internal images storage path.
         */
        public static String getInternalStoragePath(@NonNull Context context) {
            if (_internalStoragePath != null)
                return _internalStoragePath;

            _internalStoragePath = new ContextWrapper(context).getDir("images", Context.MODE_APPEND).getAbsolutePath();
            return _internalStoragePath;
        }

        /**
         * Saves an image to the internal storage.
         *
         * @param imageBitmap The image bitmap to save.
         * @param fileName    The name of the image file.
         * @param context     The context owner.
         * @return Returns the path file of the saved image.
         */
        public static String saveToInternalStorage(@NonNull Bitmap imageBitmap, @NonNull String fileName, @NonNull Context context) {
            String savePath = null;
            File path = new File(getInternalStoragePath(context), fileName + ".jpg"); // Sets the image file path.

            if (path.exists())
                return path.getAbsolutePath();

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // Writes the image to the OutputStream.
                savePath = path.getAbsolutePath(); // Sets the save path, to be returned.
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return savePath;
        }

        /**
         * Saves an image to the internal storage.
         *
         * @param imageBitmap The image bitmap to save.
         * @param fileName    The name of the image file.
         * @param context     The context owner.
         * @return Returns the path file of the saved image.
         */
        public static String saveIconToInternalStorage(@NonNull Bitmap imageBitmap, @NonNull String fileName, @NonNull Context context) {
            String savePath = null;
            File path = new File(getInternalStoragePath(context), fileName + ".png"); // Sets the image file path.

            if (path.exists())
                return path.getAbsolutePath();

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); // Writes the image to the OutputStream.
                savePath = path.getAbsolutePath(); // Sets the save path, to be returned.
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return savePath;
        }

        /**
         * Deletes an image from the internal storage.
         *
         * @param fileName The file name to delete.
         * @param context  The context owner.
         * @return Returns true if the image deleted, otherwise false.
         */
        public static boolean deleteFromInternalStorage(String fileName, @NonNull Context context) {
            if (fileName == null)
                throw new NullPointerException("fileName");

            File file = new File(getInternalStoragePath(context), fileName + ".jpg"); // Sets the image file path.
            return file.delete();
        }

    }

    /**
     * Represents utilities and helpers for GPS.
     */
    public static final class GPS {

        /**
         * Checks if the GPS is enabled or not.
         *
         * @param context The context owner.
         * @return Returns true if the GPS is enabled, otherwise false.
         */
        public static boolean checkGpsEnabled(@NonNull Context context) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        /**
         * Checks if the network is enabled or not.
         *
         * @param context The context owner.
         * @return Returns true if the network is enabled, otherwise false.
         */
        public static boolean checkNetworkEnabled(@NonNull Context context) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        /**
         * Displays the location settings request, if location is disabled.
         *
         * @param activity    The activity owner.
         * @param requestCode The request code of the location settings dialog.
         */
        public static void displayLocationSettingsRequest(final Activity activity, final int requestCode) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Shows the dialog by calling startResolutionForResult(), and the result will be in the onActivityResult():
                                status.startResolutionForResult(activity, requestCode);
                            } catch (IntentSender.SendIntentException ignored) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }

        /**
         * Checks if "Google Play Services" is available or not.
         *
         * @param context The context owner.
         * @return Returns true if "Google Play Services" is available, otherwise false.
         */
        public static boolean isPlayServicesAvailable(Context context) {
            return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        }

    }

    /**
     * Represents utilities and helpers for "LatLng" class.
     */
    public static final class LatLng {

        /**
         * Serializes a LatLng object to a string representation.
         *
         * @param latLng The LatLng object to serialize a string representation.
         * @return Returns a string representation of the LatLng object.
         */
        public static String serialize(@NonNull com.google.android.gms.maps.model.LatLng latLng) {
            return latLng.latitude + "," + latLng.longitude;
        }

        /**
         * Parses a serialized LatLng object (string represent), to a LatLng object.
         *
         * @param latLng The serialized LatLng object (string represent), to parse a LatLng object.
         * @return Returns a LatLng object parsed from a serialized LatLng string representation, or null if couldn't parse.
         */
        public static com.google.android.gms.maps.model.LatLng parse(String latLng) {
            try {
                String[] tmp = latLng.split(",");
                return new com.google.android.gms.maps.model.LatLng(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Calculates the distance between two location coordinates, in meters.
         *
         * @param location1 The first location.
         * @param location2 The second location.
         * @return Returns the distance between two locations, in meters.
         */
        public static double calculateDistanceInMeters(com.google.android.gms.maps.model.LatLng location1, com.google.android.gms.maps.model.LatLng location2) {
            final int R = 6371; // Average radius of the earth in Km.

            Double latDistance = Math.toRadians(location2.latitude - location1.latitude);
            Double lonDistance = Math.toRadians(location2.longitude - location1.longitude);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c * 1000; // Converts to meters.
        }

    }

    /**
     * Represents utilities and helpers for SharedPreferences.
     */
    public static final class Preferences {

        /**
         * Represents preference keys in the SharedPreferences.
         */
        public static final class Keys {

            /**
             * Holds a constant for the "Map Resources Loaded" preference key.
             */
            public static final String MAP_RESOURCES_LOADED = "pref_map_resources_loaded";

            /**
             * Holds a constant for the "Last Location" preference key.
             */
            public static final String LAST_LOCATION = "pref_last_location";

            /**
             * Holds a constant for the "Map Zoom" preference key.
             */
            public static final String MAP_ZOOM = "pref_map_zoom";

            /**
             * Holds a constant for the "Language" preference key.
             */
            public static final String LANGUAGE = "pref_language";

            /**
             * Holds a constant for the "Unit of length" preference key.
             */
            public static final String UNIT_OF_LENGTH = "pref_unit_of_length";

            /**
             * Holds a constant for the "Erase all favorites" preference key.
             */
            public static final String ERASE_ALL_FAVORITES = "pref_erase_favorites";

            /**
             * Holds a constant for the "Erase search history" preference key.
             */
            public static final String ERASE_SEARCH_HISTORY = "pref_erase_search_history";

            /**
             * Holds a constant for the "Rate app" preference key.
             */
            public static final String RATE_APP = "pref_rate_app";

            /**
             * Holds a constant for the "About app" preference key.
             */
            public static final String ABOUT_APP = "pref_about_app";

        }

        /**
         * Represents preference values in the SharedPreferences.
         */
        public static final class Values {

            //region Public Members

            /**
             * Holds a constant for the "Km" value in the "Unit of length" preference.
             */
            public static final int UNIT_OF_LENGTH_Km = 1;

            /**
             * Holds a constant for the "Miles" value in the "Unit of length" preference.
             */
            public static final int UNIT_OF_LENGTH_Miles = 2;

            //endregion

        }

    }

    /**
     * Represents utilities and helpers for units, like: meters, Km, and Miles.
     */
    public static final class Units {

        /**
         * Holds a constant for the conversion rate from meters to yards.
         */
        public static final double M_TO_YD = 1.09361;

        /**
         * Displays meters in the correct string format.
         *
         * @param meters  The meters to display.
         * @param context The context owner.
         * @return Returns the correct string format to display the meters.
         */
        public static String displayMeters(int meters, @NonNull Context context) {
            if (meters < 0)
                throw new IllegalStateException("meters");

            if (meters < 1000) {
                return meters + " " + context.getString(R.string.unit_meter);
            } else {
                double km = Math.floor(((double) meters / 1000) * 10) / 10;
                if (km - (int) km < 0.01)
                    return (int) km + " " + context.getString(R.string.unit_kilometer);
                else
                    return km + " " + context.getString(R.string.unit_kilometer);
            }
        }

        /**
         * Displays meters array in the correct format.
         *
         * @param values  The meters array to display.
         * @param context The context owner.
         * @return Returns the correct format to display the meters array.
         */
        public static String[] displayMetersArray(int[] values, @NonNull Context context) {
            String[] array = new String[values.length];

            for (int i = 0; i < values.length; i++)
                array[i] = Units.displayMeters(values[i], context);

            return array;
        }

        /**
         * Displays yards in the correct string format.
         *
         * @param yards   The yards to display.
         * @param context The context owner.
         * @return Returns the correct string format to display the yards.
         */
        public static String displayYards(int yards, @NonNull Context context) {
            if (yards < 0)
                throw new IllegalStateException("yards");

            if (yards < 1760) {
                return yards + " " + context.getString(R.string.unit_yard);
            } else {
                double miles = Math.floor(((double) yards / 1760) * 10) / 10;
                if (miles - (int) miles < 0.01)
                    return (int) miles + " " + context.getString(R.string.unit_mile);
                else
                    return miles + " " + context.getString(R.string.unit_mile);
            }
        }

        /**
         * Displays yards array in the correct format.
         *
         * @param values  The yards array to display.
         * @param context The context owner.
         * @return Returns the correct format to display the yards array.
         */
        public static String[] displayYardsArray(int[] values, @NonNull Context context) {
            String[] array = new String[values.length];

            for (int i = 0; i < values.length; i++)
                array[i] = Units.displayYards((int) (values[i] * M_TO_YD), context);

            return array;
        }

        /**
         * Displays a distance in meters to the specified unit.
         *
         * @param distanceInMeters The distance in meters to display.
         * @param toUnit           The unit to display the distance in.
         * @param context          The context owner.
         * @return Returns the formatted string to display the distance in the unit.
         */
        public static String displayDistance(double distanceInMeters, int toUnit, @NonNull Context context) {
            if (toUnit == Preferences.Values.UNIT_OF_LENGTH_Km) {
                return displayMeters((int) distanceInMeters, context);
            } else if (toUnit == Preferences.Values.UNIT_OF_LENGTH_Miles) {

                return displayYards((int) (distanceInMeters * M_TO_YD), context);
            } else {
                throw new IllegalStateException("");
            }
        }

    }

    /**
     * Represents utilities and helpers for integers.
     */
    public static final class Ints {

        /**
         * Gets the index of the value in the array.
         *
         * @param value The value to find the index.
         * @param array The array to get from.
         * @return Returns the index of the value in the array if found, otherwise -1.
         */
        public static int indexOf(int value, int[] array) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == value)
                    return i;
            }
            return -1;
        }

    }

    /**
     * Represents utilities and helpers for strings.
     */
    public static final class Strings {

        /**
         * Makes a string in a title representation (changes to camel case).
         *
         * @param str The string to change.
         * @return Returns the title representation (camel case).
         */
        public static String toTitle(String str) {
            StringBuilder sb = new StringBuilder();
            boolean isNextWord = true;

            for (char c : str.toCharArray()) {
                if (c == '_') {
                    c = ' ';
                    isNextWord = true;
                } else if (Character.isSpaceChar(c)) {
                    isNextWord = true;
                } else if (isNextWord) {
                    c = Character.toTitleCase(c);
                    isNextWord = false;
                }
                sb.append(c);
            }

            return sb.toString();
        }

        /**
         * Gets a file name from a url.
         *
         * @param url The url to get the file name from.
         * @return Returns the file name from the url.
         */
        public static String getFileNameFromUrl(String url) {
            if (url == null)
                throw new NullPointerException("url");

            return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        }

    }

    /**
     * Represents utilities and helpers for conversions.
     */
    public static final class Conversions {

        /**
         * Converts dp to px.
         *
         * @param dp      The amount of dp to convert.
         * @param context The context owner.
         * @return Returns the converted px.
         */
        public static int dpToPx(int dp, @NonNull Context context) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        /**
         * Converts px to dp.
         *
         * @param px      The amount of px to convert.
         * @param context The context owner.
         * @return Returns the converted dp.
         */
        public static int pxToDp(int px, @NonNull Context context) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }

    }

    /**
     * Represents utilities and helpers for handling orientations.
     */
    public static final class Orientation {

        /**
         * Locks the orientation state.
         *
         * @param activity The activity owner.
         */
        public static void lock(@NonNull Activity activity) {
            int orientation = activity.getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            else
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        /**
         * Unlocks the orientation state.
         *
         * @param activity The activity owner.
         */
        public static void unlock(@NonNull Activity activity) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

    }


}
