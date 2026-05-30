package com.magenta.products.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_assets", schema = "products")
@Getter @Setter
public class MediaAssetJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @Column(name = "kind", nullable = false, length = 20)
    private String kind;

    @Column(name = "storage_uri", nullable = false)
    private String storageUri;

    @Column(name = "mime_type", nullable = false, length = 80)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "ai_tags", columnDefinition = "text[]")
    private String[] aiTags;

    @Column(name = "\"order\"", nullable = false)
    private int order;

    @Column(name = "is_cover", nullable = false)
    private boolean isCover;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;
}
