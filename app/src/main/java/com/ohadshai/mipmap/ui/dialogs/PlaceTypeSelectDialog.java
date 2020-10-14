package com.ohadshai.mipmap.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.entities.PlaceType;
import com.ohadshai.mipmap.ui.UIConsts;

import java.util.ArrayList;

import static com.ohadshai.mipmap.R.id.rgPlaceTypes;

/**
 * Represents a place type select dialog.
 * Created by Ohad on 9/18/2016.
 */
public class PlaceTypeSelectDialog extends DialogFragment implements DialogInterface.OnShowListener, SearchView.OnQueryTextListener {

    //region Private Members

    /**
     * Holds the result listener for the dialog.
     */
    private ResultListener _listener;

    /**
     * Holds the list of all place types objects from the array resource.
     */
    private ArrayList<PlaceType> _placeTypes = new ArrayList<>();

    /**
     * Holds the filtered list of place types from the search.
     */
    private ArrayList<PlaceType> _filtered = new ArrayList<>();

    /**
     * Holds the current place type in the search fields.
     */
    private PlaceType _currentPlaceType;

    /**
     * Holds the RadioGroup control for the dialog.
     */
    private RadioGroup _rgPlaceTypes;

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
        View dialogView = inflater.inflate(R.layout.dialog_place_type_select, null);

        this.initControls(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        this.updatePlaceTypesList(_placeTypes);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.search(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.search(newText);
        return true;
    }

    //endregion

    //region Public API

    /**
     * Sets an event listener for the result from the dialog.
     *
     * @param listener The listener to set.
     */
    public void setResultListener(ResultListener listener) {
        _listener = listener;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all dialog view controls.
     *
     * @param view The view of the dialog.
     */
    private void initControls(View view) {

        // Gets the place types arrays from the resource file:
        String[] placeTypes = getResources().getStringArray(R.array.placeTypes);
        String[] placeTypesValues = getResources().getStringArray(R.array.placeTypeValues);

        for (int i = 0; i < placeTypes.length; i++) {
            PlaceType type = new PlaceType(placeTypes[i], placeTypesValues[i]);
            _placeTypes.add(type);
            _filtered.add(type);
        }

        // Gets the current place type in the search (if exists):
        if (getArguments() != null)
            _currentPlaceType = getArguments().getParcelable(UIConsts.Bundles.PLACE_TYPE);

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
                if (_rgPlaceTypes.getCheckedRadioButtonId() < 0) {
                    Toast.makeText(getActivity(), R.string.dialog_place_type_validation, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (_listener != null)
                    _listener.onPositiveResult(_filtered.get(_rgPlaceTypes.getCheckedRadioButtonId()));

                dismiss();
            }
        });

        Button btnReset = (Button) view.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_listener != null)
                    _listener.onReset();

                dismiss();
            }
        });

        SearchView searchPlaceType = (SearchView) view.findViewById(R.id.searchPlaceType);
        searchPlaceType.setIconified(true);
        searchPlaceType.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackgroundColor(Color.TRANSPARENT); // Hides the search underline.
        searchPlaceType.setOnQueryTextListener(this);

        _rgPlaceTypes = (RadioGroup) view.findViewById(rgPlaceTypes);

    }

    /**
     * Searches the place types list.
     *
     * @param query The search query text.
     */
    private void search(String query) {
        query = query.trim().toLowerCase();
        if (query.equals("")) {
            this.updatePlaceTypesList(_placeTypes);
            return;
        }

        _filtered.clear();
        // Filters the place types list by the query text:
        for (PlaceType type : _placeTypes) {
            if (type.getName().toLowerCase().contains(query))
                _filtered.add(type);
        }

        this.updatePlaceTypesList(_filtered);
    }

    /**
     * Updates the place types list with a new list of place types.
     *
     * @param placeTypes The new list of place type to update.
     */
    private void updatePlaceTypesList(ArrayList<PlaceType> placeTypes) {
        _rgPlaceTypes.clearCheck();
        _rgPlaceTypes.removeAllViews();

        for (int i = 0; i < placeTypes.size(); i++) {
            PlaceType type = placeTypes.get(i);
            RadioButton rb = new RadioButton(getActivity());
            rb.setId(i);
            rb.setPadding(50, 30, 50, 30);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setText(type.getName());
            _rgPlaceTypes.addView(rb, i, new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (_currentPlaceType != null && _currentPlaceType.getValue().equals(type.getValue()))
                _rgPlaceTypes.check(i);
        }

        // Checks if there're no results:
        if (placeTypes.size() < 1) {
            TextView tv = new TextView(getActivity());
            tv.setText(R.string.general_msg_no_results);
            tv.setPadding(0, 50, 0, 50);
            tv.setGravity(Gravity.CENTER);
            _rgPlaceTypes.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a callback for the result from the PlaceTypeSelectDialog.
     */
    public interface ResultListener {

        /**
         * Event occurs when there's a positive result from the dialog (place type has been selected).
         *
         * @param placeType The place type selected from the dialog.
         */
        void onPositiveResult(PlaceType placeType);

        /**
         * Event occurs when a reset requested from the dialog.
         */
        void onReset();

    }

    //endregion

}
