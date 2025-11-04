package com.bitsealer.dto;

public record JwtResponse(String accessToken, String refreshToken, UserDto user) {}
