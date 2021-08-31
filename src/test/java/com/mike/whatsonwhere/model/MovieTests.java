package com.mike.whatsonwhere.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mike.whatsonwhere.model.Movie.Service;

public class MovieTests {
	
	@Test
	public void special_characters_removed_in_id_creation() {
		//commas and spaces should be replaced with hyphens
		//other characters should be removed
		String name = "m, o,v i[e]n:a\"m'e...moviename!";
		//-year should be appended
		int year = 1970;
		String expectedId = "m-o-v-ienamemoviename-1970";
		
		Movie sut = new Movie(name, year);
		
		assertEquals(expectedId, sut.getId());
	}
	
	@Test
	public void movie_created_with_no_watch_urls() {
		Movie sut = new Movie("moviename", 1970);
		
		assertEquals(0, sut.getWatchUrls().size());
	}
	
	@Test
	public void adding_watch_url_means_it_is_streamable() {
		//arrange
		Movie sut = new Movie("moviename", 1970);
		
		//act
		sut.addWatchUrl(Service.DISNEY, "disney-url");
		sut.addWatchUrl(Service.NETFLIX, "netflix-url");
		
		//assert
		assertEquals(2, sut.getWatchUrls().size());
		assertEquals(sut.isStreamableOn(Service.DISNEY), true);
		assertEquals(sut.isStreamableOn(Service.NETFLIX), true);
		assertEquals(sut.isStreamableOn(Service.HULU), false);
		
	}

}
