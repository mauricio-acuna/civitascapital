package com.magenta.customers.domain.port.out;

import java.io.InputStream;
import java.time.Duration;

public interface DocumentStoragePort {
    /**
     * Sube el fichero a S3/MinIO con cifrado server-side y devuelve la URI interna.
     * @param key       clave del objeto (path dentro del bucket)
     * @param data      stream del contenido
     * @param sizeBytes tamaño en bytes
     * @param mimeType  MIME type del fichero
     * @return          URI interna del objeto (s3://bucket/key)
     */
    String upload(String key, InputStream data, long sizeBytes, String mimeType);

    /**
     * Genera una URL pre-firmada con TTL para descarga directa (RGPD export).
     */
    String presignedDownloadUrl(String key, Duration ttl);

    /** Elimina el objeto del almacenamiento (crypto-shredding). */
    void delete(String key);
}
