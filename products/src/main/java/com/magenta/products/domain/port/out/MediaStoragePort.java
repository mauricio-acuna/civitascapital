package com.magenta.products.domain.port.out;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public interface MediaStoragePort {
    /**
     * Stores raw media and returns the CDN URI.
     */
    String store(UUID propertyId, UUID assetId, String filename, String mimeType, InputStream content, long sizeBytes);

    /**
     * Generates a pre-signed URL (valid for 5 minutes) for the original file.
     */
    URL presignedUrl(String storageUri);

    void delete(String storageUri);
}
