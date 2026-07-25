package in.guvi.event.management.system.service;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.User;

public interface EmailService {

    void sendRegistrationConfirmation(User user, Event event);

    void sendEventReminder(User user, Event event);

    void sendWelcomeEmail(User user);

    void sendEventUpdateNotification(User user, Event event, String changesSummary);
}
