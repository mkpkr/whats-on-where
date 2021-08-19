package com.mike.whatsonwhere;

import static org.asynchttpclient.Dsl.asyncHttpClient;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import org.asynchttpclient.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.mike.whatsonwhere.details.reelgood.ReelgoodMovieDetailsFetcher;
import com.mike.whatsonwhere.model.Movie;
import com.mike.whatsonwhere.watchlist.WatchlistParser;

@SpringBootApplication
public class MoviesApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(MoviesApplication.class);
			
	@Autowired
	WatchlistParser watchlistParser;
	
	@Autowired
	ReelgoodMovieDetailsFetcher movieDetailsFetcher;
	
	public static void main(String[] args) {
		SpringApplication.run(MoviesApplication.class, args);
	}

	//TODO can this application class implement CommandLineRunner? or static inner? what's best way
	@Bean
	public CommandLineRunner runner() {
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {
					Comparator<Movie> movieComparator = Comparator.comparing((Movie m) -> m.isStreamableOn(Movie.Service.NETFLIX)).reversed()
							                                      .thenComparing(m -> m.getDuration());
					Queue<Movie> watchList = watchlistParser.parseWatchlist();
					List<Movie> movies = movieDetailsFetcher.fetchMovieDetails(watchList);
					movies.sort(movieComparator);
					
					for(Movie movie : movies) {
						LOGGER.info(movie.toString());
					}
			}
		};
	}
	
	//TODO separate configuration class and maybe inout and output queue/list should be beans
	@Bean
	public AsyncHttpClient httpClient() {
		return asyncHttpClient();
	}
	
}
