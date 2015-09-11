package com.oursaviorgames.backend.model.datastore;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

import com.oursaviorgames.backend.utils.TimeUtils;
import com.oursaviorgames.backend.model.request.DeveloperForm;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

@Entity
@Cache
public class Developer {

    @Id
    private Long   EF_Id;          // Developer id
    private String EF_Name;        // Developer name
    private String EF_Email;       // Developer email
    private String EF_Website;     // Developer website
    private Date   EF_DateJoined;  // Date of entity creation

    /**
     * Private default constructor for Objectify.
     */
    @SuppressWarnings("unused")
    private Developer() {
    }

    /**
     * Constructor for Developer
     *
     * @param id
     *            Cannot be null
     * @param name
     *            Cannot be null
     * @param email
     *            Cannot be null
     * @param website Nullable
     */
    public Developer(Long id, String name, String email, String website) {
        this.EF_Id = checkNotNull(id, "Null developer id");
        this.EF_Name = checkNotNull(name, "Null developer name");
        this.EF_Email = email;
		this.EF_Website = website;
		this.EF_DateJoined = TimeUtils.getCurrentTime();
	}

	/**
	 * Updates developer profile information.
	 * @param form
	 */
	public void updateDeveloper(DeveloperForm form) {
		this.EF_Name = form.getName();
		this.EF_Email = form.getEmail();
		this.EF_Website = form.getWebsite();
	}

    /**
     * Returns developer id.
     * @return
     */
    public Long getId() {
        return EF_Id;
    }

	/**
	 * Getter for name.
	 * @return
	 */
	public String getName() {
		return EF_Name;
	}
	
	/**
	 * Getter for email.
	 * @return
	 */
	public String getEmail() {
		return EF_Email;
	}
	
	/**
	 * Getter for website.
	 * @return
	 */
	public String getWebsite() {
		return EF_Website;
	}
	
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Developer> getKey() {
		return Key.create(Developer.class, EF_Id);
	}

}
