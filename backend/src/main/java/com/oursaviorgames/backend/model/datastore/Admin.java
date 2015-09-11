package com.oursaviorgames.backend.model.datastore;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * This class represents an admin user.
 */
@Entity
public class Admin {

    @Id
    private String EF_UserId;
    private String EF_Name;
    private String EF_Email;

    @SuppressWarnings("unused")
    private Admin() {
    }

    public Admin(String userId, String name, String email) {
        this.EF_UserId = userId;
        this.EF_Name = name;
        this.EF_Email = email;
    }

    public String getUserId() {
        return EF_UserId;
    }

    public String getName() {
        return EF_Name;
    }

    public String getEmail() {
        return EF_Email;
    }

}
