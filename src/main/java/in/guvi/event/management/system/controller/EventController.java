package in.guvi.event.management.system.controller;

import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.service.AttendanceService;
import in.guvi.event.management.system.service.EventService;
import in.guvi.event.management.system.service.RegistrationService;
import in.guvi.event.management.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService        eventService;
    private final RegistrationService registrationService;
    private final UserService         userService;
    private final AttendanceService   attendanceService;

    @GetMapping
    public String listEvents(
            @ModelAttribute EventSearchDto searchDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);

        Page<Event> events = eventService.searchEvents(
            searchDto,
            PageRequest.of(safePage, safeSize, Sort.by("eventDate").ascending())
        );

        model.addAttribute("events",      events);
        model.addAttribute("searchDto",   searchDto);
        model.addAttribute("categories",  EventCategory.values());
        model.addAttribute("currentPage", safePage);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String viewEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Event event = eventService.findById(id);
        model.addAttribute("event", event);

        boolean isRegistered  = false;
        boolean hasAttended   = false;

        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                isRegistered = registrationService.isRegistered(currentUser.getId(), event.getId());
                if (isRegistered) {
                    hasAttended = registrationService.hasAttended(currentUser.getId(), event.getId());
                }
                model.addAttribute("currentUserId", currentUser.getId());
            }
        }

        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("hasAttended",  hasAttended);
        return "events/detail";
    }

    /** User self-marks attendance during the event window */
    @PostMapping("/{id}/attend")
    @PreAuthorize("isAuthenticated()")
    public String markSelfAttendance(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {

        try {
            User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
            attendanceService.markSelfAttendance(id, currentUser.getId());
            ra.addFlashAttribute("successMsg", "✅ Attendance marked! Welcome to the event.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMsg", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error marking self attendance for event id={}", id, ex);
            ra.addFlashAttribute("errorMsg", "Could not mark attendance. Please try again.");
        }
        return "redirect:/events/" + id;
    }
}
