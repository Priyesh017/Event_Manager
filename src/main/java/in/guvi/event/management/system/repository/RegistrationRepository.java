package in.guvi.event.management.system.repository;

import in.guvi.event.management.system.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find registration by user and event (for duplicate check)
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    // Check if user is already registered
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Get all registrations for a user (for My Registrations page)
    @Query("""
        SELECT r FROM Registration r
        JOIN FETCH r.event e
        WHERE r.user.id = :userId
        ORDER BY e.eventDate ASC
        """)
    List<Registration> findByUserIdWithEvent(@Param("userId") Long userId);

    // Get all registrations for an event (for admin attendance page)
    @Query("""
        SELECT r FROM Registration r
        JOIN FETCH r.user u
        WHERE r.event.id = :eventId
        ORDER BY r.registeredAt ASC
        """)
    List<Registration> findByEventIdWithUser(@Param("eventId") Long eventId);

    // Count registrations per event
    long countByEventId(Long eventId);

    // Count total registrations (for admin dashboard)
    long count();
}
