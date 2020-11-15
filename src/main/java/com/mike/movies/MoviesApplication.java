package com.mike.movies;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.comparator.Comparators;

import com.mike.movies.details.reelgood.ReelgoodMovieDetailsFetcher;
import com.mike.movies.watchlist.WatchlistParser;

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
				try {
	//				Comparator<Movie> movieComparator = Comparator.comparing(keyExtractor)
					long before = System.nanoTime();
					List<Movie> movies = watchlistParser.parseWatchlist();
					long after = System.nanoTime();
					LOGGER.info("Parse time: " + (after-before));
					before = System.nanoTime();
					movieDetailsFetcher.populateDetails(movies);
					after = System.nanoTime();
					LOGGER.info("Details time: " + (after-before));
					movies.forEach(s -> System.out.println(s));
				} catch(Throwable t) {
					LOGGER.error("Exception thrown during execution.", t);
				}
				}
		};
	}
	
	//TODO separate configuration class
	@Bean
	public ThreadPoolTaskExecutor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(32);
		executor.setMaxPoolSize(32);
		return executor;
	}
	
}
