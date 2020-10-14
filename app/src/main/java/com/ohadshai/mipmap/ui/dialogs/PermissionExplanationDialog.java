package com.ohadshai.mipmap.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.utils.PermissionsHelper;

/**
 * Represents a dialog for a permission explanation.
 * Created by Ohad on 12/7/2016.
 */
public class PermissionExplanationDialog extends DialogFragment {

    //region Private Members

    /**
     * Holds an indicator indicating whether the dialog is showing or not.
     */
    private static boolean _isShowing;

    /**
     * Holds the permission explanation.
     */
    private PermissionsHelper.Explanation _permissionExplanation;

    //endregion

    //region Dialog Events

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_permission_explanation, null);

        this.initControls(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        _isShowing = true;

        // Checks if the permission is granted by the user, on the resume of the dialog:
        if (PermissionsHelper.checkPermission(_permissionExplanation.getPermissionCode(), getActivity())) {
            PermissionsHelper.getInstance().onRequestPermissionsResult(_permissionExplanation.getPermissionCode(), new String[]{""}, new int[]{PackageManager.PERMISSION_GRANTED}, getActivity());
            this.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _isShowing = false;
    }

    //endregion

    //region Public Static API

    /**
     * Indicating whether the dialog is showing or not.
     *
     * @return Returns true if the dialog is showing, otherwise false.
     */
    public static boolean isShowing() {
        return _isShowing;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all dialog view controls.
     *
     * @param view The view of the dialog.
     */
    private void initControls(final View view) {
        // Gets the permission explanation object assigned to the bundle:
        _permissionExplanation = getArguments().getParcelable(PermissionsHelper.Explanation.PERMISSION_EXPLANATION_SERIALIZABLE_KEY);
        if (_permissionExplanation == null)
            throw new NullPointerException("_permissionExplanation");

        ImageButton imgbtnClose = (ImageButton) view.findViewById(R.id.imgbtnClose);
        imgbtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(view.getContext(), getString(R.string.general_btn_close), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        ImageView imgPermissionIcon = (ImageView) view.findViewById(R.id.imgPermissionIcon);
        imgPermissionIcon.setImageResource(_permissionExplanation.getIcon());

        TextView txtPermissionName = (TextView) view.findViewById(R.id.txtPermissionName);
        txtPermissionName.setText(_permissionExplanation.getName());

        TextView txtPermissionDescription = (TextView) view.findViewById(R.id.txtPermissionDescription);
        txtPermissionDescription.setText(_permissionExplanation.getDescription());

        Button btnAllow = (Button) view.findViewById(R.id.btnAllow);
        btnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRequestPermission();
            }
        });

    }

    /**
     * Handles a request permission.
     * Tries to request the permission via the android dialog, if the user chose "Never ask again" - then opens the "App Settings" activity.
     */
    private void handleRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionsHelper.tryRequestPermission(PermissionsHelper.LOCATION, getActivity())) {
            return;
        } else {
            Intent appSettingsIntent = new Intent();
            appSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            appSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            appSettingsIntent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            getActivity().startActivity(appSettingsIntent);
        }
    }

    //endregion

}
