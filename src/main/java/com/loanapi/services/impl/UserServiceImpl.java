package com.loanapi.services.impl;

import com.loanapi.model.ERole;
import com.loanapi.model.LoanApiErrorCode;
import com.loanapi.model.LoanApiException;
import com.loanapi.model.User;
import com.loanapi.repository.UserRepository;
import com.loanapi.security.services.UserDetailsImpl;
import com.loanapi.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUser(Authentication authentication) throws LoanApiException {
        return userRepository.findByUsername(((UserDetailsImpl)authentication.getPrincipal()).getUsername())
                .orElseThrow(() -> new LoanApiException(LoanApiErrorCode.USER_NOT_FOUND));
    }

    public boolean isAdmin(User user) {
        return user.getRoles().stream().filter(r->r.getName().equals(ERole.ROLE_ADMIN)).findAny().isPresent();
    }

    public User getUser(String username) throws LoanApiException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new LoanApiException(LoanApiErrorCode.USER_NOT_FOUND));
    }
}
