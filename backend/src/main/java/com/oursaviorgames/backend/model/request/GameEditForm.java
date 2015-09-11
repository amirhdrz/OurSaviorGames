package com.oursaviorgames.backend.model.request;

/**
 * Admin form for editing Game entity
 */
public class GameEditForm {

    private String description;
    private Integer version;
    private Boolean isOffline;
    private String originUrl;
    private Boolean published;

    public GameEditForm() {
        // required empty constructor
    }

    public String getDescription() {
        return description;
    }

    public Integer getVersion() {
        return version;
    }

    public Boolean getIsOffline() {
        return isOffline;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public Boolean getPublished() {
        return published;
    }
}
