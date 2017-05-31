package com.walmart.moviesdatabase.models.MoviePortfolio;

import java.util.List;

/**
 * Created by bdesai on 5/30/17.
 */

public class MovieInfo {
    private boolean adult;
    private List<GenreInfo> genres;
    private List<CompanyInfo> production_companies;
    private List<CountriesInfo> production_countries;
    public List<GenreInfo> getGenres() {
        return genres;
    }
    public List<CompanyInfo> getProduction_companies() {
        return production_companies;
    }
    public List<CountriesInfo> getProduction_countries() {
        return production_countries;
    }


}
