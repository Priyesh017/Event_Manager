package in.guvi.event.management.system.controller.admin;

import in.guvi.event.management.system.dto.SpeakerDto;
import in.guvi.event.management.system.entity.Speaker;
import in.guvi.event.management.system.service.SpeakerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/speakers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSpeakerController {

    private final SpeakerService speakerService;

    @GetMapping
    public String listSpeakers(Model model) {
        model.addAttribute("speakers", speakerService.findAll());
        return "admin/speakers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("speakerDto", new SpeakerDto());
        return "admin/speakers/form";
    }

    @PostMapping("/create")
    public String createSpeaker(
            @Valid @ModelAttribute("speakerDto") SpeakerDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) return "admin/speakers/form";
        speakerService.createSpeaker(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Speaker created successfully!");
        return "redirect:/admin/speakers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Speaker speaker = speakerService.findById(id);
        model.addAttribute("speakerDto", speakerService.toDto(speaker));
        model.addAttribute("speakerId", id);
        return "admin/speakers/form";
    }

    @PostMapping("/{id}/edit")
    public String updateSpeaker(
            @PathVariable Long id,
            @Valid @ModelAttribute("speakerDto") SpeakerDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) return "admin/speakers/form";
        speakerService.updateSpeaker(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Speaker updated successfully!");
        return "redirect:/admin/speakers";
    }

    @PostMapping("/{id}/delete")
    public String deleteSpeaker(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        speakerService.deleteSpeaker(id);
        redirectAttributes.addFlashAttribute("successMessage", "Speaker deleted.");
        return "redirect:/admin/speakers";
    }
}
