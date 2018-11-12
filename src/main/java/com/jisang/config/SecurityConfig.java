package com.jisang.config;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.jisang.security.authentication.JWTLoginAuthenticationFilter;
import com.jisang.security.authentication.JWTTokenAuthenticationFilter;
import com.jisang.security.authentication.TemporaryPasswordAuthenticationFilter;
import com.jisang.security.authentication.handler.RestAuthenticationFailureHandler;
import com.jisang.security.exception.handler.AccountStatusExceptionHandler;
import com.jisang.security.exception.handler.AuthenticationExceptionHandler;
import com.jisang.security.exception.handler.AuthenticationServiceExceptionHandler;
import com.jisang.security.exception.handler.BadRequestExceptionHandler;
import com.jisang.security.persistence.SecurityMybatisMapper;
import com.jisang.security.service.AnonymousUserAuthJWTService;
import com.jisang.security.service.AuthenticationNumberJWTService;
import com.jisang.security.service.JWTService;
import com.jisang.security.service.JWTServiceResolver;
import com.jisang.security.service.UserAuthJWTService;
import com.jisang.security.authentication.AuthenticationNumberAuthenticationFilter;
import com.jisang.security.authentication.AuthenticationNumberEntryPointFilter;

@Configuration
@EnableWebSecurity(debug = true)
@MapperScan(basePackages = { "com.jisang.security.persistence" }, markerInterface = SecurityMybatisMapper.class)
@ComponentScan(basePackages = { "com.jisang.security.validation", "com.jisang.security.aop",
        "com.jisang.security.authentication.provider", "com.jisang.security.authentication.handler",
        "com.jisang.security.core.userdetails", "com.jisang.security.support", "com.jisang.security.service",
        "com.jisang.security.persistence" })
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_END_POINT = "/login";
    private static final String LOGOUT_END_POINT = "/logout";
    private static final String AUTHENTICATION_NUMBER_END_POINT = "/authentication-number";
    private static final String TEMPORARY_PASSWORD_END_POINT = "/temporary-password";

    private static final String TOKEN_AUTH_ANT_PATTERN = "/auth/**";

    private static final String ACCESS_MANAGE_MARKET_ANT_PATTERN = "/auth/market/**";
    private static final String ACCESS_MANAGE_PRODUCT_ANT_PATTERN = "/auth/product/**";

    @Autowired
    @Qualifier("tokenProvider")
    private AuthenticationProvider tokenAuthProvider;

    @Autowired
    @Qualifier("phoneNumberProvider")
    private AuthenticationProvider phoneNumberAuthProvider;

    @Autowired
    @Qualifier("loginProvider")
    private AuthenticationProvider loginAuthProvider;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;
    @Autowired
    private AuthenticationEntryPoint authEntryPoint;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {

        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(loginAuthProvider);
        auth.authenticationProvider(phoneNumberAuthProvider);
        auth.authenticationProvider(tokenAuthProvider);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable().exceptionHandling().authenticationEntryPoint(authEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(authenticationEntryPointFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authenticationNumberAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(temporaryPasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class).logout()
                .logoutUrl(LOGOUT_END_POINT).logoutSuccessHandler(logoutSuccessHandler)
                // .permitAll()
                .and().authorizeRequests().antMatchers(ACCESS_MANAGE_MARKET_ANT_PATTERN)
                .access("hasRole('ROLE_MANAGER')").antMatchers(ACCESS_MANAGE_PRODUCT_ANT_PATTERN)
                .access("hasRole('ROLE_MANAGER')");
    }

    @Bean
    public JWTLoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        JWTLoginAuthenticationFilter filter = new JWTLoginAuthenticationFilter(authLoginRequestMatcher());

        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(restAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public JWTTokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
        JWTTokenAuthenticationFilter filter = new JWTTokenAuthenticationFilter(authJwtRequestMatcher());

        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(restAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public AuthenticationNumberEntryPointFilter authenticationEntryPointFilter() throws Exception {
        AuthenticationNumberEntryPointFilter filter = new AuthenticationNumberEntryPointFilter(
                authNumberEntryPointRequestMatcher());

        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(restAuthenticationFailureHandler());

        return filter;
    }

    @Bean
    public AuthenticationNumberAuthenticationFilter authenticationNumberAuthenticationFilter() throws Exception {
        AuthenticationNumberAuthenticationFilter filter = new AuthenticationNumberAuthenticationFilter(
                authNumberAuthenticationRequestMatcher());

        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(restAuthenticationFailureHandler());

        return filter;
    }

    @Bean
    public TemporaryPasswordAuthenticationFilter temporaryPasswordAuthenticationFilter() throws Exception {
        TemporaryPasswordAuthenticationFilter filter = new TemporaryPasswordAuthenticationFilter(
                temporaryPasswordAuthenticationRequestMatcher());

        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(restAuthenticationFailureHandler());

        return filter;
    }

    private RequestMatcher authLoginRequestMatcher() {
        RequestMatcher reqMatcher = new AntPathRequestMatcher(LOGIN_END_POINT, HttpMethod.POST.toString());
        return reqMatcher;
    }

    private RequestMatcher authJwtRequestMatcher() {
        RequestMatcher reqMatcher = new AntPathRequestMatcher(TOKEN_AUTH_ANT_PATTERN);
        return reqMatcher;
    }

    private RequestMatcher authNumberEntryPointRequestMatcher() {
        RequestMatcher reqMatcher = new AntPathRequestMatcher(AUTHENTICATION_NUMBER_END_POINT,
                HttpMethod.GET.toString());
        return reqMatcher;
    }

    private RequestMatcher authNumberAuthenticationRequestMatcher() {
        RequestMatcher reqMatcher = new AntPathRequestMatcher(AUTHENTICATION_NUMBER_END_POINT,
                HttpMethod.POST.toString());
        return reqMatcher;
    }

    private RequestMatcher temporaryPasswordAuthenticationRequestMatcher() {
        RequestMatcher reqMatcher = new AntPathRequestMatcher(TEMPORARY_PASSWORD_END_POINT,
                HttpMethod.PATCH.toString());
        return reqMatcher;
    }

    @Bean
    public AuthenticationFailureHandler restAuthenticationFailureHandler() {

        RestAuthenticationFailureHandler failureHandler = new RestAuthenticationFailureHandler();
        failureHandler.addExceptionHandler(new AuthenticationExceptionHandler(messageSource));
        failureHandler.addExceptionHandler(new AuthenticationServiceExceptionHandler(messageSource));
        failureHandler.addExceptionHandler(new AccountStatusExceptionHandler(messageSource));
        failureHandler.addExceptionHandler(new BadRequestExceptionHandler(messageSource));

        return failureHandler;
    }

    @Bean
    public JWTService userAuthJWTService() {
        return new UserAuthJWTService();
    }

    @Bean
    public JWTService authenticationNumberJWTService() {
        return new AuthenticationNumberJWTService();
    }

    @Bean
    public JWTService anonymousUserAuthJWTService() {
        return new AnonymousUserAuthJWTService();
    }

    @Bean
    public JWTServiceResolver jwtServiceResolver() {
        List<JWTService> jwtServices = new ArrayList<>();
        jwtServices.add(userAuthJWTService());
        jwtServices.add(authenticationNumberJWTService());
        jwtServices.add(anonymousUserAuthJWTService());

        return new JWTServiceResolver(jwtServices);
    }

}
