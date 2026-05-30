package com.magenta.products.domain.port.out;

import com.magenta.products.domain.model.Favorite;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository {
    void save(Favorite favorite);
    void delete(UUID customerId, UUID propertyId);
    List<Favorite> findByCustomerId(UUID customerId);
    boolean exists(UUID customerId, UUID propertyId);
}
