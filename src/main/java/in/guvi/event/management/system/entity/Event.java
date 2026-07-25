package in.guvi.event.management.system.entity;

import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventCategory category;

    /** Start date/time of the event (stored as event_date for backwards compatibility) */
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    /** End date/time of the event */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private int capacity = 100;

    @Column(name = "registration_count", nullable = false)
    @Builder.Default
    private int registrationCount = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
        name = "event_speakers",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "speaker_id")
    )
    @Builder.Default
    private List<Speaker> speakers = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Computed helpers ─────────────────────────────────────────────────────

    public boolean isAvailable() {
        return registrationCount < capacity;
    }

    public int getAvailableSeats() {
        return capacity - registrationCount;
    }

    /** Derived status based on current time vs eventDate / endDate */
    @Transient
    public EventStatus getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(eventDate)) return EventStatus.UPCOMING;
        if (now.isAfter(endDate))    return EventStatus.ENDED;
        return EventStatus.ONGOING;
    }

    /** True only while the event is actively running */
    @Transient
    public boolean isOngoing() {
        return getStatus() == EventStatus.ONGOING;
    }
}
