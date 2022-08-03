package org.vrex.recognito.security;

/**
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.repository.UserRepository;


@Service
public class SecuredUserReader implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserByName(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException(ApplicationConstants.INVALID_USER);
        }
        return new SecuredUser(user);
    }
}
**/

/**
 * DISABLED FOR CUSTOM AUTHENTICATION PROVIDER : org.vrex.recognito.security.UserAuthenticationProvider
 */
