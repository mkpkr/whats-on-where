package com.mike.whatsonwhere.details.reelgood;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mike.whatsonwhere.details.MovieDetailsFetcher;
import com.mike.whatsonwhere.model.Movie;
import com.mike.whatsonwhere.model.Movie.Service;

import io.netty.handler.codec.http.HttpResponseStatus;

@Component
@Profile("reelgood")
public class ReelgoodMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReelgoodMovieDetailsFetcher.class);

	private final String detailsUrl;
	private CountDownLatch remaining;
	private int maxConcurrentRequests;
	
	private AsyncHttpClient httpClient;
	
	private Queue<Movie> input;
	private List<Movie> output;
	
	@Autowired
	public ReelgoodMovieDetailsFetcher(@Value("${movies.details.url}") String detailsUrl,
							   		   @Value("${movies.details.max_concurrent_requests}") int maxConcurrentRequests,
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
				LOGGER.error("Couldn't close httpClient.", e);
			} 
		}
		
		return output;
	}
	
	private void fetchNext() {
		if(!input.isEmpty()) {
			fetchDetails(input.poll());
		}
	}
	
	private void fetchDetails(Movie movie) {
		httpClient.prepareGet(detailsUrl + movie.getId())
		          .execute(new CompletionHandler(movie));
	}

	private class CompletionHandler extends AsyncCompletionHandler<Movie> {
		
		private Movie movie;
		
		public CompletionHandler(Movie movie) {
			this.movie = movie;
		}
		
		@Override
		public Movie onCompleted(Response response) {
			int httpStatusCode = response.getStatusCode();

			if(httpStatusCode == HttpResponseStatus.OK.code()) {
				success(response);
			} else {
				fail(httpStatusCode);
			}
			
			remaining.countDown();
			fetchNext();
			output.add(movie);
			return movie;
		}

		private void success(Response response) {
			Document document = Jsoup.parse(response.getResponseBody());
			Element body = document.body();
			
			getStreamUrls(body);
			getImdbRating(body);
			getDuration(body);
		}

		private void getStreamUrls(Element body) {
			Elements streamElements = body.getElementsByAttributeValueStarting("title", "Stream on ");
			
			for(Element streamElement : streamElements) {
				String url = streamElement.getElementsByTag("a").get(0).attr("href");
				
				switch(streamElement.attr("title")) {
				case "Stream on Netflix" : movie.addWatchUrl(Service.NETFLIX, url); break;
				case "Stream on Prime Video" : movie.addWatchUrl(Service.AMAZON, url); break;
				case "Stream on Hulu" : movie.addWatchUrl(Service.HULU, url); break;
				default: //unknown service
				}
			}
		}

		private void getImdbRating(Element body) {
			String imdbRating = null;
			
			try {
				imdbRating = body.getElementsByAttributeValue("title", "IMDB Rating").get(0)
	                                    .getElementsByTag("div").get(0)
	                                    .getElementsByTag("span").get(0)
	                                    .text();
			} catch(Exception e) {
				LOGGER.error("Could not find imdb rating for movie={}", movie.getId());
			}
			
			if(imdbRating != null) {
				try {
					movie.setRating(Double.parseDouble(imdbRating));
				} catch(NumberFormatException|NullPointerException e) {
					LOGGER.error("Could not parse imdb rating for movie={}, rating={}", movie.getId(), imdbRating);
				}
			}
		}
		
		private void getDuration(Element body) {
			String durationString = null;
			try {
				durationString = body.getElementsByAttributeValue("itemprop", "duration").get(0)
						             .parent()
						             .text();
			} catch(Exception e) {
				LOGGER.error("Could not find duration for movie={}", movie.getId());
			}
			
			if(durationString != null) {
				String[] durationStrings = durationString.split(" ");
				try {
					int hours = Integer.parseInt(durationStrings[0].substring(0, durationStrings[0].length()-1));
					int minutes = Integer.parseInt(durationStrings[1].substring(0, durationStrings[1].length()-1));
					int duration = hours*60 + minutes;
					movie.setDuration(duration);
				} catch(NumberFormatException|ArrayIndexOutOfBoundsException e) {
					LOGGER.error("Could not parse duration for movie={}, duration={}", movie.getId(), durationString);
				}
			}			
		}
		
		private void fail(int httpStatusCode) {
			LOGGER.error("Failed to find movie={}, status={}", movie.getId(), httpStatusCode);
			
		}

		@Override
		public void onThrowable(Throwable t) {
		    LOGGER.error("Error getting details for {}, retrying ({})", movie.getId(), t.getMessage());
		    fetchDetails(movie);
		 }


	}
}
