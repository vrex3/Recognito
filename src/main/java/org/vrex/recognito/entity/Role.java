package org.vrex.recognito.entity;

public enum Role {

    /**
     * SYS refers to this system i.e. RECOGNITO
     * NO RESOURCE SHOULD BE MAPPED TO SYSTEM ROLES
     */

    /**
     * SYSTEM ROLES
     */
    SYS_ADMIN,
    SYS_DEVELOPER,
    SYS_USER,

    /**
     * CLIENT/APP ROLES
     */
    APP_ADMIN,
    APP_DEVELOPER,
    APP_USER,
}
