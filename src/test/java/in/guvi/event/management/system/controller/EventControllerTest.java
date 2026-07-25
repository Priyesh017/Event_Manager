package in.guvi.event.management.system.controller;

import in.guvi.event.management.system.config.CustomUserDetailsService;
import in.guvi.event.management.system.config.SecurityConfig;
import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.service.AttendanceService;
import in.guvi.event.management.system.service.EventService;
import in.guvi.event.management.system.service.RegistrationService;
import in.guvi.event.management.system.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class})
@DisplayName("EventController Tests")
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean EventService eventService;
    @MockitoBean RegistrationService registrationService;
    @MockitoBean UserService userService;
    @MockitoBean AttendanceService attendanceService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /events - should return 200 with events list")
    void getEvents_ShouldReturn200() throws Exception {
        Event event = Event.builder()
            .id(1L).title("Java Conf").category(EventCategory.CONFERENCE)
            .eventDate(LocalDateTime.now().plusDays(5))
            .endDate(LocalDateTime.now().plusDays(5).plusHours(3))
            .venue("Hall A").location("Chennai").capacity(100).registrationCount(0).build();

        Page<Event> page = new PageImpl<>(List.of(event));
        given(eventService.searchEvents(any(EventSearchDto.class), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(view().name("events/list"))
            .andExpect(model().attributeExists("events", "searchDto", "categories"));
    }

    @Test
    @DisplayName("GET /events/{id} - should return 200 with event detail")
    void getEventDetail_ShouldReturn200() throws Exception {
        Event event = Event.builder()
            .id(1L).title("Java Conf").category(EventCategory.CONFERENCE)
            .eventDate(LocalDateTime.now().plusDays(5))
            .endDate(LocalDateTime.now().plusDays(5).plusHours(3))
            .venue("Hall A").location("Chennai").capacity(100).registrationCount(0).build();

        given(eventService.findById(1L)).willReturn(event);

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("events/detail"))
            .andExpect(model().attributeExists("event"));
    }
}
