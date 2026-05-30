package com.magenta.products.infrastructure.adapter.out.search;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.SearchIndexPort;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PropertySearchAdapter implements SearchIndexPort {

    private static final Logger log = LoggerFactory.getLogger(PropertySearchAdapter.class);

    private final OpenSearchClient client;

    @Value("${magenta.search.index:properties_v1}")
    private String indexName;

    public PropertySearchAdapter(OpenSearchClient client) {
        this.client = client;
    }

    @Override
    public void index(Property property) {
        PropertyIndexDocument doc = PropertyIndexDocument.from(property);
        try {
            client.index(IndexRequest.of(req -> req
                    .index(indexName)
                    .id(property.id().toString())
                    .document(doc)));
        } catch (Exception e) {
            log.error("Failed to index property {}: {}", property.id(), e.getMessage(), e);
            throw new SearchIndexException("Cannot index property " + property.id(), e);
        }
    }

    @Override
    public void delete(UUID propertyId) {
        try {
            client.delete(DeleteRequest.of(req -> req
                    .index(indexName)
                    .id(propertyId.toString())));
        } catch (Exception e) {
            log.warn("Failed to delete property {} from index: {}", propertyId, e.getMessage());
        }
    }

    @Override
    public void reindexByZone(UUID zoneId, List<Property> properties) {
        properties.forEach(this::index);
    }
}
