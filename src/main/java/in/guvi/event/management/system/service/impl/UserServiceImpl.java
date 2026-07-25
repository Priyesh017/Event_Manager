package in.guvi.event.management.system.service.impl;

import in.guvi.event.management.system.dto.UserRegistrationDto;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.exception.ResourceNotFoundException;
import in.guvi.event.management.system.repository.UserRepository;
import in.guvi.event.management.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                "An account with email '" + dto.getEmail() + "' already exists.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = User.builder()
            .name(dto.getName())
            .email(dto.getEmail().toLowerCase())
            .password(passwordEncoder.encode(dto.getPassword()))
            .role(Role.ROLE_USER)
            .enabled(true)
            .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("User id={} role updated to {}", userId, newRole);
        return saved;
    }
}
