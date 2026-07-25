package in.guvi.event.management.system.controller;

import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EventService eventService;

    @GetMapping("/")
    public String home(Model model) {
        EventSearchDto searchDto = new EventSearchDto();
        model.addAttribute("events",
            eventService.searchEvents(searchDto, PageRequest.of(0, 6)));
        model.addAttribute("totalEvents", eventService.countTotalEvents());
        model.addAttribute("upcomingEvents", eventService.countUpcomingEvents());
        return "index";
    }
}
