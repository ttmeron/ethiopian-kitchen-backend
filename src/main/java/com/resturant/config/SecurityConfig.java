package com.resturant.config;


import com.resturant.security.JwtFilter;
import com.resturant.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                .cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/api/foods", "/foods").permitAll()
                .antMatchers("/api/ingredients", "/ingredients").permitAll()
                .antMatchers("/api/foods/{id}", "/foods/{id}").permitAll()
                .antMatchers("/api/user/*", "/user/guest").permitAll()
                .antMatchers("/api/images/**","/images/**").permitAll()
                .antMatchers("/api/payments/**","/payments/**").permitAll()
                .antMatchers("/api/user/register","/user/register").permitAll()
                .antMatchers("/api/user/check-email","/user/check-email").permitAll()
                .antMatchers("/api/orders/by-email", "/orders/by-email").permitAll()
                .antMatchers("/api/soft-drinks", "/soft-drinks").permitAll()




                .antMatchers("/api/orders/**","/orders/**").hasAnyRole("ADMIN","EMPLOYEE","USER","GUEST")



                .antMatchers("/api/foods/admin/**", "/foods/admin/**").hasRole("ADMIN")
                .antMatchers("/api/soft-drinks/admin/**", "/foods/soft-drinks/admin/**").hasRole("ADMIN")
                .antMatchers("/api/admin/employees/**", "/admin/employees/**").hasRole("ADMIN")
                .antMatchers("/api/ingredients/admin/**", "/ingredients/admin/**").hasRole("ADMIN")
                .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);



    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200","https://ethiopian-kitchen-frontend.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
