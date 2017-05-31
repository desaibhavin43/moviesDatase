package com.walmart.moviesdatabase.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.walmart.moviesdatabase.services.MovieDetailsTask;
import com.walmart.moviesdatabase.services.MovieTask;
import com.walmart.moviesdatabase.R;
import com.walmart.moviesdatabase.utils.LazyLoadListener;
import com.walmart.moviesdatabase.models.MoviePortfolio.MovieInfo;
import com.walmart.moviesdatabase.models.Movies.MovieFeed;
import com.walmart.moviesdatabase.models.Movies.MovieDetail;
import com.walmart.moviesdatabase.adapters.RelatedMovieAdapter;

import java.util.ArrayList;


/**
 * Created by bdesai on 5/30/17.
 */

public class DetailFragment extends Fragment {
    private final String TAG = DetailFragment.class.getSimpleName();


    private RecyclerView relatedMovieRecyclerView;
    private RelatedMovieAdapter relatedMovieAdapter;
    private MovieFeed movieFeed;
    private ArrayList<MovieDetail> movieDetailsList;

    private static int numberOfPages = 0;
    View view = null;
    MovieDetail itemInfo;
    int movieId = -1;

    StringBuffer type;
    StringBuffer countries;
    StringBuffer companies;

    private ProgressDialog progressDialog;
    LinearLayoutManager linearLayoutManager;
    ImageView ivImage, ivDate, ivStar;
    TextView movieTitle, movieType, movieDate, movieRating;
    TextView movieOverview, movieProductionCountries, movieProductionCompanies;
    TextView moviePermanentOverview, movieCountries, movieCompanies, relatedMovies;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        relatedMovieRecyclerView.setHasFixedSize(false);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        type = new StringBuffer();
        countries = new StringBuffer();
        companies = new StringBuffer();

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (getArguments() != null) {
            itemInfo = getArguments().getParcelable("Item GenreInfo");
        }
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.detail_fragment, container, false);

        ivImage = (ImageView) view.findViewById(R.id.ivImage);

        movieTitle = (TextView) view.findViewById(R.id.tvTitle);

        movieType = (TextView) view.findViewById(R.id.tvType);

        movieDate = (TextView) view.findViewById(R.id.tvDate);
        ivDate = (ImageView) view.findViewById(R.id.ivDate);

        ivStar = (ImageView) view.findViewById(R.id.ivStar);
        movieRating = (TextView) view.findViewById(R.id.tvRating);

        moviePermanentOverview = (TextView) view.findViewById(R.id.tvPermanentOverview);
        movieOverview = (TextView) view.findViewById(R.id.tvOverview);

        movieCountries = (TextView) view.findViewById(R.id.tvCountries);
        movieProductionCountries = (TextView) view.findViewById(R.id.tvProductionCounties);

        movieCompanies = (TextView) view.findViewById(R.id.tvCompanies);
        movieProductionCompanies = (TextView) view.findViewById(R.id.tvProductionCompanies);
        relatedMovies = (TextView) view.findViewById(R.id.tvRelatedMovies);
        setVisibility(View.INVISIBLE);

        movieDetailsList = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        relatedMovieRecyclerView = (RecyclerView) view.findViewById(R.id.rvRelated);
        relatedMovieRecyclerView.setLayoutManager(linearLayoutManager);

        relatedMovieAdapter = new RelatedMovieAdapter(getActivity(), relatedMovieRecyclerView, linearLayoutManager, movieDetailsList);

        relatedMovieRecyclerView.setAdapter(relatedMovieAdapter);

        //load more item on scroll
        relatedMovieAdapter.setOnLoadMoreListener(new LazyLoadListener() {
            @Override
            public void onLazyLoad() {
                if (movieFeed != null && movieFeed.getTotal_pages() > numberOfPages) {
                    numberOfPages++;
                    Log.d("haint", "Load More");
                    movieDetailsList.add(null);

                    //Load more data for reyclerview
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("haint", "Load More 2");

                            //Remove loading item
                            if (movieDetailsList.size() > 1)
                                movieDetailsList.remove(movieDetailsList.size() - 1);
                            getLoaderManager().restartLoader(MovieTask.LOADER_ID, null, similarMovieLoaderListener).forceLoad();
                        }
                    }, 1000);
                }
            }
        });

        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        return view;
    }

    // make visible
    private void setVisibility(int visibility) {
        ivImage.setVisibility(visibility);
        movieTitle.setVisibility(visibility);
        movieType.setVisibility(visibility);
        movieDate.setVisibility(visibility);
        movieRating.setVisibility(visibility);
        movieOverview.setVisibility(visibility);
        movieProductionCountries.setVisibility(visibility);
        movieProductionCompanies.setVisibility(visibility);
        ivStar.setVisibility(visibility);
        ivDate.setVisibility(visibility);
        moviePermanentOverview.setVisibility(visibility);
        movieCountries.setVisibility(visibility);
        movieCompanies.setVisibility(visibility);
        relatedMovies.setVisibility(visibility);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isConnected()) {

            // check if the arguments are passed in the fragment during the app launch
            Bundle args = getArguments();
            if (args != null) {
                // Set article based on argument passed in
                getData((MovieDetail) args.getParcelable("Item GenreInfo"));
            } else if (itemInfo != null) {
                // Set article based on saved instance state defined during onCreateView
                getData(itemInfo);
            }
        }
        else
            buildDialog(getActivity()).show();
    }

    //fetch data from backend
    public void getData(MovieDetail itemInfo) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        type = type.delete(0,type.length());
        companies = companies.delete(0,companies.length());
        countries = countries.delete(0,countries.length());
        this.itemInfo = itemInfo;
        if(itemInfo.getId() != -1) {
            numberOfPages = 1;
            movieId = itemInfo.getId();
            getLoaderManager().initLoader(MovieDetailsTask.LOADER_ID, null, movieDetailsLoaderListener).forceLoad();
            getLoaderManager().initLoader(MovieTask.LOADER_ID, null, similarMovieLoaderListener).forceLoad();
        }
    }

    //inflate layout with the data
    public void loadData(MovieDetail itemInfo) {
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        if(itemInfo.getPoster_path() == null)
            ivImage.setImageResource(R.mipmap.no_image);
        else
            Glide.with(getContext())
            .load("http://image.tmdb.org/t/p/w185"+itemInfo
                    .getPoster_path())
                    .fitCenter().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivImage);

        movieTitle = (TextView) view.findViewById(R.id.tvTitle);
        if(itemInfo.getTitle().length() > 47)
            movieTitle.setText(itemInfo.getTitle().substring(0,47)+" ...");
        else if(itemInfo.getTitle().length() < 29)
            movieTitle.setText(itemInfo.getTitle()+"\n");
        else
            movieTitle.setText(itemInfo.getTitle());

        setVisibility(View.VISIBLE);
        movieType.setText(type.toString());
        movieDate.setText(itemInfo.getRelease_date());
        movieRating.setText(itemInfo.getVoteAverage());
        movieOverview.setText(itemInfo.getOverview());
        movieProductionCountries.setText(countries.toString());
        movieProductionCompanies.setText(companies.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putParcelable("Item GenreInfo", itemInfo);
    }

    private LoaderManager.LoaderCallbacks<MovieInfo> movieDetailsLoaderListener
            = new LoaderManager.LoaderCallbacks<MovieInfo>() {
        @Override
        public Loader<MovieInfo> onCreateLoader(int id, Bundle args) {
            Log.d("******onCreateLoader",""+movieId);
            return new MovieDetailsTask(getActivity(), Integer.toString(movieId));
        }

        @Override
        public void onLoadFinished(Loader<MovieInfo> loader, MovieInfo data) {
            processData(data);
            loadData(itemInfo);
        }

        @Override
        public void onLoaderReset(Loader<MovieInfo> loader) {
            loader.cancelLoad();
            loader.abandon();
        }
    };

    private LoaderManager.LoaderCallbacks<MovieFeed> similarMovieLoaderListener
            = new LoaderManager.LoaderCallbacks<MovieFeed>() {
        @Override
        public Loader<MovieFeed> onCreateLoader(int id, Bundle args) {
            return new MovieTask(getActivity(), "", numberOfPages, false, Integer.toString(movieId));
        }

        @Override
        public void onLoadFinished(Loader<MovieFeed> loader, MovieFeed data) {
            for (int i = 0; i < data.results.size(); i++) {
                movieDetailsList.add(data.results.get(i));
            }
            movieFeed = data;
            if (movieFeed.page == 1)
                progressDialog.dismiss();
            relatedMovieAdapter.setData(movieDetailsList, data);
        }

        @Override
        public void onLoaderReset(Loader<MovieFeed> loader) {

        }
    };

    public void processData(MovieInfo data) {
        for (int i = 0; i < data.getGenres().size(); i++) {
            type = type.append(data.getGenres().get(i).getName() + ", ");
            if (i == data.getGenres().size() - 1)
                type = type.append(data.getGenres().get(i).getName());
        }
        for (int i = 0; i < data.getProduction_companies().size(); i++) {
            companies = companies.append(data.getProduction_companies().get(i).getName() + ", ");
            if(i == data.getProduction_companies().size()-1)
                companies = companies.append(data.getProduction_companies().get(i).getName());

        }
        for (int i = 0; i < data.getProduction_countries().size(); i++) {
            countries = countries.append(data.getProduction_countries().get(i).getName() + ", ");
            if(i == data.getProduction_countries().size()-1)
                countries = countries.append(data.getProduction_countries().get(i).getName());
        }
    }

    public boolean isConnected() {
        //Checking Internet Connection
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
}