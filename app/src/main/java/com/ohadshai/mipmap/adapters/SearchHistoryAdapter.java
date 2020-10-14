package com.ohadshai.mipmap.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ohadshai.mipmap.R;
import com.ohadshai.mipmap.db.DBHandler;
import com.ohadshai.mipmap.entities.Search;
import com.ohadshai.mipmap.ui.fragments.SearchHistoryFragment;
import com.ohadshai.mipmap.ui.views.SearchHelper;

import java.util.ArrayList;

/**
 * Represents an adapter for the search history list.
 * Created by Ohad on 1/6/2017.
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    //region Private Members

    /**
     * Holds the list of search history to adapt.
     */
    private ArrayList<Search> _searchHistory;

    /**
     * Holds the activity owner of the adapter.
     */
    private Activity _activity;

    /**
     * Holds the fragment owner.
     */
    private SearchHistoryFragment _searchHistoryFragment;

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of an adapter for the search history list.
     *
     * @param searchHistory         The list of search history to adapt.
     * @param searchHistoryFragment The fragment owner of the adapter.
     */
    public SearchHistoryAdapter(ArrayList<Search> searchHistory, SearchHistoryFragment searchHistoryFragment) {
        this._searchHistory = searchHistory;
        this._activity = searchHistoryFragment.getActivity();
        this._searchHistoryFragment = searchHistoryFragment;
        this._repository = DBHandler.getInstance(_activity);
    }

    //endregion

    //region Events

    @Override
    public SearchHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new SearchHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchHistoryAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bindViewHolder(_searchHistory.get(position)); // Gets the search by the position, and binds it to the view holder.
    }

    @Override
    public int getItemCount() {
        return _searchHistory.size();
    }

    //endregion

    //region Public API

    /**
     * Sets the list of search history to adapt (Overrides the current list if exists).
     *
     * @param searchHistory The list of search history to adapt.
     */
    public void setSearchHistory(ArrayList<Search> searchHistory) {
        this._searchHistory = searchHistory;
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a view holder for an item in the adapter.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        //region Private Members

        /**
         * Holds the search object of the current position item.
         */
        private Search _search;

        private CardView _cardSearch;
        private TextView _lblSearchText;

        //endregion

        /**
         * C'tor
         * Initializes a new instance of a view holder for an item in the adapter.
         *
         * @param itemView The view of the item in the adapter.
         */
        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnCreateContextMenuListener(this);

            _cardSearch = (CardView) itemView.findViewById(R.id.cardSearch);
            _cardSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchHelper.with((AppCompatActivity) _activity).loadQueryAndSearch(_search.getText());
                }
            });

            _lblSearchText = (TextView) itemView.findViewById(R.id.lblSearchText);

        }

        //region Events

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(_search.getText());
            menu.setHeaderIcon(R.mipmap.ic_history_black);
            menu.add(0, v.getId(), 0, R.string.general_delete);

            // OnClickListener for: "Delete":
            menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Deletes the search history:
                    _repository.searchHistory.delete(_search.getId());
                    _searchHistory.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    _searchHistoryFragment.displayListState();
                    return true;
                }
            });
        }

        //endregion

        //region Local Methods

        /**
         * Binds a search object to the view holder.
         *
         * @param search The search object to bind.
         */
        void bindViewHolder(Search search) {
            this._search = search;

            // Sets item views, based on the views and the data model:

            _lblSearchText.setText(_search.getText());
        }

        //endregion

    }

    //endregion

}
