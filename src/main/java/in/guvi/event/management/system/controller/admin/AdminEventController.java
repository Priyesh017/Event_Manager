package in.guvi.event.management.system.controller.admin;

import in.guvi.event.management.system.dto.EventDto;
import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.service.EventService;
import in.guvi.event.management.system.service.SpeakerService;
import in.guvi.event.management.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;
    private final SpeakerService speakerService;
    private final UserService userService;

    @GetMapping
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        EventSearchDto searchDto = new EventSearchDto();
        Page<Event> events = eventService.searchEvents(
            searchDto,
            PageRequest.of(page, size, Sort.by("eventDate").descending())
        );
        model.addAttribute("events", events);
        model.addAttribute("currentPage", page);
        return "admin/events/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("eventDto", new EventDto());
        model.addAttribute("speakers", speakerService.findAll());
        model.addAttribute("categories", EventCategory.values());
        return "admin/events/form";
    }

    @PostMapping("/create")
    public String createEvent(
            @Valid @ModelAttribute("eventDto") EventDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("speakers", speakerService.findAll());
            model.addAttribute("categories", EventCategory.values());
            return "admin/events/form";
        }

        User admin = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        eventService.createEvent(dto, admin.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/admin/events";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id);
        model.addAttribute("eventDto", eventService.toDto(event));
        model.addAttribute("event", event);
        model.addAttribute("speakers", speakerService.findAll());
        model.addAttribute("categories", EventCategory.values());
        return "admin/events/form";
    }

    @PostMapping("/{id}/edit")
    public String updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("eventDto") EventDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("speakers", speakerService.findAll());
            model.addAttribute("categories", EventCategory.values());
            return "admin/events/form";
        }

        eventService.updateEvent(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return "redirect:/admin/events";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Event deleted.");
        return "redirect:/admin/events";
    }
}
