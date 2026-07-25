package in.guvi.event.management.system.service;

import in.guvi.event.management.system.dto.UserRegistrationDto;
import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(UserRegistrationDto dto);

    Optional<User> findByEmail(String email);

    User findById(Long id);

    List<User> findAllUsers();

    boolean emailExists(String email);

    /** Update only the role of a user (admin operation) */
    User updateUserRole(Long userId, Role newRole);
}
