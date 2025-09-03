package com.wipro.dto;

public class AuthResponse {
    private String jwt;
    private String role;
    private String name;

    public AuthResponse(String jwt, String role, String name) {
        this.jwt = jwt;
        this.role = role;
        this.name = name;
    }

    // Getters and Setters
    public String getJwt() { return jwt; }
    public void setJwt(String jwt) { this.jwt = jwt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
