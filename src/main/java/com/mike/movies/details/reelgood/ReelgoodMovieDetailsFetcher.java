package com.mike.movies.details.reelgood;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.mike.movies.Movie;
import com.mike.movies.details.MovieDetailsFetcher;

@Component
@Profile("reelgood")
public class ReelgoodMovieDetailsFetcher implements MovieDetailsFetcher {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReelgoodMovieDetailsFetcher.class);

	private final String detailsUrl;
	private final int maxAttempts;
	private final long pauseTimeMs;
	private CountDownLatch remaining;
	
	@Autowired
	public ReelgoodMovieDetailsFetcher(@Value("${movies.details.url}") String detailsUrl,
							   @Value("${movies.details.max_attempts}") int maxAttempts,
							   @Value("${movies.details.pause_time_ms}") long pauseTimeMs) {
		this.detailsUrl = detailsUrl;
		this.maxAttempts = maxAttempts;
		this.pauseTimeMs = pauseTimeMs;
	}
	
	@Autowired
	private ThreadPoolTaskExecutor executor;
	
	@Override
	public void populateDetails(List<Movie> input) {
		remaining = new CountDownLatch(input.size());
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(100);
        
		try (CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build()) {
			input.forEach(movie -> executor.submit(new MovieDetailsFetchTask(movie, httpclient)));
			remaining.await();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class MovieDetailsFetchTask implements Runnable {
		private Movie movie;
		private CloseableHttpClient httpClient;
		private int attempt;
		
		public MovieDetailsFetchTask(Movie movie, CloseableHttpClient httpClient) {
			this.movie = movie;
			this.httpClient = httpClient;
			this.attempt = 1;
		}
		
		public void run() {
			try (CloseableHttpResponse response = httpClient.execute(new HttpGet(detailsUrl + movie.getId()))) {
				if(response.getCode() == HttpStatus.SC_NOT_FOUND) {
					
					//TODO if not found, change some characters or change year up or down
					
					
					fail(movie, HttpStatus.SC_NOT_FOUND);
				} else if(response.getCode() != HttpStatus.SC_OK) {
					pause();
					if(++attempt > maxAttempts) {
						fail(movie, response.getCode());
					} else {
						executor.submit(this);
					}
				} else {
					success(response);
				}
				
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		private void success(CloseableHttpResponse response) {
			HttpEntity entity = response.getEntity();
			try{
				String body = EntityUtils.toString(entity);
				EntityUtils.consume(entity);
				remaining.countDown();
			} catch(ParseException|IOException e) {
				LOGGER.error("Error parsing response.", e);
			}
			
		}

		private void fail(Movie movie, int httpStatusCode) {
			remaining.countDown();
			LOGGER.error("Failed to find movie={}, status={}, remaining={}", movie.getId(), httpStatusCode, remaining.getCount());
		}
		

		private void pause() {
			try{
				Thread.sleep(pauseTimeMs);
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
	}

}
