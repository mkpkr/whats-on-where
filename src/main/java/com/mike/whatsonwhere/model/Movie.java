package com.mike.whatsonwhere.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Movie {
	
	public enum Service {
		NETFLIX,
		AMAZON,
		HULU
	}
	
	private final String id;
	private final String name;
	private final int year;
	
	private int duration;
	private Map<Service, String> watchUrls;
	private double rating;
	
	public Movie(String name, String year) {
		this.id = parse(name, year);
		this.name = name;
		if(year.trim().isEmpty()) {
			this.year = -1;
		} else {
			
			//TODO probably should accept int year and do parsing elsewhere 
			
			this.year = Integer.parseInt(year);
		}
		
		watchUrls = new HashMap<>();
		
	}
	
	public void addWatchUrl(Service service, String url) {
		watchUrls.put(service, url);
	}
	
	public boolean isStreamableOn(Service service) {
		return watchUrls.containsKey(service);
	}
	
	private String parse(String name, String year) {
		
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
