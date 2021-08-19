package com.mike.whatsonwhere.details.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mike.whatsonwhere.details.MovieDetailsFetcher;
import com.mike.whatsonwhere.model.Movie;

import lombok.extern.slf4j.Slf4j;

/**
 * Makes API request to streaming-availability.p.rapidapi.com to fetch movie details.
 */
@Component
@Profile("api")
@Slf4j
public class StreamingAvailabilityApiDetailsFetcher implements MovieDetailsFetcher {

	private final String detailsUrl;
	private final String apiHost;
	private final String apiKey;
	
	private AsyncHttpClient httpClient;
	
	private Queue<Movie> input;
	private List<Movie> output;
	
	@Autowired
	public StreamingAvailabilityApiDetailsFetcher(@Value("${whatsonwhere.details.url}") String detailsUrl,
												  @Value("${whatsonwhere.api.host}") String apiHost,
												  @Value("${whatsonwhere.api.key}") String apiKey,
							                      AsyncHttpClient httpClient) {
		this.detailsUrl = detailsUrl;
		this.apiHost = apiHost;
		this.apiKey = apiKey;
		this.httpClient = httpClient;
	}
	
	@Override
	public List<Movie> fetchMovieDetails(Queue<Movie> input) {
		
		this.input = input;
		output = new ArrayList<>();
		
//		"https://streaming-availability.p.rapidapi.com/search/basic?country=us&service=prime&type=movie&genre=18&page=1&keyword=the%20descent&language=en"
		String url = new StringBuilder(detailsUrl)
				         .append("?")
				         .toString();
		
		try(AsyncHttpClient client = new DefaultAsyncHttpClient()) {
			client.prepare("GET", url)
			.setHeader("x-rapidapi-key", apiKey)
			.setHeader("x-rapidapi-host", apiHost)
			.execute()
			.toCompletableFuture()
			.thenAccept(System.out::println)
			.join();
		} catch(IOException e) {
			log.error("Exception thrown closing AsyncHttpClient", e);
		}

		return null;
	}

}
