package com.mike.whatsonwhere.details.reelgood;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mike.whatsonwhere.details.AsyncHttpMovieDetailsFetcher;
import com.mike.whatsonwhere.model.Movie;
import com.mike.whatsonwhere.model.Movie.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Makes HTTP GET requests to reelgood.com and scrapes the HTML to see which services the movie streams on.
 * 
 * Issues with using ReelGood (or any similaar movie site): 
 * -must make separate HTTP request for each movie
 * -scrapes HTML; no defined contract so could break any time
 *
 */
@Component
@Profile("reelgood")
@Slf4j
public class ReelgoodMovieDetailsFetcher extends AsyncHttpMovieDetailsFetcher {
	
	@Autowired
	public ReelgoodMovieDetailsFetcher(@Value("${whatsonwhere.details.url}") String detailsUrl,
							   		   @Value("${whatsonwhere.details.max_concurrent_requests}") int maxConcurrentRequests,
							           AsyncHttpClient httpClient) {
		super(detailsUrl, maxConcurrentRequests, httpClient);
	}
	
	@Override
	protected void handleSuccessResponse(Response response, Movie movie) {
		Document document = Jsoup.parse(response.getResponseBody());
		Element body = document.body();
		
		populateStreamUrls(body, movie);
		populateImdbRating(body, movie);
		populateDuration(body, movie);
	}

	private void populateStreamUrls(Element body, Movie movie) {
		Elements streamElements = body.getElementsByAttributeValueStarting("title", "Stream on ");
		
		for(Element streamElement : streamElements) {
			String url = streamElement.getElementsByTag("a").get(0).attr("href");
			
			switch(streamElement.attr("title")) {
			case "Stream on Netflix" : movie.addWatchUrl(Service.NETFLIX, url); break;
			case "Stream on Prime Video" : movie.addWatchUrl(Service.PRIME, url); break;
			case "Stream on Hulu" : movie.addWatchUrl(Service.HULU, url); break;
			case "Stream on Disney+" : movie.addWatchUrl(Service.DISNEY, url); break;
			case "Stream on Paramount+" : movie.addWatchUrl(Service.PARAMOUNT, url); break;
			case "Stream on Starz" : movie.addWatchUrl(Service.STARZ, url); break;
			case "Stream on Showtime" : movie.addWatchUrl(Service.SHOWTIME, url); break;
			case "Stream on Peacock" : movie.addWatchUrl(Service.PEACOCK, url); break;
			default: //unknown service
			}
		}
	}

	private void populateImdbRating(Element body, Movie movie) {
		String imdbRating = null;
		
		try {
			imdbRating = body.getElementsByAttributeValue("title", "IMDB Rating").get(0)
                                    .getElementsByTag("div").get(0)
                                    .getElementsByTag("span").get(0)
                                    .text();
		} catch(Exception e) {
			log.error("Could not find imdb rating for movie={}", movie.getId());
		}
		
		if(imdbRating != null) {
			try {
				movie.setRating(Double.parseDouble(imdbRating));
			} catch(NumberFormatException|NullPointerException e) {
				log.error("Could not parse imdb rating for movie={}, rating={}", movie.getId(), imdbRating);
			}
		}
	}
	
	private void populateDuration(Element body, Movie movie) {
		String durationString = null;
		try {
			durationString = body.getElementsByAttributeValue("itemprop", "duration").get(0)
					             .parent()
					             .text();
		} catch(Exception e) {
			log.error("Could not find duration for movie={}", movie.getId());
		}
		
		if(durationString != null) {
			String[] durationStrings = durationString.split(" ");
			try {
				int hours = Integer.parseInt(durationStrings[0].substring(0, durationStrings[0].length()-1));
				int minutes = Integer.parseInt(durationStrings[1].substring(0, durationStrings[1].length()-1));
				int duration = hours*60 + minutes;
				movie.setDuration(duration);
			} catch(NumberFormatException|ArrayIndexOutOfBoundsException e) {
				log.error("Could not parse duration for movie={}, duration={}", movie.getId(), durationString);
			}
		}			
	}



}
