package com.walmart.moviesdatabase.services;

/**
 * Created by bdesai on 5/31/17.
 */

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.walmart.moviesdatabase.models.MoviePortfolio.MovieInfo;
import com.walmart.moviesdatabase.utils.JsonUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
 *  Loads all data asynchronously from MovieDB.com in json format,
 *  Monitor data source and deliver new data to Image Adapter class
 */
public class MovieDetailsTask extends AsyncTaskLoader<MovieInfo> {
    private final String TAG = MovieDetailsTask.class.getSimpleName();
    public static final int LOADER_ID = 102;
    private String id;
    private Context context;
    private final String API_KEY = "791f19666b350f88f7bbeed9a403d0a4";

    public MovieDetailsTask(Context context, String id) {
        super(context);
        this.context = context;
        this.id = id;
    }

    @Override
    public MovieInfo loadInBackground() {
        MovieInfo dataFeed = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/" + id + "?api_key=" + API_KEY + "&language=en-US");
            connection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] contents = new byte[1024];
            int bytesRead = 0;
            StringBuffer buffer = new StringBuffer();
            while ((bytesRead = in.read(contents)) != -1) {
                buffer.append(new String(contents, 0, bytesRead));
            }

            String response = buffer.toString();
            dataFeed = new JsonUtils().getMovieInfoJSONfromString(response);
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return dataFeed;
    }

}
