package com.magenta.products.application;

import com.magenta.products.domain.model.Favorite;
import com.magenta.products.domain.port.out.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * UC-P6: Gestión de favoritos.
 */
@Service
@Transactional
public class FavoriteUseCase {

    private final FavoriteRepository favoriteRepository;

    public FavoriteUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public void add(UUID customerId, UUID propertyId) {
        if (!favoriteRepository.exists(customerId, propertyId)) {
            favoriteRepository.save(new Favorite(customerId, propertyId, Instant.now()));
        }
    }

    public void remove(UUID customerId, UUID propertyId) {
        favoriteRepository.delete(customerId, propertyId);
    }

    @Transactional(readOnly = true)
    public List<Favorite> listByCustomer(UUID customerId) {
        return favoriteRepository.findByCustomerId(customerId);
    }
}
