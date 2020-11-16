package com.mike.movies.details;

import java.util.List;
import java.util.Queue;

import com.mike.movies.Movie;

public interface MovieDetailsFetcher {
	
	List<Movie> fetchMovieDetails(Queue<Movie> input);

}
