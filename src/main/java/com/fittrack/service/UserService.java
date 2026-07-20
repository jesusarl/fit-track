package com.fittrack.service;

import com.fittrack.dto.CreateUserRequest;
import com.fittrack.dto.UserResponse;
import com.fittrack.model.User;
import com.fittrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User user = new User(request.nombre(), request.email());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getNombre(), user.getEmail());
    }
}
