package com.oursaviorgames.backend.model.request;

/**
 * POJO representing developer profile form on the client side.
 */
public class DeveloperForm {

	private String name;
	private String email;
	private String website;
	
	@SuppressWarnings("unused")
	private DeveloperForm(){
        // required empty constructor
    };
	
	public DeveloperForm(String name, String email, String website) {
		//TODO validate name, email, website
		this.name = name;
		this.email = email;
		this.website = website;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getWebsite() {
		return website;
	}
}
