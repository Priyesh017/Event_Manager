package in.guvi.event.management.system.scheduler;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.service.EmailService;
import in.guvi.event.management.system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailReminderScheduler {

    private final EventRepository eventRepository;
    private final RegistrationService registrationService;
    private final EmailService emailService;

    /**
     * Runs every day at 9:00 AM.
     * Finds events happening within the next 20–28 hours and sends reminder emails.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(20);
        LocalDateTime to = LocalDateTime.now().plusHours(28);

        List<Event> upcomingEvents = eventRepository.findEventsBetween(from, to);
        log.info("Reminder job: found {} event(s) for tomorrow", upcomingEvents.size());

        for (Event event : upcomingEvents) {
            List<Registration> registrations =
                registrationService.getRegistrationsForEvent(event.getId());

            for (Registration reg : registrations) {
                emailService.sendEventReminder(reg.getUser(), event);
            }
            log.info("Sent {} reminder(s) for event '{}'",
                     registrations.size(), event.getTitle());
        }
    }
}
