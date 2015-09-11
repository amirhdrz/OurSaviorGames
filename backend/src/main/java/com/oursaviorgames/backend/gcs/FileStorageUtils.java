package com.oursaviorgames.backend.gcs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File utility functions for working with Google Cloud Storage.
 */
public class FileStorageUtils {

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    public static void copyStream(int bufferSize, InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

}
