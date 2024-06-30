package account.config;

import account.exceptions.CustomAccessDeniedException;
import account.services.SecurityEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";
    static final String ROLE_ACCOUNTANT = "ROLE_ACCOUNTANT";
    static final String ROLE_USER = "ROLE_USER";
    static final String ROLE_AUDITOR = "ROLE_AUDITOR";
    private final SecurityEventService securityEventService;
    private final ObjectMapper objectMapper;


    @Autowired
    public SecurityConfig(SecurityEventService securityEventService, ObjectMapper objectMapper) {
        this.securityEventService = securityEventService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler())
                ) // Handle auth errors
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .authorizeHttpRequests(auth -> auth  // manage access
                                .requestMatchers(HttpMethod.POST, "/api/auth/signup").anonymous()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers(
                                        "/actuator/shutdown",
                                        "/h2",
                                        "/h2/**",
                                        "/api/register"
                                ).permitAll()
                                .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasAnyAuthority(
                                        ROLE_ACCOUNTANT
                                )

                                .requestMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyAuthority(ROLE_USER, ROLE_ACCOUNTANT, ROLE_ADMINISTRATOR)
                                .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority(ROLE_ACCOUNTANT)
                                .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority(ROLE_ACCOUNTANT)
                                .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority(ROLE_USER, ROLE_ACCOUNTANT)
                                .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasAuthority(ROLE_ADMINISTRATOR)
                                .requestMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasAuthority(ROLE_ADMINISTRATOR)
                                .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority(ROLE_ADMINISTRATOR)
                                .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasAuthority(ROLE_ADMINISTRATOR)
                                .requestMatchers(HttpMethod.GET, "/api/security/events/").hasAuthority(ROLE_AUDITOR)
                                .anyRequest().authenticated()

                        // other matchers
                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedException(securityEventService);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint(objectMapper);
    }
}
