package com.walmart.moviesdatabase.services;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;


import com.walmart.moviesdatabase.models.Movies.MovieFeed;
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
public class MovieTask extends AsyncTaskLoader<MovieFeed> {

    public static final int LOADER_ID = 101;
    private static final String API_KEY = "791f19666b350f88f7bbeed9a403d0a4";
    private String search;
    private String page;
    private Context context;
    private boolean flag;
    private URL url;
    private String id;

    public MovieTask(Context context, String search, int pageNumber, boolean flag, String id) {
        super(context);
        this.context = context;
        this.search = search;
        page = Integer.toString(pageNumber);
        this.flag = flag;
        this.id = id;
    }

    @Override
    public MovieFeed loadInBackground() {
        MovieFeed dataFeed = null;
        HttpURLConnection connection = null;
        try {
            if (flag)
                url = new URL("https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&query=" + search + "&page=" + page);
            else
                url = new URL("https://api.themoviedb.org/3/movie/" + id + "/similar?api_key=" + API_KEY + "&language=en-US");
            // initialize and fetch data
            connection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] contents = new byte[1024];
            int bytesRead = 0;
            StringBuffer buffer = new StringBuffer();
            while ((bytesRead = in.read(contents)) != -1) {
                buffer.append(new String(contents, 0, bytesRead));
            }
            // parse json String
            String response = buffer.toString();
            dataFeed =  new JsonUtils().getMovieFeedJSONfromString(response);
        } catch (MalformedURLException e) {
            Log.d("", e.getMessage());
        } catch (Exception e) {
            Log.d("", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return dataFeed;
    }
}
