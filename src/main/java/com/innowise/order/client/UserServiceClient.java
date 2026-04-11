package com.innowise.order.client;


import com.innowise.order.dto.response.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final UserClient userClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    public UserResponseDto getUserById(Long id) {
        return userClient.getUserById(id);
    }

    @SuppressWarnings("unused")
    public UserResponseDto fallbackGetUser(Long id, Throwable ex) {
        UserResponseDto user = new UserResponseDto();
        user.setId(id);
        user.setName("Unknown");
        user.setSurname("User");
        user.setEmail("unknown@example.com");
        return user;
    }
}
