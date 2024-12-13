package com.loanapi.services;


import com.loanapi.model.LoanApiException;
import com.loanapi.payload.JwtResponse;
import com.loanapi.payload.LoginRequest;
import com.loanapi.payload.SignupRequest;

public interface AuthenticationService {

    JwtResponse signIn (LoginRequest loginRequest);

    void signUp(SignupRequest signUpRequest) throws LoanApiException;
}
