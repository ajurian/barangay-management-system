package com.barangay.application.dto;

/**
 * DTO for first-run setup input (creating super admin)
 */
public class SetupInputDto {
    private final String username;
    private final String password;

    public SetupInputDto(String username, String password) {
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
