package com.loanapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.loanapi")  // Scan the base package and subpackages

public class LoanApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApiApplication.class, args);
    }

}
