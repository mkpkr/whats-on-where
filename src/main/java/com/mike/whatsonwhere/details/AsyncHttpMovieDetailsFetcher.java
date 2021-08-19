package com.mike.whatsonwhere.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import com.mike.whatsonwhere.model.Movie;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AsyncHttpMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private String detailsUrl;
	private CountDownLatch remaining;
	private int maxConcurrentRequests;
	
	private AsyncHttpClient httpClient;
	
	private Queue<Movie> input;
	private List<Movie> output;
	
	public AsyncHttpMovieDetailsFetcher(String detailsUrl,
							   		   int maxConcurrentRequests,
							           AsyncHttpClient httpClient) {
		this.detailsUrl = detailsUrl;
		this.maxConcurrentRequests = maxConcurrentRequests;
		this.httpClient = httpClient;
	}

	@Override
	public List<Movie> fetchMovieDetails(Queue<Movie> input) {
		this.input = input;
		output = new ArrayList<>();
		remaining = new CountDownLatch(input.size());

		for(int i = 0; i < maxConcurrentRequests; i++) {
			fetchNext();
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
	
	private void fetchNext() {
		if(!input.isEmpty()) {
			fetchDetails(input.poll(), true);
		}
	}
	
	private void fetchDetails(Movie movie, boolean retryOnFail) {
		httpClient.prepareGet(detailsUrl + movie.getId())
		          .execute(new CompletionHandler(movie, retryOnFail));
	}
	
	protected abstract void handleSuccessResponse(Response response, Movie movie);
	
	/*
	 * Handle async HTTP response to fill movie details 
	 */
	private class CompletionHandler extends AsyncCompletionHandler<Movie> {
		
		private Movie movie;
		private boolean retryOnFail;
		
		public CompletionHandler(Movie movie, boolean retryOnFail) {
			this.movie = movie;
			this.retryOnFail = retryOnFail;
		}
		
		@Override
		public Movie onCompleted(Response response) {
			int httpStatusCode = response.getStatusCode();

			if(httpStatusCode == HttpResponseStatus.OK.code()) {
				handleSuccessResponse(response, movie);
			} else {
				handleFailResponse(httpStatusCode);
			}
			
			remaining.countDown();
			fetchNext();
			output.add(movie);
			return movie;
		}
		
		private void handleFailResponse(int httpStatusCode) {
			log.error("Failed to find movie={}, status={}", movie.getId(), httpStatusCode);
			
		}

		@Override
		public void onThrowable(Throwable t) {
			if(retryOnFail) {
			    log.error("Error getting details for {}, retrying ({})", movie.getId(), t.getMessage());
			    fetchDetails(movie, false);
			} else {
				log.error("Error getting details for {}, will not retry ({})", movie.getId(), t.getMessage());
			}
		 }


	}
	

}
