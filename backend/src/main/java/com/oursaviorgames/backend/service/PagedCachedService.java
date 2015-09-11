package com.oursaviorgames.backend.service;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.memcache.MemcacheService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service abstraction that caches its data in pages.
 * This service does not store any state.
 */
//TODO: effed up logic, need to re-written.
    // token pattern limits to 9 pages to be cached, should handle null token more gracefully.
abstract class PagedCachedService<T> extends CachedService {

    /**
     * Token pattern for cached pages.
     * Page numbers start with 0.
     */
    private static final String TOKEN_PATTERN = "^page[0-9]*$";

    /**
     * Number of items in each page.
     */
    private final int ITEMS_PER_PAGE;

    /**
     * Number of pages that are cached.
     */
    private final int PAGES_TO_CACHE;

    /**
     * Constructor.
     * @param itemsPerPage
     * @param pagesToCache
     * @param cacheNameSpace
     */
    public PagedCachedService(int itemsPerPage, int pagesToCache, String cacheNameSpace) {
        super(cacheNameSpace);
        ITEMS_PER_PAGE = itemsPerPage;
        PAGES_TO_CACHE = pagesToCache;
    }

    /**
     * The query to run when loading data from the datastore.
     * The query's chunk size and limit is set automatically.
     * @return
     */
    abstract public Query<T> createQueryObject(QueryParams queryParams);

    /**
     * Returns a page of result from cache if result is cached,
     * or queries the data without caching from datastore if nextPageToken is a cursor String.
     * @param cacheKeyPrefix A non-null cache-prefix for the page of data to retrieve.
     * @param nextPageToken Token supplied by a previous call to getPage().
     * @return
     * @throws java.lang.IllegalArgumentException if nextPageToken is not a valid token.
     */
    public final Page<T> getPage(String cacheKeyPrefix, String nextPageToken, QueryParams queryParams)
    throws IllegalArgumentException {
        int page = parseToken(nextPageToken);
        if (page != -1) {
            if (page < PAGES_TO_CACHE) {
                //TODO: handle multiple requests coming in.
                if (!getMemcache().contains(getCacheKey(cacheKeyPrefix, page))) {
                    // If the key is not in memcache, recache all the pages.
                    recacheAllPages(cacheKeyPrefix, queryParams);
                }
                // Returns the result from memcache.
                return (Page<T>) (getMemcache().get(getCacheKey(cacheKeyPrefix, page)));
            } else {
                throw new IllegalArgumentException("Token " + nextPageToken + " is invalid");
            }
        } else {
            // nextPageToken doesn't match pattern,
            // so it is most likely a cursor position string.
            return queryWithCursor(nextPageToken, queryParams);
        }
    }

    /**
     * Re-caches all the pages beginning with {@code cacheKeyPrefix}.
     * @param cacheKeyPrefix
     */
    public final void recacheAllPages(String cacheKeyPrefix, QueryParams queryParams) {
        String nextPageCursorString = null;
        for (int page = 0; page < PAGES_TO_CACHE; page++) {
            // Creates cache key for this page.
            String cacheKey = getCacheKey(cacheKeyPrefix, page);
            // Gets unique from memcache.
            MemcacheService.IdentifiableValue identifiable =
                    getMemcache().getIdentifiable(cacheKey);
            // Queries the datastore for current page.
            Page<T> resultPage = queryWithCursor(nextPageCursorString, queryParams);
            nextPageCursorString = resultPage.getToken();
            // If there is more result to cache and we haven't reached out page cache limit
            // set the token to the next page.
            if (nextPageCursorString != null && page < PAGES_TO_CACHE - 1) {
                resultPage.setToken("page" + String.valueOf(page + 1));
            }
            // Puts the result into memcache.
            boolean cachePutSuccess;
            if (identifiable != null) {
                cachePutSuccess = getMemcache ()
                        .putIfUntouched(cacheKey, identifiable, resultPage);
            } else {
                getMemcache().put(cacheKey, resultPage);
                cachePutSuccess = true;
            }
            // If resultPage does not have a nextPageToken,
            // or if memcache value has changed by another process,
            // escape.
            if (nextPageCursorString == null) { // || !cachePutSuccess) { TODO: handle multiple requests writing to cache.
                break;
            }
        }
    }

    /**
     * Queries the datastore starting at {@code startCursor}.
     * If there are more items in the query result
     * @param startCursor The start cursor. Can be null.
     * @return Page with token set to cursor next page or null if there are no more results
     *          to be retrieved.
     */
    private Page<T> queryWithCursor(String startCursor, QueryParams queryParams) throws IllegalArgumentException {
        List<T> queryResult = new ArrayList<>();
        Query<T> query = createQueryObject(queryParams).chunk(ITEMS_PER_PAGE + 1).limit(ITEMS_PER_PAGE + 1);
        if (startCursor != null) {
            try {
                query = query.startAt(Cursor.fromWebSafeString(startCursor));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cursor (" + startCursor + ") is not valid");
            }
        }
        QueryResultIterator<T> iterator = query.iterator();
        int count = ITEMS_PER_PAGE;
        while(count > 0 && iterator.hasNext()) {
            T entity = iterator.next();
            queryResult.add(entity);
            count--;
        }
        String nextPageToken = null;
        if (iterator.hasNext()) {
                nextPageToken = iterator.getCursor().toWebSafeString();
        }
        Page<T> result = new Page<>();
        result.setItems(queryResult);
        result.setToken(nextPageToken);
        return result;
    }

    /**
     * Returns cache key based on cachePrefix and result page number.
     * @param cachePrefix
     * @param page
     * @return
     */
    private String getCacheKey(String cachePrefix, int page) {
        return cachePrefix + "|page" + String.valueOf(page);
    }

    /**
     * Returns the page number from token or -1 if token doesn't match the page pattern.
     * <p>
     * Null token implies page 0.
     * <p>
     * If token doesn't match the page pattern, then it should be regarded as cursor position.
     * @param token Token supplied by a previous call to getPage() or null if not available.
     * @return Page number from 0 to PAGES_TO_CACHE, or -1.
     */
    private int parseToken(String token) {
        if (token == null) {
            return 0;
        } else if (token.matches(TOKEN_PATTERN)) {
            int pageNumber = Integer.valueOf(token.substring(4));
            if (pageNumber < PAGES_TO_CACHE) {
                return pageNumber;
            }
        }
        return -1;
    }

    /**
     * Query parameters passed to {@link PagedCachedService#createQueryObject(com.oursaviorgames.backend.service.PagedCachedService.QueryParams)}
     * during a call to {@link PagedCachedService#getPage(String, String, com.oursaviorgames.backend.service.PagedCachedService.QueryParams)} ()}.
     */
    public final static class QueryParams {

        Map<String, Object> params = new HashMap<>();

        public void put(String key, Object value) {
            params.put(key, value);
        }

        public Object get(String key) {
            return params.get(key);
        }
    }
}
