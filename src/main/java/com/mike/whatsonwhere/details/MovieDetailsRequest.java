package com.mike.whatsonwhere.details;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.mike.whatsonwhere.model.Movie.Service;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class MovieDetailsRequest {
	
	private String baseUrl;
	private EnumSet<Service> services;
	@Singular private Map<String, String> queryParams;
	@Singular private Map<String, List<String>> headers;
	
	
	public String getCompleteUrl() {
		StringBuilder sb = new StringBuilder(baseUrl);
		sb.append("?");
		queryParams.entrySet().stream().forEach(param -> sb.append(param.getKey())
				                                           .append("=")
				                                           .append(param.getValue())
				                                           .append("&"));
		return sb.toString();
	}
	
	
}
