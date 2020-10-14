package com.ohadshai.mipmap.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.FavoritePlacesAdapter;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.fragments.FavoritePlacesFragment;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;

import java.util.ArrayList;

import static android.widget.Toast.makeText;
import static com.ohadshai.mipmap.R.id.txtSelected;

/**
 * Represents a helper for handling the favorites reveal action.
 * Created by Ohad on 12/30/2016.
 */
public class FavoritesRevealHelper {

    //region Private Members

    /**
     * Holds a constant for the string of the icons color.
     */
    private static final String ICONS_COLOR = "#219FD1";

    /**
     * Holds the instance of the "FavoritesRevealHelper", in order to implement a singleton manner.
     */
    private static FavoritesRevealHelper _instance;

    /**
     * Holds the current relation for the "FavoritesRevealHelper".
     */
    private FavoritesRevealHelper.Relation _currentRelation;

    /**
     * Holds a list of cached places.
     */
    private ArrayList<Place> _cache = new ArrayList<>();

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a helper for handling the favorites reveal action.
     */
    private FavoritesRevealHelper() {
        // Disables the initialization of a new instance from the outside,
        // in order to implement a singleton manner.
    }

    //endregion

    //region Public Static API

    /**
     * Gets the "FavoritesRevealHelper" instance of the application, or creates a new instance if null.
     *
     * @return Returns the "FavoritesRevealHelper" instance of the application.
     */
    public static FavoritesRevealHelper getInstance() {
        if (_instance == null)
            _instance = new FavoritesRevealHelper();

        return _instance;
    }

    /**
     * Gets the "FavoritesRevealHelper" instance to help the activity.
     *
     * @param activity The activity owner.
     * @return Returns the "FavoritesRevealHelper" instance.
     */
    public static FavoritesRevealHelper.Relation with(@NonNull AppCompatActivity activity) {
        return FavoritesRevealHelper.getInstance().initializeRelation(activity);
    }

