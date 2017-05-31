package com.walmart.moviesdatabase.utils;

import com.google.gson.Gson;
import com.walmart.moviesdatabase.models.MoviePortfolio.MovieInfo;
import com.walmart.moviesdatabase.models.Movies.MovieFeed;

/**
 * Created by bdesai on 5/31/17.
 */

public class JsonUtils {
    //Get JSON object from response string
    public  MovieFeed getMovieFeedJSONfromString(String result){
        MovieFeed dataFeed = new Gson().fromJson(result,MovieFeed.class);
        return dataFeed;
    }

    //Get JSON object from response string
    public  MovieInfo getMovieInfoJSONfromString(String result){
        MovieInfo dataFeed = new Gson().fromJson(result,MovieInfo.class);
        return dataFeed;
    }
}
