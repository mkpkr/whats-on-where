package com.mike.movies.watchlist;

import java.util.Queue;

import com.mike.movies.Movie;

public interface WatchlistParser {
	
	Queue<Movie> parseWatchlist();

}
