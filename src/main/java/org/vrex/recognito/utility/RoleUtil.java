package org.vrex.recognito.utility;


import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.vrex.recognito.entity.Role;

@Component
public class RoleUtil {

    /**
     * Checks whether a role is an accepted role or not
     *
     * @param role
     * @return
     */
    public boolean isValidRole(String role) {
        return StringUtils.isEmpty(role) ? false : EnumUtils.isValidEnum(Role.class, role);
    }
}
