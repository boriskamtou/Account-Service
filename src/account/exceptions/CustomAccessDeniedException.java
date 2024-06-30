package account.exceptions;

import account.adapters.UserAdapter;
import account.services.SecurityEventService;
import account.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedException implements AccessDeniedHandler {
    private final SecurityEventService securityEventService;
    @Autowired
    public CustomAccessDeniedException(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String endpoint = request.getRequestURI();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", "Access Denied!");
        body.put("path", request.getRequestURI());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));

        securityEventService.saveEvent(Constants.EVENT_ACCESS_DENIED, getCurrentUsername(), endpoint, endpoint);
    }

    private String getCurrentUsername() {
        try {
            UserAdapter userAdapter = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userAdapter.getUsername();
            return userAdapter.getUsername().toLowerCase();
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}
