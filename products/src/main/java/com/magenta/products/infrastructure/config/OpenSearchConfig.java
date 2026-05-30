package com.magenta.products.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class OpenSearchConfig {

    @Value("${magenta.search.opensearch-uri}")
    private String opensearchUri;

    @Bean
    public OpenSearchClient openSearchClient() throws Exception {
        URI uri = URI.create(opensearchUri);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        var transport = ApacheHttpClient5TransportBuilder
                .builder(new org.apache.hc.core5.http.HttpHost(
                        uri.getScheme(), uri.getHost(), uri.getPort()))
                .build();
        return new OpenSearchClient(transport);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
