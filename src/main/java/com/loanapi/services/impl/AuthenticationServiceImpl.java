package com.loanapi.services.impl;

import com.loanapi.model.*;
import com.loanapi.payload.JwtResponse;
import com.loanapi.payload.LoginRequest;
import com.loanapi.payload.SignupRequest;
import com.loanapi.repository.RoleRepository;
import com.loanapi.repository.UserRepository;
import com.loanapi.security.jwt.JwtUtils;
import com.loanapi.security.services.UserDetailsImpl;
import com.loanapi.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public JwtResponse signIn (LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        Long expiration = jwtUtils.getExpiration();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                expiration);
    }

    @Override
    public void signUp(SignupRequest signUpRequest) throws LoanApiException {
        if (signUpRequest.getEmail() == null && signUpRequest.getUsername() != null) {
            signUpRequest.setEmail(signUpRequest.getUsername());
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new LoanApiException(LoanApiErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new LoanApiException(LoanApiErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);

        user.setName(signUpRequest.getName());
        user.setSurname(signUpRequest.getSurname());
        user.setUsedCreditLimit(new BigDecimal(0));
        user.setCreditLimit(signUpRequest.getCreditLimit());
        userRepository.save(user);
    }


}
