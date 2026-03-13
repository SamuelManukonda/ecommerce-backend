package com.hometask.inventory.service;

import com.hometask.inventory.dto.AuthRequest;
import com.hometask.inventory.dto.AuthResponse;
import com.hometask.inventory.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return new AuthResponse(jwtService.generateToken(userDetails));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password", ex);
        }
    }
}

