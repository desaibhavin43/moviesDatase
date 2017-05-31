package com.walmart.moviesdatabase.models.Movies;

import java.util.List;

public class MovieFeed {

    public int page;
    public List<MovieDetail> results;
    public int total_results;
    public int total_pages;

    public int getTotal_pages() {
        return total_pages;
    }
    public int getTotal_results() {
        return total_results;
    }
    public void setTotal_pages(int n) {
        total_pages = n;
    }

}
