package com.vncdigital.vpulse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.vncdigital.vpulse.security.CustomUserDetailsService;
import com.vncdigital.vpulse.security.JwtAuthenticationEntryPoint;
import com.vncdigital.vpulse.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                    .and()
                .csrf()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                        .permitAll()
                    .antMatchers("/v1/user/signin")
                        .permitAll()
                     .antMatchers("/v1/user/getPassword/*")
                        .permitAll()
                    .antMatchers("/v1/user/create")
                    	.permitAll()
                    .antMatchers("/v1/pdfreport")
                    	.permitAll()
                    .antMatchers("/v1/payment/**")
                        .permitAll()
                    .antMatchers("/v1/sales/viewFile/**")
                        .permitAll()   
                    .antMatchers("/v1/doctor/viewFile/**")
                    	.permitAll() 
                    .antMatchers("/v1/nurse/viewFile/**")
                    	.permitAll()
                    .antMatchers("/v1/voucher/viewFile/**")
                    	.permitAll()
                    .antMatchers("/v1/lab/servicePdf/viewFile/**")
                    	.permitAll()
                     .antMatchers("/v1/patient/blank")	
                        	.permitAll()
                     .antMatchers("/v1/doctor/create/**")
                         	.permitAll()
                     .antMatchers("/v1/doctor/viewFile/**")
                           	.permitAll()
                      .antMatchers("/v1/**/prescription/**")
                         	.permitAll()
                    .anyRequest()
                        .authenticated()
                     ;
        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}