package org.vrex.recognito.utility;


import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vrex.recognito.entity.Role;
import org.vrex.recognito.entity.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RoleUtil {

    private RoleUtil() {
        //enforces singleton pattern
    }

    private static final String SYSTEM_ROLE_IDENTIFIER = "SYS_";
    private static final String CLIENT_ROLE_IDENTIFIER = "APP_";
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
     * Extracts all valid roles from a comma separated string of roles
     * Returns response as an array
     *
     * @param roles
     * @return
     */
    public static String[] extractValidRoles(String roles) {
        String[] roleArray = StringUtils.isEmpty(roles) ? new String[0] : roles.split(",");
        List<String> validRoles = new LinkedList<>();
        String role;
        for (int i = 0; i < roleArray.length; i++) {
            role = roleArray[i] != null ? roleArray[i].trim().toUpperCase() : "";
            if (isValidRole(role)) validRoles.add(role);
        }
        return validRoles.toArray(new String[validRoles.size()]);
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
     * Checks whether a role is a system role
     *
     * @param role
     * @return
     */
    public static boolean isClientRole(String role) {
        return StringUtils.isEmpty(role) ? false : role.startsWith(CLIENT_ROLE_IDENTIFIER);
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

    /**
     * Wraps any number of role enums in a string array
     *
     * @param roles
     * @return
     */
    public static String[] wrapRoles(Role... roles) {
        int i, len = roles.length;
        String[] output = new String[len];
        for (i = 0; i < len; i++)
            output[i] = roles[i].name();

        return output;
    }

    /**
     * Wraps and returns only SYSTEM roles as Authorities
     *
     * @return
     */
    public static String[] getSystemRoles() {
        return wrapRoles(Role.SYS_ADMIN, Role.SYS_DEVELOPER, Role.SYS_USER);
    }

    /**
     * Wraps and returns only APP USER roles as Authorities
     *
     * @return
     */
    public static String[] getUserRoles() {
        return wrapRoles(Role.APP_ADMIN, Role.APP_USER, Role.APP_DEVELOPER);
    }

    /**
     * Checks whether a logged in user is a system admin or not
     *
     * @return
     */
    public static boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !ObjectUtils.isEmpty(auth) && auth.getAuthorities().stream().anyMatch(user -> user.getAuthority().equals(Role.SYS_ADMIN.name()));
    }
}
