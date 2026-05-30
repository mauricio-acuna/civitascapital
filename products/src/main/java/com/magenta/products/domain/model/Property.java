package com.magenta.products.domain.model;

import com.magenta.products.domain.event.PropertyArchived;
import com.magenta.products.domain.event.PropertyCreated;
import com.magenta.products.domain.event.PropertyPublished;
import com.magenta.products.domain.event.PropertyUpdated;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate root: Property.
 * Domain layer — no Spring/JPA imports.
 */
public class Property {

    private final UUID id;
    private final UUID tenantId;
    private String reference;
    private PropertyType type;
    private String subtype;
    private PropertyStatus status;
    private OwnerInfo ownership;
    private Location location;
    private Surface surface;
    private Layout layout;
    private PropertyCondition condition;
    private Integer buildYear;
    private Integer lastRenovationYear;
    private EnergyRating energyRating;
    private Set<String> features;
    private Set<Orientation> orientation;
    private IteInfo ite;
    private Set<String> tags;
    private List<MediaAsset> media;
    private List<Operation> operations;
    private FinancingHint financing;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private long version;

    private final List<Object> domainEvents = new ArrayList<>();

    private Property() {}

    public static Property create(
            UUID id,
            UUID tenantId,
            String reference,
            PropertyType type,
            String subtype,
            OwnerInfo ownership,
            Location location,
            Surface surface,
            Layout layout,
            PropertyCondition condition,
            Integer buildYear,
            EnergyRating energyRating,
            Set<String> features,
            Set<Orientation> orientation,
            Set<String> tags,
            String createdBy) {

        Objects.requireNonNull(id, "id required");
        Objects.requireNonNull(tenantId, "tenantId required");
        Objects.requireNonNull(reference, "reference required");
        Objects.requireNonNull(type, "type required");
        Objects.requireNonNull(ownership, "ownership required");
        Objects.requireNonNull(location, "location required");
        Objects.requireNonNull(surface, "surface required");

        Property p = new Property();
        p.id = id;
        p.tenantId = tenantId;
        p.reference = reference;
        p.type = type;
        p.subtype = subtype;
        p.status = PropertyStatus.DRAFT;
        p.ownership = ownership;
        p.location = location;
        p.surface = surface;
        p.layout = layout != null ? layout : new Layout(null, null, null, null, null, null, null);
        p.condition = condition;
        p.buildYear = buildYear;
        p.energyRating = energyRating;
        p.features = features != null ? new LinkedHashSet<>(features) : new LinkedHashSet<>();
        p.orientation = orientation != null ? new LinkedHashSet<>(orientation) : new LinkedHashSet<>();
        p.tags = tags != null ? new LinkedHashSet<>(tags) : new LinkedHashSet<>();
        p.media = new ArrayList<>();
        p.operations = new ArrayList<>();
        p.financing = FinancingHint.empty();
        p.createdAt = Instant.now();
        p.updatedAt = p.createdAt;
        p.createdBy = createdBy;
        p.updatedBy = createdBy;
        p.version = 0;

        p.domainEvents.add(new PropertyCreated(id, tenantId, reference, type, createdBy, p.createdAt));
        return p;
    }

    /**
     * Publish the property.
     * Invariants (§3.3 spec):
     *  - ≥ 3 PHOTO media assets
     *  - ≥ 1 active operation
     *  - energyRating declared
     *  - zoneId resolved
     */
    public void publish(String updatedBy) {
        assertCanPublish();
        this.status = PropertyStatus.ACTIVE;
        this.publishedAt = Instant.now();
        this.updatedAt = this.publishedAt;
        this.updatedBy = updatedBy;
        this.version++;

        Operation activeOp = getActiveOperation();
        domainEvents.add(new PropertyPublished(
                id, tenantId, location.zoneId(), type,
                activeOp.type(), activeOp.price().amount(),
                surface.builtSqm(), layout.rooms(),
                location.coordinates(), features,
                energyRating.consumptionLetter(), publishedAt));
    }

    public void archive(String updatedBy) {
        if (status == PropertyStatus.ARCHIVED) return;
        this.status = PropertyStatus.ARCHIVED;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
        this.version++;
        domainEvents.add(new PropertyArchived(id, tenantId, updatedAt));
    }

