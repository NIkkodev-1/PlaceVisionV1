package com.nikko.backend.service;

import com.nikko.backend.dto.auth.AuthResponseDto;
import com.nikko.backend.dto.auth.LoginRequestDto;
import com.nikko.backend.dto.auth.RegisterRequestDto;
import com.nikko.backend.entities.User;
import com.nikko.backend.enums.Role;
import com.nikko.backend.exception.InvalidRequestException;
import com.nikko.backend.repositories.UserRepository;
import com.nikko.backend.security.CustomUserDetails;
import com.nikko.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidRequestException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // hardcoded — never trust a client-supplied role at registration

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(new CustomUserDetails(savedUser));

        return new AuthResponseDto(
                token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

        String token = jwtService.generateToken(new CustomUserDetails(user));

        return new AuthResponseDto(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}