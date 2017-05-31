package com.walmart.moviesdatabase.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.walmart.moviesdatabase.services.MovieTask;
import com.walmart.moviesdatabase.R;
import com.walmart.moviesdatabase.utils.LazyLoadListener;
import com.walmart.moviesdatabase.models.Movies.MovieFeed;
import com.walmart.moviesdatabase.models.Movies.MovieDetail;
import com.walmart.moviesdatabase.adapters.MovieAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by bdesai on 5/30/17.
 */

public class SearchFragment extends Fragment {

    private String TAG = SearchFragment.class.getSimpleName();

    LinearLayoutManager linearLayoutManager;
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private SearchView searchView;
    private MovieFeed movieDataSet;
    private ArrayList<MovieDetail> result;
    private static int numberOfPages = 0;
    ProgressDialog progressDialog;
    StringBuffer query = new StringBuffer();
    private boolean flag = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setHasFixedSize(false);

        getLoaderManager().initLoader(MovieTask.LOADER_ID, null, movieLoaderListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected() && flag) {
            if (getLoaderManager() != null) {
                result.clear();
                mAdapter.clearAdapter();
                getLoaderManager().destroyLoader(MovieTask.LOADER_ID);
            }
            movieDataSet = null;
            numberOfPages = 1;
            getLoaderManager().initLoader(MovieTask.LOADER_ID, null, movieLoaderListener).forceLoad();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Query", searchView.getQuery().toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        flag = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        flag = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);

        View view = inflater.inflate(R.layout.search_fragment, container, false);

        searchView = (SearchView) view.findViewById(R.id.search);
        searchView.setQueryHint("Search Movie");
        searchView.setBackgroundColor(Color.parseColor("#c9c9c9"));
        if (isConnected()) {
            if (getArguments() != null) {
                query = query.replace(0, query.length() - 1, getArguments().getString("Query"));
                if (getLoaderManager() != null) {
                    result.clear();
                    mAdapter.clearAdapter();
                    getLoaderManager().destroyLoader(MovieTask.LOADER_ID);
                }
                movieDataSet = null;
                numberOfPages = 1;
                getLoaderManager().initLoader(MovieTask.LOADER_ID, null, movieLoaderListener).forceLoad();
            }
        } else
            buildDialog(getActivity()).show();

        result = new ArrayList<>();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvlistItem);
        linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new MovieAdapter(getActivity(), mRecyclerView, linearLayoutManager, result);

        mRecyclerView.setAdapter(mAdapter);

        //search the query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String submittedQuery) {
                if (isConnected()) {
                    if (getLoaderManager() != null) {
                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Processing...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        result.clear();
                        mAdapter.clearAdapter();
                        getLoaderManager().destroyLoader(MovieTask.LOADER_ID);
                    }
                    movieDataSet = null;
                    numberOfPages = 1;
                    getLoaderManager().initLoader(MovieTask.LOADER_ID, null, movieLoaderListener).forceLoad();
                } else
                    buildDialog(getActivity()).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Load more items on scroll
        mAdapter.setOnLoadMoreListener(new LazyLoadListener() {

            @Override
            public void onLazyLoad() {
                if (movieDataSet != null && movieDataSet.getTotal_pages() > numberOfPages) {
                    numberOfPages++;
                    Log.d("haint", "Load More");
                    result.add(null);

                    //Load more data for reyclerview
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("haint", "Load More 2");

                            //Remove loading item
                            if (result.size() > 1)
                                result.remove(result.size() - 1);
                            getLoaderManager().restartLoader(MovieTask.LOADER_ID, null, movieLoaderListener).forceLoad();
                        }
                    }, 4000);
                }
            }
        });
        return view;
    }

    //Loader to get data from server
    private LoaderManager.LoaderCallbacks<MovieFeed> movieLoaderListener
            = new LoaderManager.LoaderCallbacks<MovieFeed>() {
        @Override
        public Loader<MovieFeed> onCreateLoader(int id, Bundle args) {
            if (query.length() > 0)
                return new MovieTask(getActivity(), encode(query.toString()), numberOfPages, true, null);
            else
                return new MovieTask(getActivity(), encode(searchView.getQuery().toString()), numberOfPages, true, null);
        }

        @Override
        public void onLoadFinished(Loader<MovieFeed> loader, MovieFeed data) {
            if (result != null) {
                for (int i = 0; i < data.results.size(); i++) {
                    result.add(data.results.get(i));
                }
                movieDataSet = data;
                if (movieDataSet.page == 1)
                    progressDialog.dismiss();
                mAdapter.setData(result, data);
            }
        }

        @Override
        public void onLoaderReset(Loader<MovieFeed> loader) {
            loader.cancelLoad();
            loader.abandon();
            getLoaderManager().destroyLoader(MovieTask.LOADER_ID);
        }
    };

    //Checking Internet Connection
    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet connection.");
        builder.setMessage("You have no internet connection");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        return builder;
    }

    public String encode(String query) {
        try {
            return URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "error - " + e.getMessage());
        }
        return query;
    }
}