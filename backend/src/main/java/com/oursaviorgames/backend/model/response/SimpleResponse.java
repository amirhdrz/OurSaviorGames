package com.oursaviorgames.backend.model.response;

/**
 * Represents a simple String response.
 *
 */
public class SimpleResponse {
	
	private final String result;
	
	public SimpleResponse(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return result;
	}
}
