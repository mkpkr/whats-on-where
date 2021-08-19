package com.mike.whatsonwhere.watchlist;

import java.util.Queue;

import com.mike.whatsonwhere.model.Movie;

public interface WatchlistParser {
	
	Queue<Movie> parseWatchlist();

}
