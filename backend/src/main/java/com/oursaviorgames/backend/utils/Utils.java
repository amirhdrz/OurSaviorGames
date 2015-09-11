package com.oursaviorgames.backend.utils;

import com.google.common.io.BaseEncoding;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

public class Utils {

    /**
     * Converts base64 encoded String to long.
     * @param base64 Non-null base64 encoded String.
     * @throws IllegalArgumentException If input is not valid base64 String.
     */
    public static long decodeBase64String(String base64) throws IllegalArgumentException {
        checkNotNull(base64, "Null String");
        byte[] decodedBytes = BaseEncoding.base64().decode(base64);
        return Long.valueOf(new String(decodedBytes));
    }

}
