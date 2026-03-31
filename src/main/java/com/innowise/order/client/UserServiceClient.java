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
    public UserResponseDto getUserByEmail(String email) {
        return userClient.getUserByEmail(email);
    }

    public UserResponseDto fallbackGetUser(String email, Throwable ex) {
        UserResponseDto user = new UserResponseDto();
        user.setEmail(email);
        user.setName("Unknown");
        user.setSurname("User");
        return user;
    }
}
