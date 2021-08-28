package com.mike.whatsonwhere.details;

import java.util.List;
import java.util.Queue;

import com.mike.whatsonwhere.model.Movie;

/**
 * TODO
 *
 */
public class CachingDecoratorMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private MovieDetailsFetcher delegate;
	
	@Override
	public List<Movie> fetchMovieDetails(Queue<Movie> input) {
		// TODO Auto-generated method stub
		return null;
	}
}
