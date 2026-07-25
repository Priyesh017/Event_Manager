package in.guvi.event.management.system.service;

import in.guvi.event.management.system.entity.Event;
import in.guvi.event.management.system.entity.Registration;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.EventCategory;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.exception.DuplicateRegistrationException;
import in.guvi.event.management.system.exception.EventFullException;
import in.guvi.event.management.system.repository.EventRepository;
import in.guvi.event.management.system.repository.RegistrationRepository;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService Unit Tests")
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @InjectMocks private RegistrationServiceImpl registrationService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L).name("John").email("john@test.com")
            .role(Role.ROLE_USER).enabled(true).build();

        testEvent = Event.builder()
            .id(1L).title("Java Conference").category(EventCategory.CONFERENCE)
            .eventDate(LocalDateTime.now().plusDays(7))
            .endDate(LocalDateTime.now().plusDays(7).plusHours(3))
            .venue("Tech Hall").location("Chennai")
            .capacity(100).registrationCount(0).build();
    }

    @Test
    @DisplayName("register - should succeed when user is not registered and event has capacity")
    void register_ShouldSucceed_WhenAvailable() {
        given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(false);
        given(eventRepository.findByIdWithLock(1L)).willReturn(Optional.of(testEvent));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        Registration saved = Registration.builder().id(1L).user(testUser).event(testEvent).build();
        given(registrationRepository.save(any())).willReturn(saved);
        given(eventRepository.atomicIncrementCount(1L)).willReturn(1);
        willDoNothing().given(emailService).sendRegistrationConfirmation(any(), any());

        Registration result = registrationService.register(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("register - should throw DuplicateRegistrationException when already registered")
    void register_ShouldThrow_WhenAlreadyRegistered() {
        given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> registrationService.register(1L, 1L))
            .isInstanceOf(DuplicateRegistrationException.class);
    }

    @Test
    @DisplayName("register - should throw EventFullException when event is at capacity")
    void register_ShouldThrow_WhenEventFull() {
        testEvent.setCapacity(10);
        testEvent.setRegistrationCount(10);
        given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(false);
        given(eventRepository.findByIdWithLock(1L)).willReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> registrationService.register(1L, 1L))
            .isInstanceOf(EventFullException.class);
    }

    @Test
    @DisplayName("register - should throw IllegalStateException when event is in the past")
    void register_ShouldThrow_WhenEventInPast() {
        testEvent.setEventDate(LocalDateTime.now().minusDays(1));
        given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(false);
        given(eventRepository.findByIdWithLock(1L)).willReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> registrationService.register(1L, 1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot register for an event that has already passed");
    }

    @Test
    @DisplayName("isRegistered - should return true when registration exists")
    void isRegistered_ShouldReturnTrue_WhenExists() {
        given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(true);
        assertThat(registrationService.isRegistered(1L, 1L)).isTrue();
    }
}
