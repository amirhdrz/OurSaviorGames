package com.oursaviorgames.backend.model.response;

import java.util.Collection;

/**
 * A comment collection response.
 * <p>
 * Note that we can't user generics with Google Cloud Endpoints due to type erasure.
 */
public class CommentCollectionResponse extends TokenedResponse {

    private final Collection<CommentResponse> items;

    public CommentCollectionResponse (Collection<CommentResponse> items, String token) {
        super(token);
        this.items = items;
    }

    public Collection<CommentResponse> getItems() {
        return items;
    }

}
