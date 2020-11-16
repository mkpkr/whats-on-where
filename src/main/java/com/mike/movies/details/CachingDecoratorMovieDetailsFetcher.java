package com.mike.movies.details;

import java.util.List;
import java.util.Queue;

import com.mike.movies.Movie;

public class CachingDecoratorMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private MovieDetailsFetcher delegate;
	
	@Override
	public List<Movie> fetchMovieDetails(Queue<Movie> input) {
		// TODO Auto-generated method stub
		return null;
	}
}
