package in.guvi.event.management.system.service.impl;

import in.guvi.event.management.system.dto.EventDto;
import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.entity.Speaker;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.repository.RegistrationRepository;
import in.guvi.event.management.system.repository.SpeakerRepository;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.EmailService;
import in.guvi.event.management.system.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository         eventRepository;
    private final SpeakerRepository       speakerRepository;
    private final UserRepository          userRepository;
    private final RegistrationRepository  registrationRepository;
    private final EmailService            emailService;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    public Event createEvent(EventDto dto, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", adminUserId));

        validateDates(dto);
        List<Speaker> speakers = resolveSpeakers(dto.getSpeakerIds());

        Event event = Event.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .category(dto.getCategory())
            .eventDate(dto.getEventDate())
            .endDate(dto.getEndDate())
            .venue(dto.getVenue())
            .location(dto.getLocation())
            .capacity(dto.getCapacity())
            .imageUrl(dto.getImageUrl())
            .createdBy(admin)
            .speakers(speakers)
            .build();

        Event saved = eventRepository.save(event);
        log.info("Event created: '{}' by admin id={}", saved.getTitle(), adminUserId);
        return saved;
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    public Event updateEvent(Long id, EventDto dto) {
        Event event = findById(id);
        List<Speaker> speakers = resolveSpeakers(dto.getSpeakerIds());

        validateDates(dto);

        if (dto.getCapacity() < event.getRegistrationCount()) {
            throw new IllegalArgumentException(
                "Capacity cannot be reduced below current registration count (" + event.getRegistrationCount() + ")");
        }

        // ── Detect changes before applying them ─────────────────────────────
        List<String> changes = new ArrayList<>();
        if (!event.getTitle().equals(dto.getTitle()))
            changes.add("• Title changed to: " + dto.getTitle());
        if (!event.getDescription().equals(dto.getDescription()))
            changes.add("• Description updated");
        if (!event.getCategory().equals(dto.getCategory()))
            changes.add("• Category changed to: " + dto.getCategory().name());
        if (!event.getEventDate().equals(dto.getEventDate()))
            changes.add("• Start date/time changed to: " + dto.getEventDate());
        if (!event.getEndDate().equals(dto.getEndDate()))
            changes.add("• End date/time changed to: " + dto.getEndDate());
        if (!event.getVenue().equals(dto.getVenue()))
            changes.add("• Venue changed to: " + dto.getVenue());
        if (!event.getLocation().equals(dto.getLocation()))
            changes.add("• Location changed to: " + dto.getLocation());

        // ── Apply changes ─────────────────────────────────────────────────
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setCategory(dto.getCategory());
        event.setEventDate(dto.getEventDate());
        event.setEndDate(dto.getEndDate());
        event.setVenue(dto.getVenue());
        event.setLocation(dto.getLocation());
        event.setCapacity(dto.getCapacity());
        event.setImageUrl(dto.getImageUrl());
        event.setSpeakers(speakers);

        Event saved = eventRepository.save(event);
        log.info("Event updated: id={} '{}', {} field(s) changed", id, saved.getTitle(), changes.size());

        // ── Notify registered users if anything meaningful changed ────────
        if (!changes.isEmpty()) {
            String changesSummary = String.join("\n", changes);
            List<Registration> registrations = registrationRepository.findByEventIdWithUser(id);
            for (Registration reg : registrations) {
                try {
                    emailService.sendEventUpdateNotification(reg.getUser(), saved, changesSummary);
                } catch (Exception ex) {
                    log.warn("Failed to send update notification to user id={}: {}",
                             reg.getUser().getId(), ex.getMessage());
                }
            }
        }

        return saved;
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event", id);
        }
        eventRepository.deleteById(id);
        log.info("Event deleted: id={}", id);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> searchEvents(EventSearchDto searchDto, Pageable pageable) {
        String keyword  = blankToNull(searchDto.getKeyword());
        String location = blankToNull(searchDto.getLocation());
        EventCategory category = searchDto.getCategory();

        LocalDateTime dateFrom = null;
        LocalDateTime dateTo   = null;
        if (searchDto.getDate() != null) {
            dateFrom = searchDto.getDate().atStartOfDay();
            dateTo   = searchDto.getDate().atTime(23, 59, 59);
        }

        return eventRepository.searchEvents(keyword, category, location, dateFrom, dateTo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getUpcomingEvents() {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime plus24h = now.plusHours(24);
        return eventRepository.findEventsBetween(now, plus24h);
    }

    @Override
    public EventDto toDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setCategory(event.getCategory());
        dto.setEventDate(event.getEventDate());
        dto.setEndDate(event.getEndDate());
        dto.setVenue(event.getVenue());
        dto.setLocation(event.getLocation());
        dto.setCapacity(event.getCapacity());
        dto.setImageUrl(event.getImageUrl());
        dto.setSpeakerIds(
            event.getSpeakers().stream().map(Speaker::getId).toList()
        );
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUpcomingEvents() {
        return eventRepository.countByEventDateAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalEvents() {
        return eventRepository.count();
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void validateDates(EventDto dto) {
        if (dto.getEndDate() != null && dto.getEventDate() != null
                && !dto.getEndDate().isAfter(dto.getEventDate())) {
            throw new IllegalArgumentException("End date/time must be after start date/time.");
        }
    }

    private List<Speaker> resolveSpeakers(List<Long> speakerIds) {
        if (speakerIds == null || speakerIds.isEmpty()) return List.of();
        return speakerRepository.findAllById(speakerIds);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
