package in.guvi.event.management.system.service;

import in.guvi.event.management.system.dto.UserRegistrationDto;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    private UserRegistrationDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserRegistrationDto();
        validDto.setName("John Doe");
        validDto.setEmail("john@example.com");
        validDto.setPassword("Password@123");
        validDto.setConfirmPassword("Password@123");
    }

    @Test
    @DisplayName("registerUser - should save and return user when email is unique")
    void registerUser_ShouldSucceed_WhenEmailIsNew() {
        given(userRepository.existsByEmail("john@example.com")).willReturn(false);
        given(passwordEncoder.encode("Password@123")).willReturn("encoded-pass");
        User savedUser = User.builder()
            .id(1L).name("John Doe").email("john@example.com")
            .password("encoded-pass").role(Role.ROLE_USER).enabled(true).build();
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        User result = userService.registerUser(validDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - should throw exception when email already exists")
    void registerUser_ShouldThrow_WhenEmailExists() {
        given(userRepository.existsByEmail("john@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.registerUser(validDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("registerUser - should throw exception when passwords don't match")
    void registerUser_ShouldThrow_WhenPasswordsMismatch() {
        validDto.setConfirmPassword("DifferentPass@123");
        given(userRepository.existsByEmail(anyString())).willReturn(false);

        assertThatThrownBy(() -> userService.registerUser(validDto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("findByEmail - should return user when found")
    void findByEmail_ShouldReturnUser_WhenExists() {
        User user = User.builder().id(1L).email("john@example.com").build();
        given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("emailExists - should return true when email exists in DB")
    void emailExists_ShouldReturnTrue_WhenPresent() {
        given(userRepository.existsByEmail("john@example.com")).willReturn(true);
        assertThat(userService.emailExists("john@example.com")).isTrue();
    }
}
