package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * UC-P3: Subida de media con almacenamiento en S3/CDN.
 */
@Service
@Transactional
public class UploadMediaUseCase {

    private final PropertyRepository propertyRepository;
    private final MediaStoragePort mediaStoragePort;
    private final DomainEventPublisher eventPublisher;

    public UploadMediaUseCase(PropertyRepository propertyRepository,
                               MediaStoragePort mediaStoragePort,
                               DomainEventPublisher eventPublisher) {
        this.propertyRepository = propertyRepository;
        this.mediaStoragePort = mediaStoragePort;
        this.eventPublisher = eventPublisher;
    }

    public MediaAsset execute(Command cmd) {
        Property property = propertyRepository.findById(cmd.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(cmd.propertyId()));

        UUID assetId = UUID.randomUUID();
        String storageUri = mediaStoragePort.store(
                cmd.propertyId(), assetId, cmd.filename(),
                cmd.mimeType(), cmd.content(), cmd.sizeBytes());

        boolean isCover = cmd.kind() == MediaKind.PHOTO
                && property.media().stream().noneMatch(m -> m.kind() == MediaKind.PHOTO && m.isCover());

        MediaAsset asset = new MediaAsset(
                assetId, cmd.propertyId(), cmd.kind(),
                storageUri, cmd.mimeType(), cmd.sizeBytes(),
                cmd.width(), cmd.height(), List.of(),
                property.media().size(), isCover, Instant.now());

        property.addMedia(asset);
        property.update(cmd.uploadedBy());
        propertyRepository.save(property);
        property.pullDomainEvents().forEach(eventPublisher::publish);
        return asset;
    }

    public void deleteMedia(UUID propertyId, UUID mediaId, String deletedBy) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        property.media().stream()
                .filter(m -> m.id().equals(mediaId))
                .findFirst()
                .ifPresent(m -> mediaStoragePort.delete(m.storageUri()));
        property.removeMedia(mediaId);
        property.update(deletedBy);
        propertyRepository.save(property);
    }

    public record Command(
            UUID propertyId,
            MediaKind kind,
            String filename,
            String mimeType,
            java.io.InputStream content,
            long sizeBytes,
            Integer width,
            Integer height,
            String uploadedBy) {}
}
