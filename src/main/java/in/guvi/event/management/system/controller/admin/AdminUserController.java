package in.guvi.event.management.system.controller.admin;

import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users/list";
    }

    /** Update only the role of a user */
    @PostMapping("/{id}/role")
    public String updateRole(@PathVariable Long id,
                             @RequestParam("role") String role,
                             RedirectAttributes ra) {
        try {
            Role newRole = Role.valueOf(role);
            userService.updateUserRole(id, newRole);
            ra.addFlashAttribute("successMsg", "User role updated successfully.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMsg", "Invalid role: " + role);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMsg", "Failed to update role: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
