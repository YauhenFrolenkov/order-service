package com.innowise.order.dto.response;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
}
