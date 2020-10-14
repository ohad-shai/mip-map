package com.ohadshai.mipmap.utils;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

/**
 * Represents a merger between a {@link android.view.View} control and a {@link android.support.design.widget.BottomSheetBehavior} of a bottom sheet.
 * Created by Ohad on 2/10/2017.
 */
public class BottomSheet {

    //region Private Members

    /**
     * Holds the view of the bottom sheet.
     */
    private View _view;

    /**
     * Holds the bottom sheet behavior control.
     */
    private BottomSheetBehavior _behavior;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a merger between a {@link android.view.View} control and a {@link android.support.design.widget.BottomSheetBehavior} of a bottom sheet.
     *
     * @param view       The view of the bottom sheet.
     * @param peekHeight The peek height of the bottom sheet.
     */
    public BottomSheet(@NonNull View view, int peekHeight) {
        this(view, BottomSheetBehavior.from(view), peekHeight);
    }

    /**
     * Initializes a new instance of a merger between a {@link android.view.View} control and a {@link android.support.design.widget.BottomSheetBehavior} of a bottom sheet.
     *
     * @param view       The view of the bottom sheet.
     * @param behavior   The bottom sheet behavior control.
     * @param peekHeight The peek height of the bottom sheet.
     */
    public BottomSheet(@NonNull View view, BottomSheetBehavior behavior, int peekHeight) {
        this._view = view;
        this._behavior = behavior;
        this._behavior.setPeekHeight(peekHeight);
    }

    //endregion

    //region Public API

    /**
     * Gets the view of the bottom sheet.
     *
     * @return Returns the view of the bottom sheet.
     */
    public View getView() {
        return _view;
    }

    /**
     * Gets the bottom sheet behavior control.
     *
     * @return Returns the bottom sheet behavior control.
     */
    public BottomSheetBehavior getBehavior() {
        return _behavior;
    }

    /**
     * Peeks the bottom sheet.
     */
    public void peek() {
        _behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Hides the bottom sheet.
     */
    public void hide() {
        _behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**
     * Expands the bottom sheet.
     */
    public void expend() {
        _behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    //endregion

}
