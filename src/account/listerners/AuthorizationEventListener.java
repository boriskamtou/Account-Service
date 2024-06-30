package account.listerners;

import account.exceptions.ForbiddenException;
import account.repositories.AuthRepository;
import account.services.AuthService;
import account.services.SecurityEventService;
import account.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.event.AuthorizationEvent;
import org.springframework.security.authorization.event.AuthorizationGrantedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationEventListener {

    private final AuthRepository authRepository;
    private final AuthService authService;
    private final SecurityEventService securityEventService;
    private HttpServletRequest request;

    @Autowired
    public AuthorizationEventListener(
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
    public void onAuthorizationGranted(AuthorizationGrantedEvent event) {
        log(event);
    }

    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        log(event);
    }


    @EventListener
    public void handleAuthorizationFailureEvent(AuthorizationFailureEvent event) {

        System.out.println("AUTHORIZATION FAILED!!!");

        Authentication auth = event.getAuthentication();
        String username = auth != null ? auth.getName() : "Anonymous";
        String endpoint = request.getRequestURI();

        securityEventService.saveEvent(Constants.EVENT_ACCESS_DENIED, username, endpoint, endpoint);
    }

    private void log(AuthorizationEvent event) {
        String endpoint = request.getRequestURI();

        if (event instanceof AuthorizationGrantedEvent) {
            System.out.println("ACCESS GRANTED FROM EVENT");
//            securityEventService.saveEvent(Constants.EVENT_GRANT_ROLE, event.getAuthentication().get().getName(), request.getRequestURI(), request.getRequestURI());
        }

        if (event instanceof AuthorizationDeniedEvent) {
            System.out.println("ACCESS DENIED FROM EVENT");
//            if (event.getAuthentication().get().isAuthenticated()) {
//                securityEventService.saveEvent(Constants.EVENT_ACCESS_DENIED, event.getAuthentication().get().getName(), request.getRequestURI(), request.getRequestURI());
//            }
//            throw new ForbiddenException();
        }

        System.out.println("User: " + event.getAuthentication().get().getName());
        System.out.println("Authorities: " + event.getAuthentication().get().getAuthorities());
        System.out.println("Details: " + event.getAuthentication().get().getDetails());
        System.out.println("Credentials: " + event.getAuthentication().get().getCredentials());
        System.out.println("Principal: " + event.getAuthentication().get().getPrincipal());
        System.out.println("Is authenticated: " + event.getAuthentication().get().isAuthenticated());
        System.out.println("URL: " + endpoint);

    }
}
