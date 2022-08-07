package org.vrex.recognito.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.repository.UserRepository;
import org.vrex.recognito.utility.AuthUtil;
import org.vrex.recognito.utility.RoleUtil;

@Slf4j
@Component
@SuppressWarnings("unused")
public class UserAuthenticationProvider implements AuthenticationProvider {

    private static final String LOG_TEXT = "Auth-Service : ";
    private static final String LOG_TEXT_ERROR = "Auth-Service - Encountered Exception - ";

    @Autowired
    private AuthUtil authUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String secret = authentication.getCredentials().toString();

        log.info("{} Authenticating user - {}", LOG_TEXT, username);

        User user = authUtil.authenticateUserCredentials(username, secret);
        if (user != null) {
            log.info("{} User Authenticated - {}", LOG_TEXT, username);
            return new UsernamePasswordAuthenticationToken(username, secret, RoleUtil.getAuthorities(user));
        } else {
            log.error("{} Invalid credentials for user - {}", LOG_TEXT, username);
            throw new BadCredentialsException(ApplicationConstants.INVALID_USER);
        }
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
    }
}
