package study.secondhand.global.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import study.secondhand.global.oauth2.CustomLogoutHandler;
import study.secondhand.global.oauth2.CustomOAuth2FailureHandler;
import study.secondhand.global.oauth2.CustomOAuth2SuccessHandler;
import study.secondhand.global.oauth2.CustomOAuth2UserService;
import study.secondhand.global.jwt.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    @Profile("dev")
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http, CustomOAuth2FailureHandler customOAuth2FailureHandler, CustomOAuth2SuccessHandler customOAuth2SuccessHandler, RefererSaveFilter refererSaveFilter, CustomLogoutHandler customLogoutHandler) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**", // REST API에는 CSRF 비활성화
                                "/webhook/**", // 외부에서 POST 요청 받는 엔드포인트 등
                                "/logout"
                        )
                )
                .cors(cors -> cors
                        .configurationSource(request -> {
                            var config = new CorsConfiguration();
                            config.setAllowedOrigins(List.of("http://localhost:8080", "null"));
                            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X_CSRF_TOKEN", "X-XSRF-TOKEN"));
                            config.setAllowCredentials(true);
                            return config;
                        })
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "font-src 'self' https://cdn.jsdelivr.net; " +
                                        "style-src 'self' https://cdn.jsdelivr.net; " +
                                        "script-src 'self' https://cdn.jsdelivr.net https://cdn.portone.io https://t1.daumcdn.net http://t1.daumcdn.net https://dapi.kakao.com; " +
                                        "img-src 'self' data: https://*.daumcdn.net http://*.daumcdn.net https://*.kakaocdn.net; " +
                                        "connect-src 'self' https://checkout-service.prod.iamport.co http://dapi.kakao.com; " +
                                        "object-src 'none'; " +
                                        "frame-src 'self' https://cdn.portone.io https://checkout-service.prod.iamport.co https://payment-gateway-sandbox.tosspayments.com https://postcode.map.daum.net http://postcode.map.daum.net; " +
                                        "child-src 'self' https://cdn.portone.io https://checkout-service.prod.iamport.co https://payment-gateway-sandbox.tosspayments.com https://postcode.map.daum.net http://postcode.map.daum.net; " +
                                        "frame-ancestors 'none';"
                                )
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/logout",
                                "/oauth2/**",
                                "/products/**",
                                "/shop/**",
                                "/search",
                                "/chat",
                                "/chat/user/**",
                                "/api/token/refresh",
                                "/api/auth/status",
                                "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .failureHandler(customOAuth2FailureHandler)
                        .successHandler(customOAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID", "ACCESS_TOKEN", "REFRESH_TOKEN") // 세션 쿠키 삭제
                        .invalidateHttpSession(true) // 세션 무효화
                        .clearAuthentication(true) // 인증 정보 제거

                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // CSRF 토큰 관리용
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                String errorMessage = (authException != null) ? authException.getMessage() : "Authentication required";
                                response.getWriter().write(errorMessage);
                            } else {
                                System.out.println("[인증 실패] " + authException.getMessage());
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                            // 현재 사용자가 익명 사용자인지 확인
                            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                                // 익명 사용자라면 authenticationEntryPoint와 동일하게 처리
                                if (request.getRequestURI().startsWith("/api/")) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write("Authentication required for this resource");
                                } else {
                                    response.sendRedirect("/login");
                                }
                            } else {
                                System.out.println("[접근 거부] " + accessDeniedException.getMessage());
                                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            }
                        })
                );
        http.addFilterBefore(refererSaveFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http, CustomOAuth2FailureHandler customOAuth2FailureHandler, CustomOAuth2SuccessHandler customOAuth2SuccessHandler, RefererSaveFilter refererSaveFilter, CustomLogoutHandler customLogoutHandler) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**", // REST API에는 CSRF 비활성화
                                "/webhook/**", // 외부에서 POST 요청 받는 엔드포인트 등
                                "/logout"
                        )
                )
                .cors(cors -> cors
                        .configurationSource(request -> {
                            var config = new CorsConfiguration();
                            config.setAllowedOrigins(List.of("http://15.164.226.128"));
                            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X_CSRF_TOKEN", "X-XSRF-TOKEN"));
                            config.setAllowCredentials(true);
                            return config;
                        })
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "font-src 'self' https://cdn.jsdelivr.net; " +
                                        "style-src 'self' https://cdn.jsdelivr.net; " +
                                        "script-src 'self' https://cdn.jsdelivr.net https://cdn.portone.io https://t1.daumcdn.net http://t1.daumcdn.net https://dapi.kakao.com; " +
                                        "img-src 'self' data: https://*.daumcdn.net http://*.daumcdn.net https://*.kakaocdn.net; " +
                                        "connect-src 'self' https://checkout-service.prod.iamport.co http://dapi.kakao.com; " +
                                        "object-src 'none'; " +
                                        "frame-src 'self' https://cdn.portone.io https://checkout-service.prod.iamport.co https://payment-gateway-sandbox.tosspayments.com https://postcode.map.daum.net http://postcode.map.daum.net; " +
                                        "child-src 'self' https://cdn.portone.io https://checkout-service.prod.iamport.co https://payment-gateway-sandbox.tosspayments.com https://postcode.map.daum.net http://postcode.map.daum.net; " +
                                        "frame-ancestors 'none';"
                                )
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/logout",
                                "/oauth2/**",
                                "/products/**",
                                "/shop/**",
                                "/search",
                                "/chat",
                                "/chat/user/**",
                                "/api/token/refresh",
                                "/api/auth/status",
                                "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .failureHandler(customOAuth2FailureHandler)
                        .successHandler(customOAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID", "ACCESS_TOKEN", "REFRESH_TOKEN") // 세션 쿠키 삭제
                        .invalidateHttpSession(true) // 세션 무효화
                        .clearAuthentication(true) // 인증 정보 제거
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // CSRF 토큰 관리용
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                String errorMessage = (authException != null) ? authException.getMessage() : "Authentication required";
                                response.getWriter().write(errorMessage);
                            } else {
                                System.out.println("[인증 실패] " + authException.getMessage());
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                                if (request.getRequestURI().startsWith("/api/")) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write("Authentication required for this resource");
                                } else {
                                    response.sendRedirect("/login");
                                }
                            } else {
                                System.out.println("[접근 거부] " + accessDeniedException.getMessage());
                                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            }
                        })
                );
        http.addFilterBefore(refererSaveFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
