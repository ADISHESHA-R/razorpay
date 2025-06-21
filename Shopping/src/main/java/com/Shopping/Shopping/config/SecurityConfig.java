package com.Shopping.Shopping.config;

import com.Shopping.Shopping.service.UserDetailsServiceImpl;
import com.Shopping.Shopping.service.SellerDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomLoginSuccessHandler successHandler;
    private final UserDetailsServiceImpl userDetailsService;
    private final SellerDetailsService sellerDetailsService;

    public SecurityConfig(CustomLoginSuccessHandler successHandler,
                          UserDetailsServiceImpl userDetailsService,
                          SellerDetailsService sellerDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
        this.sellerDetailsService = sellerDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider userAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider sellerAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(sellerDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/signup", "/home", "/profile", "/logout","/product-detail")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signup", "/login", "/").permitAll()
                        .requestMatchers("/profile", "/home").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        http.authenticationProvider(userAuthProvider());

        return http.build();
    }

    @Bean
    public SecurityFilterChain sellerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/seller-login", "/seller-signup", "/seller-dashboard", "/upload-product", "/seller-home", "/seller-profile","/product-detail")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/seller-login", "/seller-signup").permitAll()
                        .requestMatchers("/seller-dashboard", "/upload-product", "/seller-home", "/seller-profile").hasRole("SELLER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/seller-login")
                        .loginProcessingUrl("/seller-login")
                        .successHandler(successHandler)
                        .failureUrl("/seller-login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        http.authenticationProvider(sellerAuthProvider());

        return http.build();
    }
}
