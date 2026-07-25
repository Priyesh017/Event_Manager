package in.guvi.event.management.system.service.impl;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.exception.DuplicateRegistrationException;
import in.guvi.event.management.system.exception.EventFullException;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.repository.RegistrationRepository;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.EmailService;
import in.guvi.event.management.system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public Registration register(Long userId, Long eventId) {
        // Duplicate check
        if (registrationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new DuplicateRegistrationException(
                "You are already registered for this event.");
        }

        // Load event with a PESSIMISTIC WRITE lock to prevent overbooking
        // under concurrent requests (Fix #1: Race Condition)
        Event event = eventRepository.findByIdWithLock(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        // Fix #3: Prevent registration for past events
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                "Cannot register for an event that has already passed.");
        }

        // Capacity check
        if (!event.isAvailable()) {
            throw new EventFullException(
                "This event is fully booked. No seats available.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Save registration
        Registration registration = Registration.builder()
            .user(user)
            .event(event)
            .attended(false)
            .build();
        Registration saved = registrationRepository.save(registration);

        // Atomic increment — only increments if count < capacity at DB level (Fix #1)
        int updated = eventRepository.atomicIncrementCount(eventId);
        if (updated == 0) {
            // Another concurrent request filled the last seat between our lock and now
            registrationRepository.delete(saved);
            throw new EventFullException(
                "This event is fully booked. No seats available.");
        }

        log.info("User {} registered for event '{}'", userId, event.getTitle());

        // Send confirmation email (async, failure won't break registration)
        emailService.sendRegistrationConfirmation(user, event);

        return saved;
    }

    @Override
    public void cancelRegistration(Long userId, Long eventId) {
        Registration reg = registrationRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Registration not found for userId=" + userId + ", eventId=" + eventId));

        registrationRepository.delete(reg);

        // Atomic decrement — safe under concurrency (Fix #1)
        eventRepository.atomicDecrementCount(eventId);

        log.info("User {} cancelled registration for event id={}", userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsForUser(Long userId) {
        return registrationRepository.findByUserIdWithEvent(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsForEvent(Long eventId) {
        return registrationRepository.findByEventIdWithUser(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRegistered(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttended(Long userId, Long eventId) {
        return registrationRepository.findByUserIdAndEventId(userId, eventId)
            .map(Registration::isAttended)
            .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalRegistrations() {
        return registrationRepository.count();
    }
}
