package org.vrex.recognito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.vrex.recognito.security.UserAuthenticationProvider;
import org.vrex.recognito.utility.RoleUtil;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(userAuthenticationProvider);
        authenticationManager = authenticationManagerBuilder.build();

        http.cors().and().csrf().disable()
                .authenticationManager(authenticationManager)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()

                .antMatchers(HttpMethod.POST, "/application").permitAll()
                .antMatchers(HttpMethod.POST, "/user").permitAll()
                .antMatchers("/user/token/generate").hasAnyAuthority(RoleUtil.ALL_AUTHORITIES)
                .antMatchers("/user/token/authorize").hasAnyAuthority(RoleUtil.ALL_AUTHORITIES)
                .anyRequest().authenticated()

                .and().formLogin()
                .and().httpBasic();

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SCryptPasswordEncoder();
    }

}
