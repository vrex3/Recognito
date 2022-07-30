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
import org.vrex.recognito.utility.RoleUtil;

@Slf4j
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String secret = authentication.getCredentials().toString();
        User user = userRepository.getUserByName(username);
        if (!ObjectUtils.isEmpty(user) && passwordEncoder.matches(secret, user.getSecret())) {
            return new UsernamePasswordAuthenticationToken(username, secret, RoleUtil.getAuthorities(user));
        } else
            throw new BadCredentialsException(ApplicationConstants.INVALID_USER);
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
    }
}
