package org.vrex.recognito.utility;


import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import org.vrex.recognito.entity.Role;
import org.vrex.recognito.entity.User;

import java.util.Arrays;
import java.util.Collection;

public class RoleUtil {

    private RoleUtil() {
        //enforces singleton pattern
    }

    private static final String SYSTEM_ROLE_IDENTIFIER = "SYS_";
    public static final String[] ALL_AUTHORITIES = Arrays.stream(Role.values()).map(Enum::name).toArray(String[]::new);

    /**
     * Checks whether a role is an accepted role or not
     *
     * @param role
     * @return
     */
    public static boolean isValidRole(String role) {
        return StringUtils.isEmpty(role) ? false : EnumUtils.isValidEnum(Role.class, role);
    }

    /**
     * Checks whether a role is a system role
     *
     * @param role
     * @return
     */
    public static boolean isSystemRole(String role) {
        return StringUtils.isEmpty(role) ? false : role.startsWith(SYSTEM_ROLE_IDENTIFIER);
    }

    /**
     * Returns granted authority of a user
     *
     * @param user
     * @return
     */
    public static Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Arrays.asList(new SimpleGrantedAuthority(user.getRole()));
    }
}
