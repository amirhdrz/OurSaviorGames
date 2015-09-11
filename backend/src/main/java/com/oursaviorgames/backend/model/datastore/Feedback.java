package com.oursaviorgames.backend.model.datastore;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;

@Entity
public class Feedback {

    @Id
    private Long   EF_Id;
    private String EF_Email;
    private String EF_Message;
    private Date   EF_TimeStamp;
    //TODO: add ip address
    //TODO: add reviewed flag
    //TODO: sort by timestamp in admin console and allow to filter those not seen.

    /**
     * Private default constructor for Objectify.
     */
    @SuppressWarnings("unused")
    private Feedback() {
    }

    public Feedback(long id, String email, String message, Date timestamp) {
        EF_Id = id;
        EF_Email = email;
		EF_Message = message;
		EF_TimeStamp = timestamp;
	}
	
    public String getEmail() {
        return EF_Email;
    }

    public String getMessage() {
		return EF_Message;
	}

	public Date getTimestamp() {
		return EF_TimeStamp;
	}
}
