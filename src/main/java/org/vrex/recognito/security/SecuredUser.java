package org.vrex.recognito.security;

/**
 * DISABLED FOR CUSTOM AUTHENTICATION PROVIDER : org.vrex.recognito.security.UserAuthenticationProvider
 */
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.vrex.recognito.entity.User;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//@AllArgsConstructor
//@Getter
//public class SecuredUser implements UserDetails {
//
//    private static final long serialVersionUID = 2285497942317282278L;
//
//    /**
//     * WRAPPED MACHINE USER
//     */
//    private final User user;
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Arrays.asList(new SimpleGrantedAuthority(user.getRole()));
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getSecret();
//    }
//
//    @Override
//    public String getUsername() {
//        return user.getUsername();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return false;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return false;
//    }
//
////    /**
////     * Add credentials expiry logic later
////     *
////     * @return
////     */
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return false;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
