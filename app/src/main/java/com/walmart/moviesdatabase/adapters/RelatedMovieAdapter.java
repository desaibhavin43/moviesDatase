package com.walmart.moviesdatabase.adapters;

import android.content.Context;
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
import com.walmart.moviesdatabase.R;
import com.walmart.moviesdatabase.utils.LazyLoadListener;
import com.walmart.moviesdatabase.models.Movies.MovieFeed;
import com.walmart.moviesdatabase.models.Movies.MovieDetail;

import java.util.ArrayList;

/**
 * Created by bdesai on 5/30/17.
 */

public class RelatedMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private LazyLoadListener mOnLoadMoreListener;
    private final LinearLayoutManager linearLayoutManager;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private Context context;
    private ArrayList<MovieDetail> moviesList;
    private RecyclerView recyclerView;

    public RelatedMovieAdapter(Context context, RecyclerView recyclerView, final LinearLayoutManager linearLayoutManager, ArrayList<MovieDetail> moviesList) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.linearLayoutManager = linearLayoutManager;
        this.moviesList = moviesList;
        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        this.moviesList = result;
        notifyDataSetChanged();
        setLoaded();
    }

    public void setOnLoadMoreListener(LazyLoadListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return moviesList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_related_movies_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_movies_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder movieViewHolder = (ItemViewHolder) holder;
            movieViewHolder.tvTitle.setText(moviesList.get(position).getTitle());
            if(moviesList.get(position).getPoster_path() == null)
                movieViewHolder.ivImage.setImageResource(R.mipmap.no_image);
            else
                Glide.with(context)
                        .load("http://image.tmdb.org/t/p/w185"+ moviesList.get(position).getPoster_path())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter().centerCrop()
                        .into(movieViewHolder.ivImage);

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return moviesList == null ? 0 : moviesList.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private ImageView ivImage;
        public ItemViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

        }
    }
}