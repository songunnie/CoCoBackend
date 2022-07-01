package com.igocst.coco.security;

import com.igocst.coco.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();

        // JWT 사용 위해 세션 안씀
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/user").permitAll()
                .antMatchers("/login").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin().disable();

        return http.build();
    }
}
