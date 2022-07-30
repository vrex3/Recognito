package org.vrex.recognito.utility;


import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.vrex.recognito.entity.Role;

public class RoleUtil {

    private RoleUtil() {
        //enforces singleton pattern
    }

    private static final String SYSTEM_ROLE_IDENTIFIER = "SYS_";

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
}
