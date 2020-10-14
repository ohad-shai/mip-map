package com.ohadshai.mipmap.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.ui.UIConsts;
import com.ohadshai.mipmap.ui.dialogs.AboutDialog;
import com.ohadshai.mipmap.utils.AppCompatPreferenceActivity;
import com.ohadshai.mipmap.utils.Utils;

import static com.ohadshai.mipmap.R.xml.preferences;

public class SettingsActivity extends AppCompatPreferenceActivity {

    //region Activity Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsMainFragment()).commit();
        this.setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region Private Methods

    /**
     * Sets the action bar, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // Shows the Up button in the action bar.
        }
    }

    //endregion

    //region Inner Classes

    /**
     * Holds the main fragment of the settings activity.
     */
    public static class SettingsMainFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        //region Private Members

        /**
         * Holds the SharedPreferences control.
         */
        private SharedPreferences _sharedPreferences;

        /**
         * Holds the database interactions object.
         */
        private DBHandler _repository;

        /**
         * Holds the view for the settings main fragment.
         */
        private View _view;

        //endregion

        //region Fragment Events

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(preferences);
            this.initControls();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            _view = super.onCreateView(inflater, container, savedInstanceState);
            return _view;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Utils.Preferences.Keys.UNIT_OF_LENGTH)) {
                // Displays the current "Unit of Length" to the summary of the preference:
                int unitOfLengthValue = Integer.parseInt(sharedPreferences.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, "-1"));
                if (unitOfLengthValue > 0) {
                    if (unitOfLengthValue == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                        findPreference(Utils.Preferences.Keys.UNIT_OF_LENGTH).setSummary(getString(R.string.settings_unit_of_length_km));
                    else if (unitOfLengthValue == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                        findPreference(Utils.Preferences.Keys.UNIT_OF_LENGTH).setSummary(getString(R.string.settings_unit_of_length_miles));
                }
            } else if (key.equals(Utils.Preferences.Keys.LANGUAGE)) {
                // Displays the current "Language" to the summary of the preference:
                ListPreference preference = (ListPreference) findPreference(Utils.Preferences.Keys.LANGUAGE);
                preference.setSummary(preference.getEntry());
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            _sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        //endregion

        //region Private Methods

        /**
         * Initializes all the controls.
         */
        private void initControls() {

            _repository = DBHandler.getInstance(getActivity()); // Initializes the database object.

            // Sets on SharedPreference change listener:
            _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            _sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            //region "Language" - Preference

            // Displays the current "Language" to the summary of the preference:
            ListPreference preference = (ListPreference) findPreference(Utils.Preferences.Keys.LANGUAGE);
            preference.setSummary(preference.getEntry());

            //endregion

            //region "Unit of length" - Preference

            // Displays the current "Unit of Length" to the summary of the preference:
            int unitOfLengthValue = Integer.parseInt(_sharedPreferences.getString(Utils.Preferences.Keys.UNIT_OF_LENGTH, "-1"));
            if (unitOfLengthValue > 0) {
                if (unitOfLengthValue == Utils.Preferences.Values.UNIT_OF_LENGTH_Km)
                    findPreference(Utils.Preferences.Keys.UNIT_OF_LENGTH).setSummary(getString(R.string.settings_unit_of_length_km));
                else if (unitOfLengthValue == Utils.Preferences.Values.UNIT_OF_LENGTH_Miles)
                    findPreference(Utils.Preferences.Keys.UNIT_OF_LENGTH).setSummary(getString(R.string.settings_unit_of_length_miles));
            }

            //endregion

            //region "Erase all favorites" - Preference

            findPreference(Utils.Preferences.Keys.ERASE_ALL_FAVORITES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Displays an AlertDialog, to notify the user this will erase all of his favorites:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.general_msg_are_you_sure));
                    builder.setMessage(getString(R.string.settings_erase_all_favorites_confirm));
                    builder.setNegativeButton(getString(R.string.general_btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton(getString(R.string.general_btn_yes), new DialogInterface.OnClickListener() {
                        @SuppressLint("CommitPrefEdits")
                        public void onClick(DialogInterface dialog, int id) {
                            _repository.places.removeAllFavorites();
                            Snackbar.make(_view, R.string.settings_erase_all_favorites_done, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                    return true;
                }
            });

            //endregion

            //region "Erase all search history" - Preference

            findPreference(Utils.Preferences.Keys.ERASE_SEARCH_HISTORY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Displays an AlertDialog, to notify the user this will erase all of his favorites:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.general_msg_are_you_sure));
                    builder.setMessage(getString(R.string.settings_erase_all_search_history_confirm));
                    builder.setNegativeButton(getString(R.string.general_btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton(getString(R.string.general_btn_yes), new DialogInterface.OnClickListener() {
                        @SuppressLint("CommitPrefEdits")
                        public void onClick(DialogInterface dialog, int id) {
                            _repository.searchHistory.deleteAll();
                            Snackbar.make(_view, R.string.settings_erase_all_search_history_done, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                    return true;
                }
            });

            //endregion

            //region "Rate this app" - Preference

            findPreference(Utils.Preferences.Keys.RATE_APP).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Opens "Google Play" in the app profile to let the user rate this app:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + UIConsts.APP_PACKAGE_NAME)));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.activity_google_play_not_found), Toast.LENGTH_LONG).show(); // No "Google Play" was found.
                    }
                    return true;
                }
            });

            //endregion

            //region "About" - Preference

            findPreference(Utils.Preferences.Keys.ABOUT_APP).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Shows the "Application About" custom dialog:
                    new AboutDialog().show(getFragmentManager(), UIConsts.Fragments.ABOUT_DIALOG_TAG);
                    return true;
                }
            });

            //endregion

        }

        //endregion

    }

    //endregion

}
