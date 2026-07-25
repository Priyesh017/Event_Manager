package in.guvi.event.management.system.repository;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.enums.EventCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // ─── Pessimistic lock for concurrent registration (Fix #1: Overbooking race condition)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    // Eagerly fetch speakers and createdBy to avoid LazyInitializationException in Thymeleaf views
    @EntityGraph(attributePaths = {"speakers", "createdBy"})
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findDetailById(@Param("id") Long id);

    // Atomic increment — only increments when count < capacity
    @Modifying
    @Query("UPDATE Event e SET e.registrationCount = e.registrationCount + 1 WHERE e.id = :id AND e.registrationCount < e.capacity")
    int atomicIncrementCount(@Param("id") Long id);

    // Atomic decrement — only decrements when count > 0
    @Modifying
    @Query("UPDATE Event e SET e.registrationCount = e.registrationCount - 1 WHERE e.id = :id AND e.registrationCount > 0")
    int atomicDecrementCount(@Param("id") Long id);

    // Search events by keyword (title or description), category, location, and date range
    @EntityGraph(attributePaths = {"speakers", "createdBy"}) // Fix #5: prevents N+1 on speakers
    @Query("""
        SELECT e FROM Event e
        WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%')))
        AND (:category IS NULL OR e.category = :category)
        AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', CAST(:location AS String), '%')))
        AND (CAST(:dateFrom AS timestamp) IS NULL OR e.eventDate >= :dateFrom)
        AND (CAST(:dateTo AS timestamp) IS NULL OR e.eventDate <= :dateTo)
        ORDER BY e.eventDate ASC
        """)
    Page<Event> searchEvents(
        @Param("keyword") String keyword,
        @Param("category") EventCategory category,
        @Param("location") String location,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        Pageable pageable
    );

    // Find upcoming events (for email reminders)
    @Query("""
        SELECT e FROM Event e
        WHERE e.eventDate BETWEEN :from AND :to
        ORDER BY e.eventDate ASC
        """)
    List<Event> findEventsBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    // Find events created by a specific admin
    Page<Event> findByCreatedByIdOrderByEventDateDesc(Long adminId, Pageable pageable);

    // Count upcoming events
    long countByEventDateAfter(LocalDateTime date);
}
