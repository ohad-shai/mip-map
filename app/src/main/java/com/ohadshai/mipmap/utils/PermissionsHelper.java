package com.ohadshai.mipmap.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.dialogs.PermissionExplanationDialog;

/**
 * Represents a helper for granting permissions in the application.
 * Created by Ohad on 12/3/2016.
 */
public class PermissionsHelper {

    //region Public Constants

    /**
     * Holds a constant for the "Location" permission request code.
     */
    public static final int LOCATION = 1;

    //endregion

    //region Private Members

    /**
     * Holds the list of all the valid permissions of the "PermissionsHelper".
     */
    private int[] _validPermissions = new int[]{LOCATION};

    /**
     * Holds the instance of the "PermissionsHelper" for all the application.
     */
    private static PermissionsHelper _instance;

    /**
     * Holds the current relation between the helper to an activity.
     */
    private PermissionsHelper.Relation _currentRelation;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of a helper for granting permissions in the application.
     */
    private PermissionsHelper() {
        // Disables the initialization of a new instance from the outside,
        // in order to implement a singleton manner.
    }

    //endregion

    //region Events

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Activity activity) {
        // Checks if there's no relation to the "PermissionsHelper":
        if (_currentRelation == null || _currentRelation._activity != activity)
            return;

        // Granting process ended:
        _currentRelation._isGranting = false;

        if (_currentRelation._runnable != null) {
            // Fires the relevant event according to the grant result from the user:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                _currentRelation._runnable.runGrantedCallback(); // Permission granted.
            } else {
                if (!PermissionExplanationDialog.isShowing())
                    _currentRelation.showExplanation(requestCode);
                _currentRelation._runnable.runDeniedCallback(); // Permission denied.
            }
        }
    }

    //endregion

    //region Public Static API

    /**
     * Gets the "PermissionsHelper" instance of the application, or creates a new instance if null.
     *
     * @return Returns the "PermissionsHelper" instance of the application.
     */
    public static PermissionsHelper getInstance() {
        if (_instance == null)
            _instance = new PermissionsHelper();

        return _instance;
    }

    /**
     * Makes a new relation for the "PermissionsHelper", to relate the provided activity.
     *
     * @param activity The activity to relate the "PermissionsHelper".
     * @return Returns a relation object between the "PermissionsHelper" to the activity.
     */
    public static PermissionsHelper.Relation with(Activity activity) {
        return PermissionsHelper.getInstance().initializeRelation(activity);
    }

    /**
     * Checks if a permission is granted by the user or not.
     *
     * @param permission The permission code to check if granted or not.
     * @return Returns true if the permission is granted, otherwise false.
     */
    public static boolean checkPermission(int permission, @NonNull Context context) {
        switch (permission) {
            case LOCATION:
                return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION));
            default:
                throw new IllegalStateException("Permission not found.");
        }
    }

    /**
     * Tries to request a permission from the user, and returns an indicator if requested or not (in cases where the user chose "Never ask again").
     *
     * @param permission The permission code to request from the user.
     * @param activity   The activity owner.
     * @return Returns true if the permission request dialog showed, otherwise false (in cases where the user chose "Never ask again").
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean tryRequestPermission(int permission, @NonNull Activity activity) {
        switch (permission) {
            case LOCATION:
                boolean isRequested = activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                if (isRequested)
                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionsHelper.LOCATION);
                return isRequested;
            default:
                throw new IllegalStateException("Permission not found.");
        }
    }

    //endregion

    //region Public API

    /**
     * Gets the current relation between the "PermissionsHelper" and an activity.
     *
     * @return Returns the current relation between the "PermissionsHelper" and an activity.
     */
    public PermissionsHelper.Relation getCurrentRelation() {
        return _currentRelation;
    }

    /**
     * Checks if the "PermissionsHelper" is currently granting a permission (on process, waiting for the user grant).
     *
     * @return Returns true if currently granting a permission, otherwise false.
     */
    public boolean isGranting() {
        if (_currentRelation != null)
            return _currentRelation._isGranting;
        else
            return false;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes the relation for the "PermissionsHelper", to relate the provided activity.
     *
     * @param activity The activity to relate the "PermissionsHelper".
     * @return Returns a relation object between the "PermissionsHelper" to the activity.
     */
    private PermissionsHelper.Relation initializeRelation(Activity activity) {
        // Initializes a new instance if it's a new relation with a new activity:
        if (_currentRelation == null || _currentRelation._activity != activity)
            _currentRelation = new PermissionsHelper.Relation(activity);

        return _currentRelation;
    }

    /**
     * Handles a permission.
     * Checks if the permission is granted, and also requests the permission if not granted,
     * then returns an indicator if the permission is granted or not.
     *
     * @param permission         The permission identifier in the "PermissionsHelper".
     * @param permissionManifest The permission manifest name.
     * @return Returns true if the permission is granted, otherwise false.
     */
    private boolean handlePermission(int permission, @NonNull String permissionManifest) {
        if (_currentRelation == null)
            throw new NullPointerException("_currentRelation");

        // Checks if the permission is granted by the user:
        int result = ContextCompat.checkSelfPermission(_currentRelation._activity, permissionManifest);
        boolean isGranted = (result == PackageManager.PERMISSION_GRANTED);

        // Checks if the permission is not granted, in order to grant it from the user:
        if (!isGranted) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(_currentRelation._activity, permissionManifest)) {
                _currentRelation.showExplanation(permission);

                if (_currentRelation._runnable != null)
                    _currentRelation._runnable.runDeniedCallback(); // Fires the permission denied event.
            } else {
                _currentRelation._isGranting = true;
                ActivityCompat.requestPermissions(_currentRelation._activity, new String[]{permissionManifest}, permission);
            }
        }

        return isGranted;
    }

    /**
     * Checks if a permission is valid or not, in the "PermissionsHelper".
     *
     * @param permission The permission to check if valid or not.
     * @return Returns true if the permission is valid, otherwise false.
     */
    private boolean checkPermissionValid(int permission) {
        for (int p : _validPermissions) {
            if (p == permission)
                return true;
        }
        return false;
    }

    //endregion

    //region Public Inner Classes

    /**
     * Represents a relation between the "PermissionsHelper" to the activity.
     */
    public class Relation {

        //region Private Members

        /**
         * Holds the activity to relate the "PermissionsHelper".
         */
        private Activity _activity;

        /**
         * Holds the runnable object for the relation.
         */
        private PermissionsHelper.Runnable _runnable;

        /**
         * Holds an indicator indicating if currently granting a permission.
         */
        private boolean _isGranting;

        //endregion

        /**
         * Initializes a new instance of a a relation between the "PermissionsHelper" to the activity.
         *
         * @param _activity The activity to relate the "PermissionsHelper".
         */
        Relation(Activity _activity) {
            this._activity = _activity;
            this._isGranting = false;
        }

        //region Public API

        /**
         * Specifies and grants the needed permission in order to run the callback.
         *
         * @param permission The needed permission in order to run the callback.
         * @return Returns a new Runnable object, which runs the callback if the permission is granted by the user.
         */
        public PermissionsHelper.Runnable needs(int permission) {
            switch (permission) {
                case LOCATION:
                    return _runnable = new PermissionsHelper.Runnable(handlePermission(LOCATION, Manifest.permission.ACCESS_FINE_LOCATION));
                default:
                    throw new IllegalStateException("Invalid permission.");
            }
        }

        /**
         * Gets the activity in relation with the "PermissionsHelper".
         *
         * @return Returns the activity in relation with the "PermissionsHelper".
         */
        public Activity getActivity() {
            return _activity;
        }

        //endregion

        //region Private Methods

        /**
         * Shows an explanation to a permission.
         *
         * @param permission The permission code to show the explanation.
         */
        private void showExplanation(int permission) {
            switch (permission) {
                case LOCATION:
                    showExplanationDialog(new Explanation(LOCATION,
                            _currentRelation._activity.getString(R.string.permission_location),
                            _currentRelation._activity.getString(R.string.permission_location_description),
                            R.mipmap.ic_location_on));
                    break;
                default:
                    throw new IllegalStateException("Invalid permission.");
            }
        }

        /**
         * Shows a permission explanation dialog.
         *
         * @param explanation The permission explanation object to initial the dialog.
         */
        private void showExplanationDialog(Explanation explanation) {
            PermissionExplanationDialog _explanationDialog = new PermissionExplanationDialog();
            Bundle dialogBundle = new Bundle();
            dialogBundle.putParcelable(Explanation.PERMISSION_EXPLANATION_SERIALIZABLE_KEY, explanation);
            _explanationDialog.setArguments(dialogBundle);
            _explanationDialog.show(_currentRelation._activity.getFragmentManager(), UIConsts.Fragments.PERMISSION_EXPLANATION_DIALOG_TAG);
        }

        //endregion

    }

    /**
     * Represents a runnable procedure for a granted permission.
     */
    public class Runnable {

        //region Private Members

        /**
         * Holds an indicator indicating whether the needed permission is granted or not.
         */
        private boolean _isPermissionGranted;

        /**
         * Holds the callback to run after the needed permission is granted.
         */
        private PermissionsHelper.PermissionResultCallback _callback;

        //endregion

        /**
         * Initializes a new instance of a runnable procedure for a granted permission.
         *
         * @param isPermissionGranted An indicator indicating whether the needed permission is granted or not.
         */
        Runnable(boolean isPermissionGranted) {
            this._isPermissionGranted = isPermissionGranted;
        }

        //region Public API

        /**
         * Runs the callback - only if the permission is granted by the user.
         *
         * @param callback The callback to run after the needed permission is granted.
         */
        public void run(PermissionsHelper.PermissionResultCallback callback) {
            if (callback == null)
                throw new NullPointerException("callback");

            this._callback = callback;

            if (_isPermissionGranted)
                runGrantedCallback();
            else
                runDeniedCallback();
        }

        //endregion

        //region Local Methods

        /**
         * Runs the permission granted callback (Fires the callback event).
         */
        void runGrantedCallback() {
            if (_callback != null)
                _callback.onPermissionGranted();
        }

        /**
         * Runs the permission denied callback (Fires the callback event).
         */
        void runDeniedCallback() {
            if (_callback != null)
                _callback.onPermissionDenied();
        }

        //endregion

    }

    /**
     * Represents a callback for a permission result.
     */
    public static abstract class PermissionResultCallback {

        /**
         * Event occurs when a permission is granted by the user.
         */
        public void onPermissionGranted() {
        }

        /**
         * Event occurs when a permission is denied by the user.
         */
        public void onPermissionDenied() {
        }

    }

    /**
     * Represents a permission explanation.
     */
    public static class Explanation implements Parcelable {

        //region Private Members

        /**
         * Holds the permission code.
         */
        private int _permissionCode;

        /**
         * Holds the permission name.
         */
        private String _name;

        /**
         * Holds the permission description.
         */
        private String _description;

        /**
         * Holds the permission icon (resource id).
         */
        private int _icon;

        //endregion

        //region Public Members

        /**
         * Holds a constant for the "Permission Explanation" serializable key.
         */
        public static final String PERMISSION_EXPLANATION_SERIALIZABLE_KEY = "serializable_permission_explanation";

        //endregion

        //region C'tors

        /**
         * Initializes a new instance of a permission explanation.
         *
         * @param permissionCode The permission code.
         * @param name           The permission name.
         * @param description    The permission description.
         * @param icon           The permission icon (resource id).
         */
        public Explanation(int permissionCode, String name, String description, int icon) {
            this._permissionCode = permissionCode;
            this._name = name;
            this._description = description;
            this._icon = icon;
        }

        //endregion

        //region Public API

        /**
         * Gets the permission code.
         *
         * @return Returns the permission code.
         */
        public int getPermissionCode() {
            return _permissionCode;
        }

        /**
         * Sets the permission code.
         *
         * @param permissionCode The permission code to set.
         */
        public void setPermissionCode(int permissionCode) {
            this._permissionCode = permissionCode;
        }

        /**
         * Gets the permission name.
         *
         * @return Returns the permission name.
         */
        public String getName() {
            return _name;
        }

        /**
         * Sets the permission name.
         *
         * @param _name The permission name to set.
         */
        public void setName(String _name) {
            this._name = _name;
        }

        /**
         * Gets the permission description.
         *
         * @return Returns the permission description.
         */
        public String getDescription() {
            return _description;
        }

        /**
         * Sets the permission description.
         *
         * @param _description The permission description to set.
         */
        public void setDescription(String _description) {
            this._description = _description;
        }

        /**
         * Gets the permission icon (resource id).
         *
         * @return Returns the permission icon (resource id).
         */
        public int getIcon() {
            return _icon;
        }

        /**
         * Sets the permission icon (resource id).
         *
         * @param _icon The permission icon (resource id) to set.
         */
        public void setIcon(int _icon) {
            this._icon = _icon;
        }

        //endregion

        //region Parcelable Implementation

        protected Explanation(Parcel in) {
            _permissionCode = in.readInt();
            _name = in.readString();
            _description = in.readString();
            _icon = in.readInt();
        }

        public static final Creator<Explanation> CREATOR = new Creator<Explanation>() {
            @Override
            public Explanation createFromParcel(Parcel in) {
                return new Explanation(in);
            }

            @Override
            public Explanation[] newArray(int size) {
                return new Explanation[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(_permissionCode);
            dest.writeString(_name);
            dest.writeString(_description);
            dest.writeInt(_icon);
        }

        //endregion

    }

    //endregion

    //region Private Inner Classes

    /**
     * Represents a callback for a permission explanation.
     */
    private interface PermissionExplanationCallback {

        /**
         * Event occurs when a permission explanation is requested to be shown.
         */
        void showExplanation();

    }

    //endregion

}
