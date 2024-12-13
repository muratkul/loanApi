package com.loanapi.services;

import com.loanapi.model.LoanApiException;
import com.loanapi.model.User;
import org.springframework.security.core.Authentication;

public interface UserService {

    User getUser(Authentication authentication) throws LoanApiException;

    boolean isAdmin(User user);

    User getUser(String username) throws LoanApiException;
}
