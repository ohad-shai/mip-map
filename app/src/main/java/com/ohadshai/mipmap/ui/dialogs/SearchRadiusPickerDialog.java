package com.ohadshai.mipmap.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.utils.Utils;

/**
 * Represents a search radar picker dialog.
 * Created by Ohad on 9/18/2016.
 */
public class SearchRadiusPickerDialog extends DialogFragment {

    //region Private Members

    /**
     * Holds the unit of length for the radius value.
     */
    private int _unitOfLength;

    /**
     * Holds the list of radius values.
     */
    private int[] _radiusValues;

    /**
     * Holds the NumberPicker control for the dialog.
     */
    private NumberPicker _numPickerSearchRadius;

    /**
     * Holds the positive result listener.
     */
    private PositiveResultListener _positiveResult;

    //endregion

    //region Events

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_radius_picker, null);

        this.initControls(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }

    //endregion

    //region Public API

    /**
     * Sets an event listener for a positive result from the dialog.
     *
     * @param listener The listener to set.
     */
    public void setOnPositiveResultListener(PositiveResultListener listener) {
        _positiveResult = listener;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all dialog view controls.
     *
     * @param view The view of the dialog.
     */
    private void initControls(View view) {

        int unitOfLength = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
        if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
            _radiusValues = getResources().getIntArray(R.array.searchRadiusValuesMeters);
        else if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
            _radiusValues = getResources().getIntArray(R.array.searchRadiusValuesYardsInMeters);

        int currentRadius = getArguments().getInt(UIConsts.Bundles.SEARCH_RADIUS);

        ImageButton imgbtnClose = (ImageButton) view.findViewById(R.id.imgbtnClose);
        imgbtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast toast = Toast.makeText(getActivity(), R.string.general_btn_close, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return true;
            }
        });

        Button btnOK = (Button) view.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_positiveResult != null)
                    _positiveResult.onPositiveResult(_radiusValues[_numPickerSearchRadius.getValue()]);

                dismiss();
            }
        });

        _numPickerSearchRadius = (NumberPicker) view.findViewById(R.id.numPickerSearchRadius);
        _numPickerSearchRadius.setMinValue(0);
        _numPickerSearchRadius.setMaxValue(_radiusValues.length - 1);
        _numPickerSearchRadius.setWrapSelectorWheel(false);
        _numPickerSearchRadius.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // Checks the unit of length to display the radius values:
        if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
            _numPickerSearchRadius.setDisplayedValues(Utils.Units.displayMetersArray(_radiusValues, getActivity()));
        else if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
            _numPickerSearchRadius.setDisplayedValues(Utils.Units.displayYardsArray(_radiusValues, getActivity()));

        _numPickerSearchRadius.setValue(Utils.Ints.indexOf(currentRadius, _radiusValues));

    }

    //endregion

    //region Inner Classes

    /**
     * Represents a callback for a positive result from the SearchRadiusPickerDialog.
     */
    public interface PositiveResultListener {

        /**
         * Event occurs when there's a positive result from the dialog.
         *
         * @param radius The radius set from the dialog.
         */
        void onPositiveResult(int radius);

    }

    //endregion

}
