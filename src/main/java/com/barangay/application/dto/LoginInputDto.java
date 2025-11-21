package com.barangay.application.dto;

/**
 * DTO for user login input
 */
public class LoginInputDto {
    private final String username;
    private final String password;

    public LoginInputDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