    /**
     * Indicates whether the favorites is revealed or not.
     *
     * @return Returns true if the favorites is revealed, otherwise false.
     */
    public static boolean isRevealed() {
        return _instance != null && _instance._currentRelation != null && _instance._currentRelation._IsRevealed;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes the relation for the "FavoritesRevealHelper", to relate the provided activity.
     *
     * @param activity The activity to relate the "FavoritesRevealHelper".
     * @return Returns a relation object between the "FavoritesRevealHelper" to the activity.
     */
    private FavoritesRevealHelper.Relation initializeRelation(@NonNull AppCompatActivity activity) {
        // Initializes a new instance if it's a new relation with a new activity:
        if (_currentRelation == null)
            _currentRelation = new FavoritesRevealHelper.Relation(activity);
        else if (_currentRelation._activity != activity)
            _currentRelation.setActivity(activity);

        return _currentRelation;
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a relation between the reveal helper and an activity.
     */
    public class Relation {

        //region Private Members

        /**
         * Holds the activity owner.
         */
        private AppCompatActivity _activity;

        /**
         * Holds the favorites reveal layout container, which will contain the inflated layout to reveal.
         */
        private FrameLayout _layoutFavoritesRevealContainer;

        /**
         * Holds the favorites layout container.
         */
        private FrameLayout _layoutFavoritesContainer;

        /**
         * Holds an indicator indicating whether the favorites is revealed or not.
         */
        private boolean _IsRevealed;

        /**
         * Holds the favorite places list fragment.
         */
        private FavoritePlacesFragment _favoritePlacesFragment;

        /**
         * Holds the callback for the reveal helper.
         */
        private FavoritesRevealHelper.RevealCallback _callback;

        /**
         * Holds the selection mode interaction object.
         */
        private FavoritesRevealHelper.SelectionMode _selectionMode;

        //endregion

        //region C'tor

        /**
         * Initializes a new instance of a relation between the reveal helper and an activity.
         *
         * @param activity The activity to relate the "FavoritesRevealHelper".
         */
        public Relation(@NonNull AppCompatActivity activity) {
            this.setActivity(activity);
        }

        //endregion

        //region Public API

        /**
         * Initializes the relation (this must be called in the OnCreate() event of the activity).
         * This also restores the state of the search in cases of activity config changes.
         *
         * @param callback The callback methods for the reveal helper.
         */
        public void initialize(FavoritesRevealHelper.RevealCallback callback) {
            this._callback = callback;

            this.checkToRestore();
        }

        /**
         * Reveals the favorites.
         */
        public void reveal() {
            if (_IsRevealed)
                return;

            this.revealNoAnimation();

            //region Search layout animation...

            // Gets the favorites button view in the activity (the view who enters the search):
            final View favoritesButton = _activity.findViewById(R.id.actionFavoritePlaces);
            if (favoritesButton == null)
                throw new NullPointerException("Missing view in the activity: \"actionFavoritePlaces\".");

            // Gets the location of the favorites button on the screen:
            final int[] favoritesBtnLocation = new int[2];
            favoritesButton.getLocationOnScreen(favoritesBtnLocation);

            // Defines the point for the clipping circle:
            int revealX = favoritesBtnLocation[0] + (favoritesButton.getWidth() / 2);
            int revealY = favoritesButton.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            final float revealRadius = (float) Math.hypot(_layoutFavoritesRevealContainer.getWidth(), _layoutFavoritesRevealContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutFavoritesRevealContainer, revealX, revealY, 0, revealRadius);
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                // Fade In for API below 21:
                _layoutFavoritesRevealContainer.setAlpha(0);
                anim = ObjectAnimator.ofFloat(_layoutFavoritesRevealContainer, "alpha", 1);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (_callback != null)
                        _callback.onRevealed();

                    //region Content animation...

                    _layoutFavoritesContainer.setVisibility(View.VISIBLE);

                    int contentRevealX = _layoutFavoritesRevealContainer.getWidth() / 2;

                    Animator contentAnim = null;
                    // Checks and targets the animation capabilities:
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        // Circular Reveal for API above 21:
                        contentAnim = ViewAnimationUtils.createCircularReveal(_layoutFavoritesContainer, contentRevealX, contentRevealX * -1, contentRevealX, revealRadius * 2);
                        contentAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                    } else {
                        // Fade In for API below 21:
                        _layoutFavoritesContainer.setAlpha(0);
                        contentAnim = ObjectAnimator.ofFloat(_layoutFavoritesContainer, "alpha", 1);
                    }
                    contentAnim.setDuration(800);
                    contentAnim.start();

                    //endregion
                }
            });
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDark), _activity.getResources().getColor(R.color.colorAccent));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (_IsRevealed)
                            window.setStatusBarColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ValueAnimator colorAnimation2 = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorAccent), _activity.getResources().getColor(R.color.colorPrimaryDark));
                        colorAnimation2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                if (_IsRevealed)
                                    window.setStatusBarColor((Integer) animator.getAnimatedValue());
                            }
                        });
                        colorAnimation2.setDuration(500);
                        colorAnimation2.start();
                    }
                });
                colorAnimation.setDuration(300);
                colorAnimation.start();

            }

            //endregion
        }

        /**
         * Conceals the favorites.
         */
        public void conceal() {
            if (!_IsRevealed)
                return;

            if (_callback != null)
                _callback.onConceal();

            _IsRevealed = false;

            //region Search layout animation...

            // Gets the favorites button view in the activity (the view who exits the search):
            View favoritesButton = _activity.findViewById(R.id.actionFavoritePlaces);
            if (favoritesButton == null)
                throw new NullPointerException("Missing view in the activity: \"actionFavoritePlaces\".");

            // Gets the location of the favorites button on the screen:
            int[] searchBtnLocation = new int[2];
            favoritesButton.getLocationOnScreen(searchBtnLocation);

            // Defines the point for the clipping circle:
            int revealX = searchBtnLocation[0] + (favoritesButton.getWidth() / 2);
            int revealY = favoritesButton.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(_layoutFavoritesRevealContainer.getWidth(), _layoutFavoritesRevealContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutFavoritesRevealContainer, revealX, revealY, revealRadius, 0);
                anim.setInterpolator(new DecelerateInterpolator());
            } else {
                // Fade Out for API below 21:
                _layoutFavoritesRevealContainer.setAlpha(1);
                anim = ObjectAnimator.ofFloat(_layoutFavoritesRevealContainer, "alpha", 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _layoutFavoritesRevealContainer.setVisibility(View.INVISIBLE);
                    _layoutFavoritesContainer.setVisibility(View.INVISIBLE);

                    if (_callback != null)
                        _callback.onConcealed();

                    clearAllLocals();
                }
            });
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDark), _activity.getResources().getColor(R.color.colorAccent));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        window.setStatusBarColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ValueAnimator colorAnimation2 = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorAccent), _activity.getResources().getColor(R.color.colorPrimaryDark));
                        colorAnimation2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                            }
                        });
                        colorAnimation2.setDuration(1200);
                        colorAnimation2.setInterpolator(new DecelerateInterpolator());
                        colorAnimation2.start();
                    }
                });
                colorAnimation.setDuration(100);
                colorAnimation.start();

            }

            //endregion

            // Checks if any snackbar is showing in this area, closes it:
            if (SnackbarManager.with(_activity).isShowing())
                SnackbarManager.with(_activity).dismiss();

            // Unlocks the orientation for devices below API 21:
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                Utils.Orientation.unlock(_activity);
        }

        /**
         * Conceals the favorites with no animation.
         */
        public void concealNoAnimation() {
            if (!_IsRevealed)
                return;

            if (_callback != null)
                _callback.onConceal();

            _IsRevealed = false;

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(_activity.getResources().getColor(R.color.colorPrimaryDark));
            }

            //endregion

            _layoutFavoritesRevealContainer.setVisibility(View.INVISIBLE);
            _layoutFavoritesContainer.setVisibility(View.INVISIBLE);

            if (_callback != null)
                _callback.onConcealed();

            this.clearAllLocals();

            // Checks if any snackbar is showing in this area, closes it:
            if (SnackbarManager.with(_activity).isShowing())
                SnackbarManager.with(_activity).dismiss();

            // Unlocks the orientation for devices below API 21:
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                Utils.Orientation.unlock(_activity);
        }

        /**
         * Handles back press to close states in the favorites helper.
         *
         * @return Returns an indicator indicating whether the back press was handled by the helper or not.
         */
        public boolean handleBackPress() {
            if (_selectionMode != null && _selectionMode._IsInSelectionMode) {
                _selectionMode.closeSelectionMode();
                return true;
            } else if (_IsRevealed) {
                this.conceal();
                return true;
            } else {
                return false;
            }
        }

        /**
         * Enters the selection mode.
         *
         * @param adapter The adapter owner of the selection.
         * @return Returns the selection mode object.
         */
        public FavoritesRevealHelper.SelectionMode enterSelectionMode(@NonNull FavoritePlacesAdapter adapter) {
            if (_selectionMode != null && _selectionMode._IsInSelectionMode)
                return _selectionMode;

            _selectionMode = new FavoritesRevealHelper.SelectionMode(_activity, (FrameLayout) _layoutFavoritesContainer.findViewById(R.id.layoutSelectionContainer), adapter);
            _selectionMode.enterSelectionMode();
            return _selectionMode;
        }

        /**
         * Refreshes the favorites places list.
         */
        public void refresh() {
            if (!_IsRevealed)
                return;

            _favoritePlacesFragment.loadFavoritePlacesList();
        }

        /**
         * Gets the selection list from the selection mode if exists, otherwise returns null.
         *
         * @return Returns the selection list from the selection mode if exists, otherwise returns null.
         */
        public ArrayList<Place> getSelection() {
            if (_selectionMode != null)
                return _selectionMode.getSelection();
            else
                return null;
        }

        //endregion

        //region Private Methods

        /**
         * Sets the activity owner to relate the "FavoritesRevealHelper".
         *
         * @param activity The activity to relate the "FavoritesRevealHelper".
         */
        private void setActivity(@NonNull AppCompatActivity activity) {
            this._activity = activity;
        }

        /**
         * Reveals the favorites, with no animation.
         */
        private void revealNoAnimation() {
            if (_callback != null)
                _callback.onReveal();

            this.initFavoritesLayoutControls();

            _layoutFavoritesRevealContainer.setVisibility(View.VISIBLE);
            _IsRevealed = true;

            // Locks the orientation for devices below API 21:
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                Utils.Orientation.lock(_activity);
        }

        /**
         * Initializes all the favorites layout control views.
         */
        private void initFavoritesLayoutControls() {

            // Gets the favorites reveal layout container:
            _layoutFavoritesRevealContainer = (FrameLayout) _activity.findViewById(R.id.layoutFavoritesRevealContainer);

            ImageButton imgbtnRevealClose = (ImageButton) _layoutFavoritesRevealContainer.findViewById(R.id.imgbtnRevealClose);
            imgbtnRevealClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conceal();
                }
            });
            imgbtnRevealClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    makeText(_activity, _activity.getString(R.string.general_btn_close), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            // Gets the favorites layout container:
            _layoutFavoritesContainer = (FrameLayout) _layoutFavoritesRevealContainer.findViewById(R.id.layoutFavoritesContainer);

            final ImageButton imgbtnClose = (ImageButton) _layoutFavoritesContainer.findViewById(R.id.imgbtnClose);
            imgbtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conceal();
                }
            });
            imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, _activity.getString(R.string.general_btn_close), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.START, imgbtnClose.getLeft(), imgbtnClose.getBottom());
                    toast.show();
                    return true;
                }
            });

            if (_favoritePlacesFragment == null) {
                _favoritePlacesFragment = new FavoritePlacesFragment();
                _activity.getFragmentManager().beginTransaction().replace(R.id.flFavContent, _favoritePlacesFragment, UIConsts.Fragments.FAVORITE_PLACES_FRAGMENT_TAG).commit();
            }

        }

        /**
         * Checks to restore the state of the search mode.
         */
        private void checkToRestore() {
            if (!_IsRevealed)
                return;

            // Restores the StatusBar search color (works only for API 21+):
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                if (_selectionMode != null && _selectionMode.isInSelectionMode())
                    window.setStatusBarColor(_activity.getResources().getColor(R.color.colorPrimaryDarker));
                else
                    window.setStatusBarColor(_activity.getResources().getColor(R.color.colorPrimaryDark));
            }

            this.revealNoAnimation();
            _layoutFavoritesContainer.setVisibility(View.VISIBLE);

            if (_selectionMode != null)
                _selectionMode.restoreSelection(_activity, (FrameLayout) _layoutFavoritesContainer.findViewById(R.id.layoutSelectionContainer));

            if (_callback != null)
                _callback.onRevealed();
        }

        /**
         * Clears all the local variables.
         */
        private void clearAllLocals() {
            _favoritePlacesFragment = null;
            _layoutFavoritesRevealContainer = null;
            _layoutFavoritesContainer = null;
            _selectionMode = null;
        }

        //endregion

    }

    /**
     * Represents the callbacks for the reveal helper.
     */
    public static abstract class RevealCallback {

        /**
         * Event occurs before the reveal enters.
         */
        public void onReveal() {
        }

        /**
         * Event occurs after the reveal is finished.
         */
        public void onRevealed() {
        }

        /**
         * Event occurs before the conceal enters.
         */
        public void onConceal() {
        }

        /**
         * Event occurs after the conceal is finished.
         */
        public void onConcealed() {
        }

    }

    /**
     * Represents a selection mode for the favorites.
     */
    public class SelectionMode {

        //region Private Members

        /**
         * Holds the activity owner.
         */
        private Activity _activity;

        /**
         * Holds the database interactions object.
         */
        private DBHandler _repository;

        /**
         * Holds the parent layout to populate the selection action bar.
         */
        private FrameLayout _parentLayout;

        /**
         * Holds an indicator indicating whether the search is in selection mode or not.
         */
        private boolean _IsInSelectionMode;

        /**
         * Holds the TextView control for the selected title.
         */
        private TextView _txtSelected;

        /**
         * Holds the adapter owner of the selection mode.
         */
        private FavoritePlacesAdapter _adapter;

        /**
         * Holds a list of all the items can be selected.
         */
        private ArrayList<Place> _items;

        /**
         * Holds the selection list of places.
         */
        private ArrayList<Place> _selection = new ArrayList<>();

        //endregion

        //region C'tors

        /**
         * Initializes a new instance of a selection mode for the favorites.
         */
        private SelectionMode() {
            // Disable public initialization.
        }

        /**
         * Initializes a new instance of a selection mode for the favorites.
         *
         * @param activity     The activity owner.
         * @param parentLayout The parent layout to populate the selection action bar.
         * @param adapter      The adapter owner of the selection.
         */
        SelectionMode(Activity activity, FrameLayout parentLayout, FavoritePlacesAdapter adapter) {
            this._activity = activity;
            this._parentLayout = parentLayout;
            this._adapter = adapter;
            this._items = adapter.getPlaces();
        }

        //endregion

        //region Public API

        /**
         * Indicates whether the search is in selection mode.
         *
         * @return Returns true if the search is in selection mode, otherwise false.
         */
        public boolean isInSelectionMode() {
            return _IsInSelectionMode;
        }

        /**
         * Gets the selection list of items.
         *
         * @return Returns the list of all the selected items.
         */
        public ArrayList<Place> getSelection() {
            return _selection;
        }

        /**
         * Handles the selection of an item, according to the indicator indicating the item is selected/unselected.
         *
         * @param position   The position of the item to select / unselect.
         * @param isSelected The select indicator indicating whether the item is selected or unselected.
         */
        public void itemSelection(int position, boolean isSelected) {
            if (position < 0)
                throw new IllegalArgumentException("position");

            // Checks if the item is selected or unselected:
            if (isSelected)
                _selection.add(_items.get(position)); // Item is selected.
            else
                _selection.remove(_items.get(position)); // Item is unselected.

            // Checks if there are items selected or not:
            if (_selection.size() == 0)
                closeSelectionMode(); // No item is selected, then closes the selection mode.
            else
                _txtSelected.setText(_selection.size() + " " + _activity.getString(R.string.general_msg_selected)); // Sets the title of the actionbar to the number of the selected items.
        }

        //endregion

        //region Local Methods

        /**
         * Enters the search selection mode.
         */
        void enterSelectionMode() {
            if (_IsInSelectionMode)
                return;

            this.enterSelectionModeNoAnimation();

            //region Selection layout animation...

            // Defines the point for the clipping circle:
            int revealX = _parentLayout.getWidth() / 2;
            int revealY = _parentLayout.getHeight() + 500;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(revealX, revealY);

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_parentLayout, revealX, revealY, 500, revealRadius);
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                // Fade In for API below 21:
                _parentLayout.setAlpha(0);
                anim = ObjectAnimator.ofFloat(_parentLayout, "alpha", 1);
            }
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDark), _activity.getResources().getColor(R.color.colorPrimaryDarker));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (isRevealed())
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(500);
                colorAnimation.start();
            }

            //endregion
        }

        /**
         * Closes the search selection mode.
         */
        void closeSelectionMode() {
            _IsInSelectionMode = false;
            _adapter.notifyItemRangeChanged(0, _items.size());
            _selection.clear();
            _items = new ArrayList<>();

            //region Selection layout animation...

            // Defines the point for the clipping circle:
            int revealX = _parentLayout.getWidth() / 2;
            int revealY = -500;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(revealX, revealY);

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_parentLayout, revealX, revealY, revealRadius, 500);
                anim.setInterpolator(new DecelerateInterpolator());
            } else {
                // Fade Out for API below 21:
                _parentLayout.setAlpha(1);
                anim = ObjectAnimator.ofFloat(_parentLayout, "alpha", 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _parentLayout.setVisibility(View.INVISIBLE);
                }
            });
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDarker), _activity.getResources().getColor(R.color.colorPrimaryDark));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (isRevealed())
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(500);
                colorAnimation.setStartDelay(200);
                colorAnimation.setInterpolator(new DecelerateInterpolator());
                colorAnimation.start();
            }

            //endregion
        }

        /**
         * Enters the selection mode, with no animation.
         */
        void enterSelectionModeNoAnimation() {
            this.initSelectionModeLayoutControls();

            _parentLayout.setVisibility(View.VISIBLE);
            _IsInSelectionMode = true;
            _adapter.notifyDataSetChanged();
        }

        /**
         * Restores the selection.
         *
         * @param activity     The activity owner.
         * @param parentLayout The parent layout to populate the selection action bar.
         */
        void restoreSelection(@NonNull Activity activity, @NonNull FrameLayout parentLayout) {
            if (!_IsInSelectionMode)
                return;

            this._activity = activity;
            this._parentLayout = parentLayout;

            _IsInSelectionMode = false;
            this.enterSelectionModeNoAnimation();
            _txtSelected.setText(_selection.size() + " " + _activity.getString(R.string.general_msg_selected)); // Sets the title of the actionbar to the number of the selected items.
        }

        //endregion

        //region Private Methods

        /**
         * Initializes all the selection mode layout control views.
         */
        private void initSelectionModeLayoutControls() {

            _repository = DBHandler.getInstance(_activity);

            _txtSelected = (TextView) _parentLayout.findViewById(txtSelected);

            Toolbar toolbar = (Toolbar) _parentLayout.findViewById(R.id.toolbarSearchSelection);
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_favorites_selection);

            MenuItem actionUnfavorite = toolbar.getMenu().findItem(R.id.actionUnfavorite);
            actionUnfavorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionUnfavorite();
                    return true;
                }
            });
            MenuItem actionShare = toolbar.getMenu().findItem(R.id.actionShare);
            actionShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionShare();
                    return true;
                }
            });
            MenuItem actionSelectAll = toolbar.getMenu().findItem(R.id.actionSelectAll);
            actionSelectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionSelectAll();
                    return true;
                }
            });

            final ImageButton imgbtnClose = (ImageButton) _parentLayout.findViewById(R.id.imgbtnClose);
            imgbtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeSelectionMode();
                }
            });
            imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, R.string.search_selection_exit, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.START, imgbtnClose.getLeft(), imgbtnClose.getBottom());
                    toast.show();
                    return true;
                }
            });

        }

        /**
         * Method procedure for menu action: "Unfavorite".
         */
        private void actionUnfavorite() {
            if (!_IsInSelectionMode)
                throw new IllegalStateException("Selection mode is required.");
            if (_selection.size() < 1)
                throw new IllegalStateException("No item is selected.");

            _cache.clear(); // Clears the cached places.

            // Each place is unfavorited and cached (to undo the action in the future):
            for (Place place : _selection) {
                _cache.add(place);
                _repository.places.unfavorite(place.getId());

                int position = _adapter.getPlaces().indexOf(place);
                _adapter.getPlaces().remove(place);
                _adapter.notifyItemRemoved(position);
            }

            // Displays a Snackbar with an action to restore the unfavorited places list:
            final Snackbar sbUnfav = Snackbar.make(_activity.findViewById(R.id.coordinator), _selection.size() + " " + _activity.getString(R.string.general_msg_unfavorited), 10000);
            sbUnfav.setAction(_activity.getString(R.string.general_btn_undo).toUpperCase(), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sbUnfav.setDuration(1); // Closes the Snackbar nicely with transition, unlike the evil "dismiss()" which makes a shocking disappearance!
                    // Each place in cache gets favorited:
                    for (Place place : _cache)
                        _repository.places.favorite(place.getId());

                    _currentRelation._favoritePlacesFragment.loadFavoritePlacesList();

                    // Notifies the user the restore completed:
                    SnackbarManager.with(_activity).make(_activity.findViewById(R.id.coordinator), _cache.size() + " " + _activity.getString(R.string.general_msg_favorited), Snackbar.LENGTH_LONG).show();

                    _cache.clear(); // Clears the cached places.
                    _adapter.notifyDataSetChanged();
                    _currentRelation._favoritePlacesFragment.displayListState();
                }
            }).setActionTextColor(_activity.getResources().getColor(R.color.colorAccent));
            SnackbarManager.with(_activity).set(sbUnfav).show();

            _adapter.notifyItemRangeChanged(0, _adapter.getPlaces().size() + _selection.size());

            // Closes the selection mode:
            this.closeSelectionMode();
            _currentRelation._favoritePlacesFragment.displayListState();
        }

        /**
         * Method procedure for menu action: "Share".
         */
        private void actionShare() {
            if (!_IsInSelectionMode)
                throw new IllegalStateException("Selection mode is required.");
            if (_selection.size() < 1)
                throw new IllegalStateException("No item is selected.");

            StringBuilder share = new StringBuilder();

            // Each place to share gets added and formatted for one string:
            for (Place place : _selection) {
                share.append(place.getName())
                        .append("\n")
                        .append(place.getAddress() != null ? place.getAddress() : (place.getVicinity() != null ? place.getVicinity() : ""))
                        .append("\n")
                        .append(GooglePlacesConsts.Urls.GOOGLE_PLACE_WEB_DISPLAY).append(Uri.encode(place.getName())).append("/@").append(place.getLocation().latitude).append(",").append(place.getLocation().longitude).append(",18z/")
                        .append("\n\n");
            }

            // Starts the share intent:
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, share.substring(0, share.length() - 2));
            sendIntent.setType("text/plain");
            _activity.startActivity(Intent.createChooser(sendIntent, _activity.getResources().getText(R.string.general_msg_share_places)));

            // Closes the selection mode:
            this.closeSelectionMode();
        }

        /**
         * Method procedure for menu action: "Select All".
         */
        private void actionSelectAll() {
            if (!_IsInSelectionMode)
                throw new IllegalStateException("Selection mode is required.");

            // Checks if there are items not selected:
            if (_items.size() > _selection.size()) {
                _selection.clear();

                for (int i = 0; i < _items.size(); i++)
                    this.itemSelection(i, true);

                _adapter.notifyDataSetChanged();
            }
        }

        //endregion

    }

    //endregion

}
