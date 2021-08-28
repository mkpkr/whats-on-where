package com.mike.whatsonwhere.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

import com.mike.whatsonwhere.model.Movie;
import com.mike.whatsonwhere.model.Movie.Service;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AsyncHttpMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private CountDownLatch remaining;
	private int maxConcurrentRequests;
	
	private AsyncHttpClient httpClient;
	
	private Queue<Movie> input;
	private List<Movie> output;
	
	public AsyncHttpMovieDetailsFetcher(int maxConcurrentRequests,
							           AsyncHttpClient httpClient) {
		this.maxConcurrentRequests = maxConcurrentRequests;
		this.httpClient = httpClient;
	}
	
	protected abstract List<MovieDetailsRequest> buildMovieDetailsRequests(Movie movie);
	
//	protected abstract MovieDetailsRequest buildMovieDetailsRequest(Movie movie, Service service);
	
	protected abstract void handleSuccessResponse(Response response, Movie movie);

	@Override
	public List<Movie> fetchMovieDetails(Queue<Movie> input) {
		this.input = input;
		output = new ArrayList<>();
		remaining = new CountDownLatch(input.size());

		for(int i = 0; i < maxConcurrentRequests; i++) {
			if(!input.isEmpty()) {
				fetchDetails(input.poll());
			}
		}

		try{
			remaining.await();
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
			} catch(IOException e) {
				log.error("Couldn't close httpClient.", e);
			} 
		}
		
		return output;
	}

	private void fetchDetails(Movie movie) {
		List<MovieDetailsRequest> requests = buildMovieDetailsRequests(movie);
		
		for(MovieDetailsRequest request : requests) {
			BoundRequestBuilder builder = httpClient.prepareGet(request.getCompleteUrl());
			request.getHeaders().entrySet().stream()
			       .forEach(header -> builder.setHeader(header.getKey(), header.getValue()));
			builder.execute(new CompletionHandler(movie));
		}
		
	}
	
//	private void fetchDetails(Movie movie, EnumSet<Service> services) {
//		for(Service service : services) {
//			MovieDetailsRequest request = buildMovieDetailsRequest(movie, service);
//
//			BoundRequestBuilder builder = httpClient.prepareGet(request.getCompleteUrl());
//				request.getHeaders().entrySet().stream()
//				       .forEach(header -> builder.setHeader(header.getKey(), header.getValue()));
//				builder.execute(new CompletionHandler(movie, EnumSet.of(service), false));
//		}
//	}
	
	/*
	 * Handle async HTTP response to fill movie details 
	 */
	private class CompletionHandler extends AsyncCompletionHandler<Void> {
		
		private Movie movie;
		
		public CompletionHandler(Movie movie) {
			this.movie = movie;
		}
		
		@Override
		public Void onCompleted(Response response) {
			int httpStatusCode = response.getStatusCode();

			if(httpStatusCode == HttpResponseStatus.OK.code()) {
				handleSuccessResponse(response, movie);
			} else {
				handleFailResponse(httpStatusCode);
			}
			
			remaining.countDown();
			if(!input.isEmpty()) {
				fetchDetails(input.poll());
			}
			output.add(movie);
			return null;
		}
		
		private void handleFailResponse(int httpStatusCode) {
			log.error("Failed to find movie={}, status={}", movie.getId(), httpStatusCode);
			
		}

		@Override
		public void onThrowable(Throwable t) {
			log.error("Error getting details for {} ({})", movie.getId(), t.getMessage());
		}
	}
	
	

}
