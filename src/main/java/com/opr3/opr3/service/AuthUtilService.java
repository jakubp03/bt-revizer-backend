package com.opr3.opr3.service;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.opr3.opr3.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUtilService {

    public User getAuthenticatedUser() throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new InsufficientAuthenticationException("No authentication found");
        }

        if (!(authentication.getPrincipal() instanceof User)) {
            throw new InsufficientAuthenticationException("Invalid authentication principal type");
        }

        return (User) authentication.getPrincipal();
    }
}
