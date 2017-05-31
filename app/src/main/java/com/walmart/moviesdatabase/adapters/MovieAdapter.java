package com.walmart.moviesdatabase.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.walmart.moviesdatabase.views.MainActivity;
import com.walmart.moviesdatabase.R;
import com.walmart.moviesdatabase.utils.LazyLoadListener;
import com.walmart.moviesdatabase.models.Movies.MovieFeed;
import com.walmart.moviesdatabase.models.Movies.MovieDetail;

import java.util.ArrayList;


/**
 * Created by bdesai on 5/30/17.
 */

//Adapter for Search Fragment
public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private LazyLoadListener mOnLoadMoreListener;
    private final LinearLayoutManager linearLayoutManager;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private Context context;
    private ArrayList<MovieDetail> result;
    private RecyclerView mRecyclerView;
    private MovieFeed mMovieDataSet;

    public MovieAdapter(Context context, RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager, ArrayList<MovieDetail> result) {
        this.context = context;
        mRecyclerView = recyclerView;
        this.linearLayoutManager = linearLayoutManager;
        this.result = result;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLazyLoad();
                    }
                    isLoading = true;
                }
            }

        });
    }

    public void setData(ArrayList<MovieDetail> result, MovieFeed movieFeed) {
        mMovieDataSet = movieFeed;
        this.result = result;
        notifyDataSetChanged();
        setLoaded();
    }
    public void clearAdapter() {
        result.clear();
        linearLayoutManager.scrollToPositionWithOffset(7,20);
    }

    public void setOnLoadMoreListener(LazyLoadListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return result.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_list_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder movieViewHolder = (ItemViewHolder) holder;

            movieViewHolder.tvTitle.setText(result.get(position).getTitle());

            movieViewHolder.tvDate.setText(result.get(position).getRelease_date());
            movieViewHolder.tvRating.setText(result.get(position).getVoteAverage());
            if(result.get(position).getPoster_path() == null)
                movieViewHolder.ivImage.setImageResource(R.mipmap.no_image);
            else
                Glide.with(context)
                        .load("http://image.tmdb.org/t/p/w185"+result.get(position)
                                .getPoster_path()).thumbnail(.5f).fitCenter().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(movieViewHolder.ivImage);

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return result == null ? 0 : result.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDate;
        private TextView tvRating;
        private ImageView ivImage;
        private ImageView ivDate;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvRating = (TextView) itemView.findViewById(R.id.tvRating);


            final Context context = itemView.getContext();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected())
                        ((MainActivity) context).onItemClick(getAdapterPosition(),result.get(getAdapterPosition()));
                    else
                        buildDialog(context).show();
                }
            });
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }
    public boolean isConnected() {
        //Checking Internet Connection
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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