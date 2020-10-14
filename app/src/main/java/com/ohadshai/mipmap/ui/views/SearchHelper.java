package com.ohadshai.mipmap.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.adapters.SearchPlacesAdapter;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Place;
import com.ohadshai.mipmap.entities.PlaceType;
import com.ohadshai.mipmap.entities.SearchInfo;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.dialogs.PlaceTypeSelectDialog;
import com.ohadshai.mipmap.ui.dialogs.SearchRadiusPickerDialog;
import com.ohadshai.mipmap.ui.fragments.SearchHistoryFragment;
import com.ohadshai.mipmap.ui.fragments.SearchPlacesFragment;
import com.ohadshai.mipmap.utils.Utils;
import com.ohadshai.mipmap.utils.web_services.google_places.GooglePlacesConsts;

import java.util.ArrayList;

import static com.ohadshai.mipmap.R.id.txtSelected;

/**
 * Represents a helper for handling the search action.
 * Created by Ohad on 12/27/2016.
 */
public class SearchHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    //region Private Members

    /**
     * Holds a constant for the string of the icons color.
     */
    private static final String ICONS_COLOR = "#219FD1";

    /**
     * Holds the instance of the "SearchHelper", in order to implement a singleton manner.
     */
    private static SearchHelper _instance;

    /**
     * Holds the current relation for the "SearchHelper".
     */
    private SearchHelper.Relation _currentRelation;

    /**
     * Holds the search information object, which holds all the search fields.
     */
    private SearchInfo _searchInfo;

    /**
     * Holds a list of cached places.
     */
    private ArrayList<Place> _cache = new ArrayList<>();

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a helper for handling the search action.
     */
    private SearchHelper() {
        // Disables the initialization of a new instance from the outside,
        // in order to implement a singleton manner.
    }

    //endregion

    //region Events

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Utils.Preferences.Keys.UNIT_OF_LENGTH)) {
            int value = Integer.valueOf(sharedPreferences.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
            // Resets the default value for the search radius:
            if (value == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                _searchInfo.setRadius(1000);
            else if (value == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                _searchInfo.setRadius(1610);
        }
    }

    //endregion

    //region Public Static API

    /**
     * Gets the "SearchHelper" instance of the application, or creates a new instance if null.
     *
     * @return Returns the "SearchHelper" instance of the application.
     */
    public static SearchHelper getInstance() {
        if (_instance == null)
            _instance = new SearchHelper();

        return _instance;
    }

    /**
     * Gets the "SearchHelper" instance to help the activity.
     *
     * @param activity The activity owner.
     * @return Returns the "SearchHelper" instance.
     */
    public static SearchHelper.Relation with(@NonNull AppCompatActivity activity) {
        return SearchHelper.getInstance().initializeRelation(activity);
    }

    /**
     * Indicates whether in search mode or not.
     *
     * @return Returns true if in search mode, otherwise false.
     */
    public static boolean isInSearchMode() {
        return _instance != null && _instance._currentRelation != null && _instance._currentRelation._IsInSearchMode;
    }

    /**
     * Indicates whether the search helper is in selection mode or not.
     *
     * @return Returns true if the search is in selection mode, otherwise false.
     */
    public static boolean isInSelectionMode() {
        return _instance != null && _instance._currentRelation != null && _instance._currentRelation._selectionMode != null && _instance._currentRelation._selectionMode.isInSelectionMode();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes the relation for the "SearchHelper", to relate the provided activity.
     *
     * @param activity The activity to relate the "SearchHelper".
     * @return Returns a relation object between the "SearchHelper" to the activity.
     */
    private SearchHelper.Relation initializeRelation(@NonNull AppCompatActivity activity) {
        // Initializes a new instance if it's a new relation with a new activity:
        if (_currentRelation == null)
            _currentRelation = new SearchHelper.Relation(activity);
        else if (_currentRelation._activity != activity)
            _currentRelation.setActivity(activity);

        return _currentRelation;
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a relation between the search helper and an activity.
     */
    public class Relation implements SearchView.OnQueryTextListener {

        //region Private Members

        /**
         * Holds the activity owner.
         */
        private AppCompatActivity _activity;

        /**
         * Holds the database interactions object.
         */
        private DBHandler _repository;

        //region [Search] Related

        /**
         * Holds the search layout container, which will contain the inflated search layout.
         */
        private FrameLayout _layoutSearchContainer;

        /**
         * Holds an indicator indicating whether the search mode is entered or not.
         */
        private boolean _IsInSearchMode;

        /**
         * Holds the callback for the search enter.
         */
        private SearchHelperCallback _callback;

        /**
         * Holds the SearchView control for the main search.
         */
        private SearchView _searchMain;

        /**
         * Holds the last search info object.
         */
        private SearchInfo _lastSearchInfo;

        //endregion

        //region [Search Options] Related

        /**
         * Holds an indicator indicating whether the search is in the search options or not.
         */
        private boolean _IsInSearchOptions;

        /**
         * Holds the search options layout container, which will contain the inflated search options layout.
         */
        private FrameLayout _layoutSearchOptionsContainer;

        /**
         * Holds the MenuItem control for the search options button.
         */
        private MenuItem _searchOptionsMenuItem;

        //endregion

        /**
         * Holds the selection mode interaction object.
         */
        private SearchHelper.SearchSelectionMode _selectionMode;

        /**
         * Holds the control for the search places fragment.
         */
        private SearchPlacesFragment _searchPlacesFragment;

        //endregion

        //region C'tor

        /**
         * Initializes a new instance of a relation between the search helper and an activity
         *
         * @param activity The activity to relate the "SearchHelper".
         */
        public Relation(@NonNull AppCompatActivity activity) {
            this.setActivity(activity);
            this._repository = DBHandler.getInstance(activity);

            if (_searchInfo == null)
                _searchInfo = SearchInfo.createDefault(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(_activity).getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km))));
        }

        //endregion

        //region Events

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            this.search(query);
            return true;
        }

        //endregion

        //region Public API

        /**
         * Initializes the relation (this must be called in the OnCreate() event of the activity).
         * This also restores the state of the search in cases of activity config changes.
         *
         * @param callback The callback methods for the search helper.
         */
        public void initialize(SearchHelper.SearchHelperCallback callback) {
            this._callback = callback;

            this.checkToRestore();
        }

        /**
         * Enters the search mode.
         */
        public void enter() {
            if (_IsInSearchMode)
                return;

            this.enterNoAnimation();

            //region Search layout animation...

            // Gets the search button view in the activity (the view who enters the search):
            View searchButton = _activity.findViewById(R.id.actionSearch);
            if (searchButton == null)
                throw new NullPointerException("Missing view in the activity: \"actionSearch\".");

            // Gets the location of the search button on the screen:
            int[] searchBtnLocation = new int[2];
            searchButton.getLocationOnScreen(searchBtnLocation);

            // Defines the point for the clipping circle:
            int revealX = searchBtnLocation[0] + (searchButton.getWidth() / 2);
            int revealY = searchButton.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(_layoutSearchContainer.getWidth(), _layoutSearchContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutSearchContainer, revealX, revealY, 0, revealRadius);
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                // Fade In for API below 21:
                _layoutSearchContainer.setAlpha(0);
                anim = ObjectAnimator.ofFloat(_layoutSearchContainer, "alpha", 1);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (_callback != null)
                        _callback.onSearchEntered();
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

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDark), _activity.getResources().getColor(R.color.colorSearchStatusBar));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (_IsInSearchMode)
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(800);
                colorAnimation.start();

            }

            //endregion
        }

        /**
         * Exits the search mode.
         */
        public void exit() {
            if (!_IsInSearchMode)
                return;

            if (_callback != null)
                _callback.onSearchExit();

            _IsInSearchMode = false;
            _IsInSearchOptions = false;
            _lastSearchInfo = null;

            //region Search layout animation...

            // Gets the search button view in the activity (the view who exits the search):
            View searchButton = _activity.findViewById(R.id.actionSearch);
            if (searchButton == null)
                throw new NullPointerException("Missing view in the activity: \"actionSearch\".");

            // Gets the location of the search button on the screen:
            int[] searchBtnLocation = new int[2];
            searchButton.getLocationOnScreen(searchBtnLocation);

            // Defines the point for the clipping circle:
            int revealX = searchBtnLocation[0] + (searchButton.getWidth() / 2);
            int revealY = searchButton.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(_layoutSearchContainer.getWidth(), _layoutSearchContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutSearchContainer, revealX, revealY, revealRadius, 0);
                anim.setInterpolator(new DecelerateInterpolator());
            } else {
                // Fade Out for API below 21:
                _layoutSearchContainer.setAlpha(1);
                anim = ObjectAnimator.ofFloat(_layoutSearchContainer, "alpha", 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _layoutSearchContainer.setVisibility(View.INVISIBLE);

                    if (_callback != null)
                        _callback.onSearchExited();

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

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorSearchStatusBar), _activity.getResources().getColor(R.color.colorPrimaryDark));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(300);
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
         * Exits the search mode with no animation.
         */
        public void exitNoAnimation() {
            if (!_IsInSearchMode)
                return;

            if (_callback != null)
                _callback.onSearchExit();

            _IsInSearchMode = false;
            _IsInSearchOptions = false;
            _lastSearchInfo = null;

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(_activity.getResources().getColor(R.color.colorPrimaryDark));
            }

            //endregion

            _layoutSearchContainer.setVisibility(View.INVISIBLE);

            if (_callback != null)
                _callback.onSearchExited();

            this.clearAllLocals();

            // Checks if any snackbar is showing in this area, closes it:
            if (SnackbarManager.with(_activity).isShowing())
                SnackbarManager.with(_activity).dismiss();

            // Unlocks the orientation for devices below API 21:
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                Utils.Orientation.unlock(_activity);
        }

        /**
         * Loads the search with the provided query text, then searches the query.
         *
         * @param queryText The query text to load the search with.
         */
        public void loadQueryAndSearch(String queryText) {
            _searchMain.setQuery(queryText, true);
        }

        /**
         * Searches a query text, and opens a fragment to display the results.
         *
         * @param query The query text of the search.
         */
        public void search(String query) {
            String queryFix = query.trim().toLowerCase();

            // Updates final fields for the search info object:
            _searchInfo.setText(queryFix);
            _searchInfo.setLocation(Utils.LatLng.parse(PreferenceManager.getDefaultSharedPreferences(_activity).getString(Utils.Preferences.Keys.LAST_LOCATION, "")));

            // Checks for a new unique search:
            if (_searchInfo.equals(_lastSearchInfo)) {
                return;
            }
            // Checks if it's a Nearby Search and there's no location:
            else if (_searchInfo.isNearby() && _searchInfo.getLocation() == null) {
                Toast toast = Toast.makeText(_activity, R.string.search_validation_no_location, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, _layoutSearchContainer.findViewById(R.id.toolbarSearch).getBottom());
                toast.show();
                return;
            }
            // Checks for valid search mode (Text Search has text, or Nearby Search is enabled):
            else if (!_searchInfo.isNearby() && queryFix.equals("")) {
                Toast toast = Toast.makeText(_activity, R.string.search_invalid, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, _layoutSearchContainer.findViewById(R.id.toolbarSearch).getBottom());
                toast.show();
                return;
            }

            _searchMain.clearFocus();
            _layoutSearchContainer.findViewById(R.id.toolbarSearch).requestFocus();

            if (!queryFix.equals(""))
                _repository.searchHistory.save(queryFix); // Saves the query to the search history.

            // Saves this search info to cache:
            _lastSearchInfo = SearchInfo.copy(_searchInfo);

            if (_searchPlacesFragment != null)
                ((FrameLayout) _activity.findViewById(R.id.layoutContent)).removeAllViews();

            // Navigates to the search places fragment, to run the search:
            _searchPlacesFragment = new SearchPlacesFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(UIConsts.Intent.SEARCH_INFO_KEY, _searchInfo);
            _searchPlacesFragment.setArguments(bundle);
            FragmentTransaction transaction = _activity.getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_left_in, R.animator.slide_left_out);
            transaction.replace(R.id.layoutContent, _searchPlacesFragment, UIConsts.Fragments.SEARCH_PLACES_FRAGMENT_TAG);
            transaction.commit();
        }

        /**
         * Handles back press to close states in the search helper.
         *
         * @return Returns an indicator indicating whether the back press was handled by the helper or not.
         */
        public boolean handleBackPress() {
            if (_selectionMode != null && _selectionMode._IsInSelectionMode) {
                _selectionMode.closeSelectionMode();
                return true;
            } else if (_IsInSearchOptions) {
                this.closeSearchOptions();
                return true;
            } else if (_IsInSearchMode) {
                this.exit();
                return true;
            } else {
                return false;
            }
        }

        /**
         * Enters the search selection mode.
         *
         * @param adapter The adapter owner of the selection.
         * @return Returns the selection mode object.
         */
        public SearchHelper.SearchSelectionMode enterSelectionMode(@NonNull SearchPlacesAdapter adapter) {
            if (_selectionMode != null && _selectionMode._IsInSelectionMode)
                return _selectionMode;

            _selectionMode = new SearchSelectionMode(_activity, (FrameLayout) _layoutSearchContainer.findViewById(R.id.layoutSelectionContainer), adapter, _IsInSearchOptions);
            _selectionMode.enterSelectionMode();
            return _selectionMode;
        }

        //endregion

        //region Private Methods

        /**
         * Sets the activity owner to relate the "SearchHelper".
         *
         * @param activity The activity to relate the "SearchHelper".
         */
        void setActivity(@NonNull AppCompatActivity activity) {
            this._activity = activity;
        }

        /**
         * Enters the search mode, with no animation.
         */
        private void enterNoAnimation() {
            if (_callback != null)
                _callback.onSearchEnter();

            this.initSearchLayoutControls();

            _layoutSearchContainer.setVisibility(View.VISIBLE);
            _IsInSearchMode = true;

            // Locks the orientation for devices below API 21:
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                Utils.Orientation.lock(_activity);
        }

        /**
         * Initializes all the search layout control views.
         */
        private void initSearchLayoutControls() {

            PreferenceManager.getDefaultSharedPreferences(_activity).registerOnSharedPreferenceChangeListener(SearchHelper.this);

            // Gets the search layout container:
            _layoutSearchContainer = (FrameLayout) _activity.findViewById(R.id.layoutSearchContainer);

            final ImageButton imgbtnNavigateUp = (ImageButton) _layoutSearchContainer.findViewById(R.id.imgbtnExitSearch);
            imgbtnNavigateUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exit();
                }
            });
            imgbtnNavigateUp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, _activity.getString(R.string.general_btn_exit_search), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.START, imgbtnNavigateUp.getLeft(), imgbtnNavigateUp.getBottom());
                    toast.show();
                    return true;
                }
            });

            Toolbar toolbarSearch = (Toolbar) _layoutSearchContainer.findViewById(R.id.toolbarSearch);
            toolbarSearch.getMenu().clear();

            MenuItem searchMenuItem = toolbarSearch.getMenu().add(0, 122, 0, R.string.search_nearby_or_text);
            Drawable searchNearbyIcon = searchMenuItem.setIcon(R.mipmap.ic_search_white).getIcon();
            searchNearbyIcon.mutate().setColorFilter(Color.parseColor(ICONS_COLOR), PorterDuff.Mode.SRC_ATOP);
            searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    search(_searchMain.getQuery().toString());
                    return true;
                }
            });

            _searchOptionsMenuItem = toolbarSearch.getMenu().add(0, 123, 1, R.string.search_options);
            Drawable searchOptionsIcon = _searchOptionsMenuItem.setIcon(R.mipmap.ic_tune_black).getIcon();
            searchOptionsIcon.mutate().setColorFilter(Color.parseColor(ICONS_COLOR), PorterDuff.Mode.SRC_ATOP);
            _searchOptionsMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            _searchOptionsMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    enterSearchOptions();
                    return true;
                }
            });

            _searchMain = (SearchView) _layoutSearchContainer.findViewById(R.id.searchMain);
            _searchMain.setIconified(false);
            _searchMain.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackgroundColor(Color.TRANSPARENT); // Hides the search underline.
            _searchMain.setOnQueryTextListener(this);
            _searchMain.setQuery("", false);

            if (_searchPlacesFragment == null)
                _activity.getFragmentManager().beginTransaction().replace(R.id.layoutContent, new SearchHistoryFragment(), UIConsts.Fragments.SEARCH_HISTORY_FRAGMENT_TAG).commit();

        }

        /**
         * Checks to restore the state of the search mode.
         */
        private void checkToRestore() {
            if (!_IsInSearchMode)
                return;

            // Restores the StatusBar search color (works only for API 21+):
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                if (_IsInSearchOptions || (_selectionMode != null && _selectionMode.isInSelectionMode()))
                    window.setStatusBarColor(_activity.getResources().getColor(R.color.colorPrimaryDarker));
                else
                    window.setStatusBarColor(_activity.getResources().getColor(R.color.colorSearchStatusBar));
            }

            this.enterNoAnimation();

            if (_IsInSearchOptions)
                this.enterSearchOptionsNoAnimation();

            if (_searchPlacesFragment != null)
                _activity.getFragmentManager().beginTransaction().replace(R.id.layoutContent, _searchPlacesFragment, UIConsts.Fragments.SEARCH_PLACES_FRAGMENT_TAG).commit();

            if (_selectionMode != null)
                _selectionMode.restoreSelection(_activity, (FrameLayout) _layoutSearchContainer.findViewById(R.id.layoutSelectionContainer));

            if (_callback != null)
                _callback.onSearchEntered();
        }

        //region [Search Options] Related

        /**
         * Enters the search options.
         */
        private void enterSearchOptions() {
            if (_IsInSearchOptions)
                return;

            this.enterSearchOptionsNoAnimation();

            //region Search Options layout animation...

            // Gets the button view (the view who enters the search options):

            // Defines the point for the clipping circle:
            int revealX = _layoutSearchOptionsContainer.getWidth() - 40;
            int revealY = _layoutSearchOptionsContainer.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(_layoutSearchOptionsContainer.getWidth(), _layoutSearchOptionsContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutSearchOptionsContainer, revealX, revealY, 0, revealRadius);
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                // Fade In for API below 21:
                _layoutSearchOptionsContainer.setAlpha(0);
                anim = ObjectAnimator.ofFloat(_layoutSearchOptionsContainer, "alpha", 1);
            }
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = _activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorSearchStatusBar), _activity.getResources().getColor(R.color.colorPrimaryDarker));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (_IsInSearchMode)
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
         * Closes the search options.
         */
        private void closeSearchOptions() {
            if (!_IsInSearchOptions)
                return;

            _IsInSearchOptions = false;

            //region Search layout animation...

            // Defines the point for the clipping circle:
            int revealX = _layoutSearchOptionsContainer.getWidth() - 40;
            int revealY = _layoutSearchOptionsContainer.getHeight() / 2;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(_layoutSearchOptionsContainer.getWidth(), _layoutSearchOptionsContainer.getHeight());

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_layoutSearchOptionsContainer, revealX, revealY, revealRadius, 0);
                anim.setInterpolator(new DecelerateInterpolator());
            } else {
                // Fade Out for API below 21:
                _layoutSearchOptionsContainer.setAlpha(1);
                anim = ObjectAnimator.ofFloat(_layoutSearchOptionsContainer, "alpha", 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _layoutSearchOptionsContainer.setVisibility(View.INVISIBLE);
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

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDarker), _activity.getResources().getColor(R.color.colorSearchStatusBar));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (_IsInSearchMode)
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(400);
                colorAnimation.start();
            }

            //endregion
        }

        /**
         * Enters the search options, with no animation.
         */
        private void enterSearchOptionsNoAnimation() {

            this.initSearchOptionsLayoutControls();

            _layoutSearchOptionsContainer.setVisibility(View.VISIBLE);
            _IsInSearchOptions = true;
        }

        /**
         * Initializes all the search options layout control views.
         */
        private void initSearchOptionsLayoutControls() {

            // Gets the search options layout:
            _layoutSearchOptionsContainer = (FrameLayout) _layoutSearchContainer.findViewById(R.id.layoutSearchOptionsContainer);

            final ImageButton imgbtnClose = (ImageButton) _layoutSearchOptionsContainer.findViewById(R.id.imgbtnClose);
            imgbtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeSearchOptions();
                }
            });
            imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, R.string.general_btn_close, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, imgbtnClose.getRight(), imgbtnClose.getBottom());
                    toast.show();
                    return true;
                }
            });

            //region Search Option: [Nearby]

            final View layoutSearchOptionNearby = _layoutSearchOptionsContainer.findViewById(R.id.layoutSearchOptionNearby);
            final ImageView imgSearchOptionNearby = (ImageView) _layoutSearchOptionsContainer.findViewById(R.id.imgSearchOptionNearby);
            final TextView lblSearchOptionNearby = (TextView) _layoutSearchOptionsContainer.findViewById(R.id.lblSearchOptionNearby);
            layoutSearchOptionNearby.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_searchInfo.isNearby()) {
                        _searchInfo.setIsNearby(false);
                        layoutSearchOptionNearby.setBackgroundResource(R.drawable.search_option_unselected_background);
                        imgSearchOptionNearby.setColorFilter(Color.WHITE);
                        lblSearchOptionNearby.setTextColor(Color.WHITE);
                    } else {
                        _searchInfo.setIsNearby(true);
                        layoutSearchOptionNearby.setBackgroundResource(R.drawable.search_option_selected_background);
                        imgSearchOptionNearby.setColorFilter(ContextCompat.getColor(_activity, R.color.colorPrimaryDarker));
                        lblSearchOptionNearby.setTextColor(Color.parseColor("#666666"));
                    }
                }
            });
            layoutSearchOptionNearby.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, R.string.search_options_nearby_explanation, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, layoutSearchOptionNearby.getBottom());
                    toast.show();
                    return true;
                }
            });

            // Shows the button state after initialization:
            if (_searchInfo.isNearby()) {
                layoutSearchOptionNearby.setBackgroundResource(R.drawable.search_option_selected_background);
                imgSearchOptionNearby.setColorFilter(ContextCompat.getColor(_activity, R.color.colorPrimaryDarker));
                lblSearchOptionNearby.setTextColor(Color.parseColor("#666666"));
            } else {
                layoutSearchOptionNearby.setBackgroundResource(R.drawable.search_option_unselected_background);
                imgSearchOptionNearby.setColorFilter(Color.WHITE);
                lblSearchOptionNearby.setTextColor(Color.WHITE);
            }

            //endregion

            //region Search Option: [Radius]

            final View layoutSearchOptionRadius = _layoutSearchOptionsContainer.findViewById(R.id.layoutSearchOptionRadius);
            layoutSearchOptionRadius.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Opens the search radius dialog to let the user select the radius:
                    SearchRadiusPickerDialog dialog = new SearchRadiusPickerDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt(UIConsts.Bundles.SEARCH_RADIUS, _searchInfo.getRadius());
                    dialog.setArguments(bundle);
                    dialog.setOnPositiveResultListener(new SearchRadiusPickerDialog.PositiveResultListener() {
                        @Override
                        public void onPositiveResult(int radius) {
                            // Saves the selected radius to the search info object, then display the value to the button text:
                            _searchInfo.setRadius(radius);

                            int unitOfLength = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(_activity).getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
                            if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                                ((TextView) _layoutSearchOptionsContainer.findViewById(R.id.txtSearchRadius)).setText(Utils.Units.displayMeters(radius, _activity));
                            else if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                                ((TextView) _layoutSearchOptionsContainer.findViewById(R.id.txtSearchRadius)).setText(Utils.Units.displayYards((int) (radius * Utils.Units.M_TO_YD), _activity));

                        }
                    });
                    dialog.show(_activity.getFragmentManager(), UIConsts.Fragments.SEARCH_RADIUS_DIALOG_TAG);
                }
            });
            layoutSearchOptionRadius.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, R.string.search_options_radius, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, layoutSearchOptionRadius.getBottom());
                    toast.show();
                    return true;
                }
            });

            // Shows the button state after initialization:
            int unitOfLength = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(_activity).getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, String.valueOf(Utils.Preferences.Values.UNIT_OF_LENGTH_Km)));
            if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                ((TextView) _layoutSearchOptionsContainer.findViewById(R.id.txtSearchRadius)).setText(Utils.Units.displayMeters(_searchInfo.getRadius(), _activity));
            else if (unitOfLength == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                ((TextView) _layoutSearchOptionsContainer.findViewById(R.id.txtSearchRadius)).setText(Utils.Units.displayYards((int) (_searchInfo.getRadius() * Utils.Units.M_TO_YD), _activity));

            //endregion

            //region Search Option: [Place Type]

            final View layoutSearchOptionPlaceType = _layoutSearchOptionsContainer.findViewById(R.id.layoutSearchOptionPlaceType);
            final TextView txtSearchOptionPlaceType = (TextView) _layoutSearchOptionsContainer.findViewById(R.id.txtSearchOptionPlaceType);
            layoutSearchOptionPlaceType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Opens the place type select dialog to let the user select the place type:
                    PlaceTypeSelectDialog dialog = new PlaceTypeSelectDialog();
                    if (_searchInfo.getPlaceType() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(UIConsts.Bundles.PLACE_TYPE, _searchInfo.getPlaceType());
                        dialog.setArguments(bundle);
                    }
                    dialog.setResultListener(new PlaceTypeSelectDialog.ResultListener() {
                        @Override
                        public void onPositiveResult(PlaceType placeType) {
                            _searchInfo.setPlaceType(placeType);
                            layoutSearchOptionPlaceType.setBackgroundResource(R.drawable.search_option_selected_background);
                            txtSearchOptionPlaceType.setText(placeType.getName());
                            txtSearchOptionPlaceType.setTextColor(Color.parseColor("#666666"));
                        }

                        @Override
                        public void onReset() {
                            _searchInfo.setPlaceType(null);
                            layoutSearchOptionPlaceType.setBackgroundResource(R.drawable.search_option_unselected_background);
                            txtSearchOptionPlaceType.setText(R.string.search_options_place_type);
                            txtSearchOptionPlaceType.setTextColor(Color.WHITE);
                        }
                    });
                    dialog.show(_activity.getFragmentManager(), UIConsts.Fragments.SEARCH_PLACE_TYPE_SELECT_DIALOG_TAG);
                }
            });
            layoutSearchOptionPlaceType.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast toast = Toast.makeText(_activity, R.string.search_options_place_type, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, layoutSearchOptionPlaceType.getBottom());
                    toast.show();
                    return true;
                }
            });

            // Shows the button state after initialization:
            if (_searchInfo.getPlaceType() != null) {
                layoutSearchOptionPlaceType.setBackgroundResource(R.drawable.search_option_selected_background);
                txtSearchOptionPlaceType.setText(_searchInfo.getPlaceType().getName());
                txtSearchOptionPlaceType.setTextColor(Color.parseColor("#666666"));
            } else {
                layoutSearchOptionPlaceType.setBackgroundResource(R.drawable.search_option_unselected_background);
                txtSearchOptionPlaceType.setText(R.string.search_options_place_type);
                txtSearchOptionPlaceType.setTextColor(Color.WHITE);
            }

            //endregion

        }

        //endregion

        /**
         * Clears all the local variables.
         */
        private void clearAllLocals() {
            if (_searchPlacesFragment != null)
                _activity.getFragmentManager().beginTransaction().remove(_searchPlacesFragment).commitAllowingStateLoss();

            SearchPlacesFragment searchPlacesFragment = (SearchPlacesFragment) _activity.getFragmentManager().findFragmentByTag(UIConsts.Fragments.SEARCH_PLACES_FRAGMENT_TAG);
            if (searchPlacesFragment != null)
                _activity.getFragmentManager().beginTransaction().remove(searchPlacesFragment).commitAllowingStateLoss();

            ((FrameLayout) _activity.findViewById(R.id.layoutContent)).removeAllViews();

            _searchPlacesFragment = null;
            _layoutSearchContainer = null;
            _searchMain = null;
            _lastSearchInfo = null;
            _layoutSearchOptionsContainer = null;
            _searchOptionsMenuItem = null;
            _selectionMode = null;
        }

        //endregion

    }

    /**
     * Represents the callbacks for the search helper.
     */
    public static abstract class SearchHelperCallback {

        /**
         * Event occurs when a search result is clicked.
         *
         * @param place The place clicked from the result of the search.
         */
        public void onSearchResultClick(Place place) {
        }

        /**
         * Event occurs before the search enter.
         */
        public void onSearchEnter() {
        }

        /**
         * Event occurs after the search enter is finished.
         */
        public void onSearchEntered() {
        }

        /**
         * Event occurs before the search exit.
         */
        public void onSearchExit() {
        }

        /**
         * Event occurs after the search exit is finished.
         */
        public void onSearchExited() {
        }

    }

    /**
     * Represents a selection mode for the search.
     */
    public class SearchSelectionMode {

        //region Private Members

        /**
         * Holds the activity owner.
         */
        private Activity _activity;

        /**
         * Holds the parent layout to populate the selection action bar.
         */
        private FrameLayout _parentLayout;

        /**
         * Holds an indicator indicating whether the search is in selection mode or not.
         */
        private boolean _IsInSelectionMode;

        /**
         * Holds an indicator indicating whether the search state is in search options or not.
         */
        private boolean _IsInSearchOptions;

        /**
         * Holds the TextView control for the selected title.
         */
        private TextView _txtSelected;

        /**
         * Holds the adapter owner of the selection mode.
         */
        private SearchPlacesAdapter _adapter;

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
         * Initializes a new instance of a selection mode for the search.
         */
        private SearchSelectionMode() {
            // Disable public initialization.
        }

        /**
         * Initializes a new instance of a selection mode for the search.
         *
         * @param activity          The activity owner.
         * @param parentLayout      The parent layout to populate the selection action bar.
         * @param adapter           The adapter owner of the selection.
         * @param isInSearchOptions An indicator indicating whether the search state is in search options or not.
         */
        SearchSelectionMode(@NonNull Activity activity, @NonNull FrameLayout parentLayout, SearchPlacesAdapter adapter, boolean isInSearchOptions) {
            this._activity = activity;
            this._parentLayout = parentLayout;
            this._adapter = adapter;
            this._items = adapter.getPlaces();
            this._IsInSearchOptions = isInSearchOptions;
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

            if (!_IsInSearchOptions) {
                // Works only for API 21+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final Window window = _activity.getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                    ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorSearchStatusBar), _activity.getResources().getColor(R.color.colorPrimaryDarker));
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (isInSearchMode())
                                    window.setStatusBarColor((Integer) animator.getAnimatedValue());
                            }
                        }
                    });
                    colorAnimation.setDuration(500);
                    colorAnimation.start();
                }
            }

            //endregion
        }

        /**
         * Closes the search selection mode.
         */
        void closeSelectionMode() {
            if (!_IsInSelectionMode)
                return;

            _selection.clear();
            _items = new ArrayList<>();
            _IsInSelectionMode = false;
            _adapter.notifyDataSetChanged();
            _currentRelation._searchPlacesFragment.showFabShowInMap();

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

            if (!_IsInSearchOptions) {
                // Works only for API 21+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final Window window = _activity.getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                    ValueAnimator colorAnimation = ValueAnimator.ofArgb(_activity.getResources().getColor(R.color.colorPrimaryDarker), _activity.getResources().getColor(R.color.colorSearchStatusBar));
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (isInSearchMode())
                                    window.setStatusBarColor((Integer) animator.getAnimatedValue());
                            }
                        }
                    });
                    colorAnimation.setDuration(500);
                    colorAnimation.setStartDelay(200);
                    colorAnimation.setInterpolator(new DecelerateInterpolator());
                    colorAnimation.start();
                }
            }

            //endregion

            _instance._currentRelation._selectionMode = null;
        }

        /**
         * Enters the selection mode, with no animation.
         */
        void enterSelectionModeNoAnimation() {
            this.initSelectionModeLayoutControls();

            _currentRelation._searchPlacesFragment.hideFabShowInMap();

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

            _txtSelected = (TextView) _parentLayout.findViewById(txtSelected);

            Toolbar toolbar = (Toolbar) _parentLayout.findViewById(R.id.toolbarSearchSelection);
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_search_selection);

            MenuItem actionFavorite = toolbar.getMenu().findItem(R.id.actionFavorite);
            actionFavorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionFavorite();
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
         * Method procedure for menu action: "Favorite".
         */
        private void actionFavorite() {
            if (!_IsInSelectionMode)
                throw new IllegalStateException("Selection mode is required.");
            if (_selection.size() < 1)
                throw new IllegalStateException("No item is selected.");

            _cache.clear(); // Clears the cached places.

            // Each place is favorited and cached (to undo the action in the future):
            for (int i = _selection.size() - 1; i >= 0; i--) {
                Place place = _selection.get(i);
                _cache.add(place);
                _currentRelation._repository.places.favorite(place.getGooglePlaceId());
            }

            // Displays a Snackbar with an action to restore the favorited places list:
            final Snackbar sbFav = Snackbar.make(_activity.findViewById(R.id.coordinatorAboveBottomSheet), _selection.size() + " " + _activity.getString(R.string.general_msg_favorited), 10000);
            sbFav.setAction(_activity.getString(R.string.general_btn_undo).toUpperCase(), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sbFav.setDuration(1); // Closes the Snackbar nicely with transition, unlike the evil "dismiss()" which makes a shocking disappearance!
                    // Each place in cache gets unfavorited:
                    for (Place place : _cache)
                        _currentRelation._repository.places.unfavorite(place.getGooglePlaceId());

                    // Notifies the user the restore completed:
                    SnackbarManager.with(_activity).make(_activity.findViewById(R.id.coordinatorAboveBottomSheet), _cache.size() + " " + _activity.getString(R.string.general_msg_unfavorited), Snackbar.LENGTH_LONG).show();

                    _cache.clear(); // Clears the cached places.
                }
            }).setActionTextColor(_activity.getResources().getColor(R.color.colorAccent));
            SnackbarManager.with(_activity).set(sbFav).show();

            // Closes the selection mode:
            this.closeSelectionMode();
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
