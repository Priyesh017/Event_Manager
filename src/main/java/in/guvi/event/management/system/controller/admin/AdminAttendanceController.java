package in.guvi.event.management.system.controller.admin;

import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.service.AttendanceService;
import in.guvi.event.management.system.service.EventService;
import in.guvi.event.management.system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/attendance")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final AttendanceService attendanceService;

    @GetMapping("/{eventId}")
    public String viewAttendance(@PathVariable Long eventId, Model model) {
        List<Registration> registrations =
            registrationService.getRegistrationsForEvent(eventId);
        model.addAttribute("event", eventService.findById(eventId));
        model.addAttribute("registrations", registrations);
        model.addAttribute("attendedCount",
            attendanceService.countAttendedForEvent(eventId));
        return "admin/attendance/view";
    }

    @PostMapping("/{eventId}/mark")
    public String markAttendance(
            @PathVariable Long eventId,
            @RequestParam(required = false) List<Long> attendedUserIds,
            RedirectAttributes redirectAttributes) {

        attendanceService.markAttendance(eventId, attendedUserIds);
        redirectAttributes.addFlashAttribute("successMessage", "Attendance saved successfully!");
        return "redirect:/admin/attendance/" + eventId;
    }
}
