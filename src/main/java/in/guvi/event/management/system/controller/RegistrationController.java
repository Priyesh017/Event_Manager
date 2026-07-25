package in.guvi.event.management.system.controller;

import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.service.RegistrationService;
import in.guvi.event.management.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    @PostMapping("/registrations/register/{eventId}")
    public String register(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = resolveUser(userDetails);
        registrationService.register(user.getId(), eventId);
        redirectAttributes.addFlashAttribute("successMessage",
            "Successfully registered! Check your email for confirmation.");
        return "redirect:/my-registrations";
    }

    @PostMapping("/registrations/cancel/{eventId}")
    public String cancel(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = resolveUser(userDetails);
        registrationService.cancelRegistration(user.getId(), eventId);
        redirectAttributes.addFlashAttribute("successMessage",
            "Registration cancelled successfully.");
        return "redirect:/my-registrations";
    }

    @GetMapping("/my-registrations")
    public String myRegistrations(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = resolveUser(userDetails);
        List<Registration> registrations =
            registrationService.getRegistrationsForUser(user.getId());
        model.addAttribute("registrations", registrations);
        model.addAttribute("user", user);
        return "registrations/my-registrations";
    }

    private User resolveUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }
}