    public void update(String updatedBy) {
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
        this.version++;
        domainEvents.add(new PropertyUpdated(id, tenantId, updatedAt));
    }

    public void addMedia(MediaAsset asset) {
        Objects.requireNonNull(asset);
        media.add(asset);
        this.updatedAt = Instant.now();
    }

    public void removeMedia(UUID mediaId) {
        media.removeIf(m -> m.id().equals(mediaId));
        this.updatedAt = Instant.now();
    }

    public void addOperation(Operation operation) {
        Objects.requireNonNull(operation);
        operations.add(operation);
        this.updatedAt = Instant.now();
    }

    public void updateFinancing(FinancingHint hint) {
        this.financing = Objects.requireNonNull(hint);
        this.updatedAt = Instant.now();
        this.version++;
    }

    private void assertCanPublish() {
        List<String> violations = new ArrayList<>();
        long photoCount = media.stream().filter(m -> m.kind() == MediaKind.PHOTO).count();
        if (photoCount < 3) violations.add("at least 3 photos required (found " + photoCount + ")");
        if (getActiveOperation() == null) violations.add("at least 1 active operation required");
        if (energyRating == null) violations.add("energyRating must be declared");
        if (location.zoneId() == null) violations.add("zoneId must be resolved");
        if (!violations.isEmpty()) {
            throw new PropertyPublishException(id, violations);
        }
    }

    private Operation getActiveOperation() {
        return operations.stream()
                .filter(o -> o.status() == OperationStatus.ACTIVE || o.status() == OperationStatus.DRAFT)
                .findFirst()
                .orElse(null);
    }

    public List<Object> pullDomainEvents() {
        List<Object> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    // ── Getters (no setters — immutable via methods) ──────────────────────────

    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public String reference() { return reference; }
    public PropertyType type() { return type; }
    public String subtype() { return subtype; }
    public PropertyStatus status() { return status; }
    public OwnerInfo ownership() { return ownership; }
    public Location location() { return location; }
    public Surface surface() { return surface; }
    public Layout layout() { return layout; }
    public PropertyCondition condition() { return condition; }
    public Integer buildYear() { return buildYear; }
    public Integer lastRenovationYear() { return lastRenovationYear; }
    public EnergyRating energyRating() { return energyRating; }
    public Set<String> features() { return Collections.unmodifiableSet(features); }
    public Set<Orientation> orientation() { return Collections.unmodifiableSet(orientation); }
    public IteInfo ite() { return ite; }
    public Set<String> tags() { return Collections.unmodifiableSet(tags); }
    public List<MediaAsset> media() { return Collections.unmodifiableList(media); }
    public List<Operation> operations() { return Collections.unmodifiableList(operations); }
    public FinancingHint financing() { return financing; }
    public Instant publishedAt() { return publishedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public String createdBy() { return createdBy; }
    public String updatedBy() { return updatedBy; }
    public long version() { return version; }

    // Mutable fields needed after reconstitution
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public void setCondition(PropertyCondition condition) { this.condition = condition; }
    public void setBuildYear(Integer buildYear) { this.buildYear = buildYear; }
    public void setLastRenovationYear(Integer year) { this.lastRenovationYear = year; }
    public void setEnergyRating(EnergyRating rating) { this.energyRating = rating; }
    public void setIte(IteInfo ite) { this.ite = ite; }
    public void setTags(Set<String> tags) { this.tags = new LinkedHashSet<>(tags); }
    public void setFeatures(Set<String> features) { this.features = new LinkedHashSet<>(features); }
    public void setOrientation(Set<Orientation> orientation) { this.orientation = new LinkedHashSet<>(orientation); }
    public void setLocation(Location location) { this.location = location; }
    public void setSurface(Surface surface) { this.surface = surface; }
    public void setLayout(Layout layout) { this.layout = layout; }
    public void setOwnership(OwnerInfo ownership) { this.ownership = ownership; }

    // Package-private for repository reconstitution
    void setStatus(PropertyStatus status) { this.status = status; }
    void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    void setVersion(long version) { this.version = version; }
    void setMediaInternal(List<MediaAsset> media) { this.media = new ArrayList<>(media); }
    void setOperationsInternal(List<Operation> ops) { this.operations = new ArrayList<>(ops); }
    void setFinancingInternal(FinancingHint hint) { this.financing = hint; }
}
