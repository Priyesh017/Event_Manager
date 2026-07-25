package in.guvi.event.management.system.service;

import in.guvi.event.management.system.dto.EventDto;
import in.guvi.event.management.system.dto.EventSearchDto;
import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.repository.RegistrationRepository;
import in.guvi.event.management.system.repository.SpeakerRepository;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.EmailService;
import in.guvi.event.management.system.service.impl.EventServiceImpl;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock private EventRepository       eventRepository;
    @Mock private SpeakerRepository     speakerRepository;
    @Mock private UserRepository        userRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private EmailService          emailService;
    @InjectMocks private EventServiceImpl eventService;

    private User adminUser;
    private Event testEvent;
    private EventDto testDto;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).name("Admin").email("admin@test.com")
            .role(Role.ROLE_ADMIN).enabled(true).build();

        testEvent = Event.builder()
            .id(1L).title("Java Conference").description("Learn Java")
            .category(EventCategory.CONFERENCE)
            .eventDate(LocalDateTime.now().plusDays(10))
            .endDate(LocalDateTime.now().plusDays(10).plusHours(3))
            .venue("Tech Hall").location("Chennai")
            .capacity(100).registrationCount(0).build();

        testDto = new EventDto();
        testDto.setTitle("Java Conference");
        testDto.setDescription("Learn Java");
        testDto.setCategory(EventCategory.CONFERENCE);
        testDto.setEventDate(LocalDateTime.now().plusDays(10));
        testDto.setEndDate(LocalDateTime.now().plusDays(10).plusHours(3));
        testDto.setVenue("Tech Hall");
        testDto.setLocation("Chennai");
        testDto.setCapacity(100);
    }

    @Test
    @DisplayName("createEvent - should save and return event")
    void createEvent_ShouldReturnSavedEvent() {
        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        given(speakerRepository.findAllById(List.of())).willReturn(List.of());
        given(eventRepository.save(any(Event.class))).willReturn(testEvent);

        Event result = eventService.createEvent(testDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Conference");
        then(eventRepository).should().save(any(Event.class));
    }

    @Test
    @DisplayName("findById - should throw ResourceNotFoundException when event does not exist")
    void findById_ShouldThrow_WhenNotFound() {
        given(eventRepository.findDetailById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteEvent - should delete when event exists")
    void deleteEvent_ShouldDelete_WhenExists() {
        given(eventRepository.existsById(1L)).willReturn(true);
        willDoNothing().given(eventRepository).deleteById(1L);

        assertThatCode(() -> eventService.deleteEvent(1L)).doesNotThrowAnyException();
        then(eventRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("searchEvents - should return paginated results")
    void searchEvents_ShouldReturnPage() {
        Page<Event> page = new PageImpl<>(List.of(testEvent));
        given(eventRepository.searchEvents(any(), any(), any(), any(), any(), any())).willReturn(page);

        Page<Event> result = eventService.searchEvents(new EventSearchDto(), PageRequest.of(0, 9));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Conference");
    }
}
