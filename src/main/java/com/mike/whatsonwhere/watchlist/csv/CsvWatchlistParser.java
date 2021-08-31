package com.mike.whatsonwhere.watchlist.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mike.whatsonwhere.model.Movie;
import com.mike.whatsonwhere.watchlist.WatchlistParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("csv")
@Component
public class CsvWatchlistParser implements WatchlistParser {

	@Value("${whatsonwhere.watchlist.path}")
	private String watchlistPath;
	
	@Value("${whatsonwhere.watchlist.csv.movie-position}")
	private int moviePosition;
	
	@Value("${whatsonwhere.watchlist.csv.year-position}")
	private int yearPosition;
	
	public Queue<Movie> parseWatchlist() {
		Queue<Movie> movies = new LinkedList<>();
		try(Scanner scanner = new Scanner(new File(watchlistPath))) {
			scanner.nextLine();
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Movie movie = getMovie(line);
				if(movie != null) {
					movies.add(movie);
				}
			}
			return movies;
		} catch(FileNotFoundException e) {
			log.error("File not found: " + watchlistPath, e);
		} catch(Exception e) {
			log.error("Exception while parsing watchlist file", e);
		}
		return new LinkedList<>();
	}

	private Movie getMovie(String line) {
		String[] details = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		int year;
		try{
			year = Integer.parseInt(details[yearPosition]);
		} catch(NumberFormatException e) {
			log.error("Error parsing {}; year {} is not a valid number", line, details[yearPosition]);
			return null;
		}
		return new Movie(details[moviePosition], year);
	}

}
