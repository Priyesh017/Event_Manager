package in.guvi.event.management.system.controller;

import in.guvi.event.management.system.dto.UserRegistrationDto;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.service.EmailService;
import in.guvi.event.management.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            User user = userService.registerUser(dto);
            emailService.sendWelcomeEmail(user);
            redirectAttributes.addFlashAttribute("successMessage",
                "Account created successfully! Please login.");
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException ex) {
            result.rejectValue("email", "error.email", ex.getMessage());
            return "auth/register";
        }
    }
}
