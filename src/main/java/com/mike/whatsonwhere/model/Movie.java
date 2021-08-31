package com.mike.whatsonwhere.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Movie {
	
	public enum Service {
		DISNEY,
		HULU,
		NETFLIX,
		PARAMOUNT,
		PEACOCK,
		PRIME,
		SHOWTIME,
		STARZ
	}
	
	private final String id;
	private final String name;
	private final int year;
	
	private int duration;
	private Map<Service, String> watchUrls;
	private double rating;
	
	public Movie(String name, int year) {
		this.id = createId(name, year);
		this.name = name;
		this.year = year;
		
		watchUrls = new HashMap<>();
		
	}
	
	public void addWatchUrl(Service service, String url) {
		watchUrls.put(service, url);
	}
	
	public boolean isStreamableOn(Service service) {
		return watchUrls.containsKey(service);
	}
	
	private String createId(String name, int year) {
		
		//TODO should this parsing be here? 
		
		String id = name.toLowerCase()
				        .replace(", ", "-")
				        .replace(",", "-")
				        .replace(" ", "-")
				        .replace("[", "")
				        .replace("]", "")
				        .replace(":", "")
				        .replace("\"", "")
				        .replace("'", "")
				        .replace("...", "")
				        .replace("…", "")
				        .replace("!", "");
		
		return id + "-" + year;
	}	
	
}
