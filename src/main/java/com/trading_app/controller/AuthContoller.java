package com.trading_app.controller;

import com.trading_app.Service.CustomUserDetailsService;
import com.trading_app.config.JwtProvider;
import com.trading_app.model.User;
import com.trading_app.repository.UserRepository;
import com.trading_app.response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthContoller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        // Check if user exists
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if (isEmailExist != null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, "Email is already used with another account"));
        }

        // Save user
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFullName(user.getFullName());
        newUser.setPassword(user.getPassword()); // NOTE: Password should be hashed
        userRepository.save(newUser);

        // Authenticate and generate token
        Authentication auth = authenticate(user.getEmail(), user.getPassword());
        String token = JwtProvider.generateToken(auth.getName());

        return ResponseEntity.ok(new AuthResponse(token, "User registered successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) {
        try {
            Authentication auth = authenticate(user.getEmail(), user.getPassword());
            String token = JwtProvider.generateToken(auth.getName());
            return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid credentials"));
        }
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }

        if (!password.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

}
