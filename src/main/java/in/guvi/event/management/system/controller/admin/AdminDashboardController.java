package in.guvi.event.management.system.controller.admin;

import in.guvi.event.management.system.service.EventService;
import in.guvi.event.management.system.service.RegistrationService;
import in.guvi.event.management.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalEvents", eventService.countTotalEvents());
        model.addAttribute("upcomingEvents", eventService.countUpcomingEvents());
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("totalRegistrations", registrationService.countTotalRegistrations());
        return "admin/dashboard";
    }
}
