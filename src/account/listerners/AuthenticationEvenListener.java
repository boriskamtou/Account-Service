package account.listerners;


import account.adapters.UserAdapter;
import account.entities.Users;
import account.repositories.AuthRepository;
import account.services.AuthService;
import account.services.SecurityEventService;
import account.utils.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class AuthenticationEvenListener {

    public static int MAX_FAILED_ATTEMPTS = 4;

    private final AuthRepository authRepository;
    private final AuthService authService;
    private final SecurityEventService securityEventService;
    private final HttpServletRequest request;

    public AuthenticationEvenListener(
            AuthRepository authRepository,
            AuthService authService,
            SecurityEventService securityEventService,
            HttpServletRequest request
    ) {
        this.authRepository = authRepository;
        this.authService = authService;
        this.securityEventService = securityEventService;
        this.request = request;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent successEvent) {
        log(successEvent);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log(failureEvent);
    }

    private void log(AbstractAuthenticationEvent event) {


        if (event instanceof AuthenticationSuccessEvent) {
            String email = event.getAuthentication().getName();

            Users user = authRepository.findUsersByEmail(email.toLowerCase());

            if (user != null) {
                if (user.getFailedAttempt() > 0) {
                    authService.resetFailedAttempts(user.getEmail());
                }
            }
        }
        if (event instanceof AbstractAuthenticationFailureEvent) {
            String email = event.getAuthentication().getName();
            String endpoint = request.getRequestURI();

            Users user = authRepository.findUsersByEmail(email.toLowerCase());
            boolean isUserExist = authRepository.existsByEmail(email);

            if (user != null && isUserExist && !user.getRoles().contains("ROLE_ADMINISTRATOR")) {
                if (user.isActive()) {
                    if (user.getFailedAttempt() >= MAX_FAILED_ATTEMPTS) {
                        user.setActive(false);
                        authRepository.save(user);
                        securityEventService.saveEvent(Constants.EVENT_LOGIN_FAILED, email, endpoint, endpoint);
                        securityEventService.saveEvent(Constants.EVENT_BRUTE_FORCE, email, endpoint, endpoint);
                        securityEventService.saveEvent(Constants.EVENT_LOCK_USER, user.getEmail(), "Lock user " + user.getEmail().toLowerCase(), endpoint);
                    } else {
                        authService.increaseFailedAttempts(user);
                        securityEventService.saveEvent(Constants.EVENT_LOGIN_FAILED, email.toLowerCase(), request.getRequestURI(), endpoint);
                    }
                } else {
                    throw new LockedException("User account is locked");
                }
            } else {
                securityEventService.saveEvent(Constants.EVENT_LOGIN_FAILED, email.toLowerCase(), request.getRequestURI(), endpoint);
            }
        }
        System.out.println("User: " + event.getAuthentication().getName());
        System.out.println("Authorities: " + event.getAuthentication().getAuthorities());
        System.out.println("Details: " + event.getAuthentication().getDetails());
        System.out.println("Credentials: " + event.getAuthentication().getCredentials());
        System.out.println("Principal: " + event.getAuthentication().getPrincipal());
        System.out.println("Is authenticated: " + event.getAuthentication().isAuthenticated());
        System.out.println("Url: " + request.getRequestURI());
        if (event instanceof AbstractAuthenticationFailureEvent failureEvent) {
            System.out.println("Exception: " + failureEvent.getException());
        }
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp / 1000), TimeZone.getDefault().toZoneId()
        );
    }

}