package com.mike.movies.watchlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mike.movies.Movie;

public abstract class FileWatchlistParser implements WatchlistParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchlistParser.class); 
	
	@Value("${watchlist.path}")
	private String watchlistPath;
	
	public Queue<Movie> parseWatchlist() {
		Queue<Movie> movies = new LinkedList<>();
		try(Scanner scanner = new Scanner(new File(watchlistPath))) {
			scanner.nextLine();
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				movies.add(getMovieNameAndYear(line));
			}
			return movies;
		} catch(FileNotFoundException e) {
			LOGGER.error("File not found: " + watchlistPath, e);
		} catch(Exception e) {
			LOGGER.error("Exception while parsing watchlist file", e);
		}
		return new LinkedList<>();
	}

	protected abstract Movie getMovieNameAndYear(String line);

}
