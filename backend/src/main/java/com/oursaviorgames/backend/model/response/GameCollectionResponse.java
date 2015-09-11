package com.oursaviorgames.backend.model.response;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

import com.oursaviorgames.backend.memcache.Cachable;

/**
 * Represents an ordered collection of entities and a cursor to the next page.
 */
//TODO: implement TokenedResponse
public class GameCollectionResponse implements Cachable {

    /** items saved by this list response */
    private final Collection<GameResponse> items;

    /** cursor pointing to next page of results */
    private final String cursorString;

    public GameCollectionResponse(Builder builder) {
        this.items = builder.items;
        this.cursorString = builder.cursorString;
    }

    /**
     * Returns immutable cursor to the next page of results.
     * @return
     * @throws NullPointerException
     */
    public String getNextPageToken() throws NullPointerException {
        return cursorString;
	}

	/**
	 * Returns immutable list of entities saved in this response.
	 * @return
	 */
	public Collection<GameResponse> getItems() {
		return items;
	}

	/**
	 * Returns a builder object to build a ListResponse.
	 * @return Builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for building a {@code GameCollectionResponse}.
	 *
	 */
	public static class Builder {
		private Collection<GameResponse> items;	// required
		private String cursorString;	// optional

		private Builder() {}

		/**
		 * Creates a immutable copy of items and saves it.
		 * @param items
		 * @return Builder
		 */
		public Builder setItems(Collection<GameResponse> items) {
			this.items = ImmutableList.copyOf(items);
			return this;
		}

		/**
		 * Sets the cursor string for next page of results.
		 * @param cursorString
		 * @return Builder
		 */
		public Builder setNextPageToken(String cursorString) {
			this.cursorString = cursorString;
			return this;
		}

		/**
		 * Builds the ListResponse object.
		 * @return ListResponse containing items and possibly cursor to next page.
		 */
		public GameCollectionResponse build() {
			return new GameCollectionResponse(this);
		}
	}

}
