package org.vrex.recognito.utility;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.repository.UserRepository;

@Component
public class AuthUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticates a pair of username credentials
     * Returns user object if authentication is successful
     *
     * @param username
     * @param secret
     * @return
     */
    @Cacheable(cacheNames = ApplicationConstants.USER_CREDENTIALS_CACHE, unless = "#result!=null")
    public User authenticateUserCredentials(String username, String secret) {
        User user = userRepository.getUserByName(username);
        return !ObjectUtils.isEmpty(user) && passwordEncoder.matches(secret, user.getSecret()) ? user : null;
    }
}
