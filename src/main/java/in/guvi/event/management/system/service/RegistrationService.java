package in.guvi.event.management.system.service;

import in.guvi.event.management.system.entity.Registration;

import java.util.List;

public interface RegistrationService {

    Registration register(Long userId, Long eventId);

    void cancelRegistration(Long userId, Long eventId);

    List<Registration> getRegistrationsForUser(Long userId);

    List<Registration> getRegistrationsForEvent(Long eventId);

    boolean isRegistered(Long userId, Long eventId);

    boolean hasAttended(Long userId, Long eventId);

    long countTotalRegistrations();
}
