package com.oursaviorgames.android.data;


/**
 * Contains convenience functions to build a data contract.
 */
class DataContractUtils {

    /**
     * Builds 'vendor-specific' Mime type String for multiple rows ('dir').
     */
    public static String dirMimeType(String contentAuthority, String path) {
        return "vnd.android.cursor.dir/" + contentAuthority + "/" + path;
    }

    /**
     * Builds 'vendor-specific' Mime type String for single row ('item').
     */
    public static String itemMimeType(String contentAuthority, String path) {
        return "vnd.android.cursor.item/" + contentAuthority + "/" + path;
    }

}
