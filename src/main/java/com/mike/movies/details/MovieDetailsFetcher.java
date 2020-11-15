package com.mike.movies.details;

import java.util.List;

import com.mike.movies.Movie;

public interface MovieDetailsFetcher {
	
	void populateDetails(List<Movie> input);

}
