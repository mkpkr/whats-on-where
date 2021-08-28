package com.mike.whatsonwhere.details.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mike.whatsonwhere.details.AsyncHttpMovieDetailsFetcher;
import com.mike.whatsonwhere.details.MovieDetailsRequest;
import com.mike.whatsonwhere.model.Movie;

import lombok.extern.slf4j.Slf4j;

/**
 * Makes HTTP GET request to streaming-availability API to fetch movie details.
 * 
 * Issues with Streaming Availability API: 
 * -must make separate HTTP request for each movie AND service (total requests = num movies * num services)
 */
@Component
@Profile("api")
public class StreamingAvailabilityApiDetailsFetcher extends AsyncHttpMovieDetailsFetcher {

	private String detailsUrl;
	private String apiHost;
	private String apiKey;
	private String serviceCountry;
	
	private EnumSet<Movie.Service> services;

	
	@Autowired
	public StreamingAvailabilityApiDetailsFetcher(@Value("${whatsonwhere.details.url}") String detailsUrl,
												  @Value("${whatsonwhere.details.max_concurrent_requests}") int maxConcurrentRequests,
												  @Value("${whatsonwhere.api.host}") String apiHost,
												  @Value("${whatsonwhere.api.key}") String apiKey,
												  @Value("${whatsonwhere.service.country}") String serviceCountry,
												  @Value("${whatsonwhere.movie.services}") String services,
							                      AsyncHttpClient httpClient) {
		super(maxConcurrentRequests, httpClient);
		
		this.detailsUrl = detailsUrl;
		this.apiHost = apiHost;
		this.apiKey = apiKey;
		this.serviceCountry = serviceCountry;
		this.services = Arrays.stream(services.split(","))
				              .map(String::toUpperCase)
	                          .map(Movie.Service::valueOf)
	                          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Movie.Service.class)));
	}
	

	@Override
	protected List<MovieDetailsRequest> buildMovieDetailsRequests(Movie movie) {
		List<MovieDetailsRequest> requests = new ArrayList<>();
		
		for(Movie.Service service : services) {
			String urlWithQueryString = new StringBuilder(detailsUrl)
                    .append("?")
                    .append("country").append("=").append(serviceCountry)
                    .append("service").append("=").append(service)
                    .append("type").append("=").append("movie")
                    .append("keyword").append("=").append(movie.getName())
                    .toString();

			requests.add(MovieDetailsRequest.builder()
					                .baseUrl(urlWithQueryString)
							        .header("x-rapidapi-key", List.of(apiKey))
							        .header("x-rapidapi-host", List.of(apiHost))
							        .build());
		}
		
		return requests;
	}

	@Override
	protected void handleSuccessResponse(Response response, Movie movie) {
		// TODO Auto-generated method stub
		
	}

}
