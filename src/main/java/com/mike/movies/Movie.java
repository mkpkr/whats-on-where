package com.mike.movies;

import lombok.Data;

@Data
public class Movie {
	
	private final String id;
	private final String name;
	private final int year;
	
	private int runningTime;
	private String director;
	private boolean netflix;
	
	public Movie(String name, String year) {
		this.id = parse(name, year);
		this.name = name;
		if(year.trim().isEmpty()) {
			this.year = -1;
		} else {
			
			//TODO probably should accept int year and do parsing elsewhere 
			
			this.year = Integer.parseInt(year);
		}
		
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
