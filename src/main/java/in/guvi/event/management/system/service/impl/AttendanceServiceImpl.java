package in.guvi.event.management.system.service.impl;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.enums.EventStatus;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.repository.RegistrationRepository;
import in.guvi.event.management.system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository        eventRepository;

    @Override
    public void markAttendance(Long eventId, List<Long> attendedUserIds) {
        List<Registration> registrations =
            registrationRepository.findByEventIdWithUser(eventId);

        for (Registration reg : registrations) {
            boolean attended = attendedUserIds != null &&
                               attendedUserIds.contains(reg.getUser().getId());
            reg.setAttended(attended);
        }

        registrationRepository.saveAll(registrations);
        log.info("Attendance marked for event id={}, {} attendees marked",
                 eventId, attendedUserIds != null ? attendedUserIds.size() : 0);
    }

    @Override
    public void markSelfAttendance(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (event.getStatus() != EventStatus.ONGOING) {
            throw new IllegalStateException(
                "Self-attendance is only available while the event is ongoing.");
        }

        Registration reg = registrationRepository.findByUserIdAndEventId(userId, eventId)
            .orElseThrow(() -> new IllegalStateException(
                "You are not registered for this event."));

        if (reg.isAttended()) {
            log.info("User id={} already marked attended for event id={}", userId, eventId);
            return; // idempotent
        }

        reg.setAttended(true);
        registrationRepository.save(reg);
        log.info("User id={} self-marked attendance for event id={}", userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAttendedForEvent(Long eventId) {
        return registrationRepository.findByEventIdWithUser(eventId)
            .stream()
            .filter(Registration::isAttended)
            .count();
    }
}
