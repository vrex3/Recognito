package org.vrex.recognito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.vrex.recognito.entity.Role;
import org.vrex.recognito.security.UserAuthenticationProvider;
import org.vrex.recognito.utility.RoleUtil;

@Slf4j
@EnableWebSecurity
@Configuration
@SuppressWarnings("unused")
public class SecurityConfig {

    private static final String LOG_TEXT = "Security-Setup : ";
    private static final String LOG_TEXT_ERROR = "Security-Setup - Encountered Exception - ";

    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterChainStateful(HttpSecurity http) throws Exception {

        try {
            log.info("{} Setting up [STATEFUL] Authentication Manager", LOG_TEXT);

            AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
            authenticationManagerBuilder.authenticationProvider(userAuthenticationProvider);
            authenticationManager = authenticationManagerBuilder.build();

            log.info("{} Set up [STATEFUL] Authentication Manager with custom authentication provider", LOG_TEXT);

            log.info("{} Setting up [STATEFUL] http request security parsing", LOG_TEXT);
            http.antMatcher("/client/user/**")
                    .cors()
                    .and()
                    .csrf().disable()
                    .authenticationManager(authenticationManager)
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .and()

                    .authorizeRequests()

                    .antMatchers(HttpMethod.POST, "/client/user/register").permitAll()
                    .antMatchers(HttpMethod.GET, "/client/user/login").hasAnyAuthority(RoleUtil.getUserRoles())
                    .antMatchers(HttpMethod.GET, "/client/user/token/generate").hasAnyAuthority(RoleUtil.ALL_AUTHORITIES)
                    .antMatchers(HttpMethod.GET, "/client/user/token/authorize").hasAnyAuthority(RoleUtil.ALL_AUTHORITIES)

                    .anyRequest().authenticated()

                    .and()
                    .formLogin()
                    .and()
                    .httpBasic();

            log.info("{} Set up [STATEFUL] http request security parsing", LOG_TEXT);

        } catch (Exception exception) {
            log.error("{} [STATEFUL] {}", LOG_TEXT_ERROR, exception.getMessage(), exception);
        }

        log.info("{} [STATEFUL] HTTP REQUEST READY TO BUILD.", LOG_TEXT);

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChainStateless(HttpSecurity http) throws Exception {

        try {
            log.info("{} Setting up [STATELESS] Authentication Manager", LOG_TEXT);

            AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
            authenticationManagerBuilder.authenticationProvider(userAuthenticationProvider);
            authenticationManager = authenticationManagerBuilder.build();

            log.info("{} Set up [STATELESS] Authentication Manager with custom authentication provider", LOG_TEXT);

            log.info("{} Setting up [STATELESS] http request security parsing", LOG_TEXT);
            http.cors()
                    .and()
                    .csrf().disable()
                    .authenticationManager(authenticationManager)
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()

                    .antMatchers(HttpMethod.POST, "/application").permitAll()
                    .antMatchers(HttpMethod.GET, "/application/invite").hasAnyAuthority(RoleUtil.wrapRoles(Role.SYS_ADMIN, Role.SYS_DEVELOPER))
                    .antMatchers(HttpMethod.GET, "/application").hasAnyAuthority(RoleUtil.getSystemRoles())

                    .antMatchers(HttpMethod.POST, "/app/role/mapping").hasAnyAuthority(RoleUtil.wrapRoles(Role.SYS_ADMIN, Role.SYS_DEVELOPER))
                    .antMatchers(HttpMethod.GET, "/app/role/mapping").hasAnyAuthority(RoleUtil.getSystemRoles())

                    .antMatchers(HttpMethod.POST, "/system/user").permitAll()
                    .antMatchers(HttpMethod.GET, "/system/user").hasAnyAuthority(RoleUtil.getSystemRoles())
                    .antMatchers(HttpMethod.GET, "/system/user/application").hasAnyAuthority(RoleUtil.getSystemRoles())

                    .anyRequest().authenticated()

                    .and()
                    .formLogin()
                    .and()
                    .httpBasic();

            log.info("{} Set up [STATELESS] http request security parsing", LOG_TEXT);

        } catch (Exception exception) {
            log.error("{} [STATELESS] {}", LOG_TEXT_ERROR, exception.getMessage(), exception);
        }

        log.info("{} [STATELESS] HTTP REQUEST READY TO BUILD.", LOG_TEXT);

        return http.build();
    }
}
