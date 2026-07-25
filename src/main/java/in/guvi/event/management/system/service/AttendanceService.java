package in.guvi.event.management.system.service;

import java.util.List;

public interface AttendanceService {

    /** Admin bulk-mark attendance for an event */
    void markAttendance(Long eventId, List<Long> attendedUserIds);

    /** User self-marks their own attendance (only valid while event is ONGOING) */
    void markSelfAttendance(Long eventId, Long userId);

    long countAttendedForEvent(Long eventId);
}
