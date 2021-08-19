package com.mike.whatsonwhere.details;

import java.util.List;
import java.util.Queue;

import com.mike.whatsonwhere.model.Movie;

public interface MovieDetailsFetcher {
	
	List<Movie> fetchMovieDetails(Queue<Movie> input);

}
