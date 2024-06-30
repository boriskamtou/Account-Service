package account.controllers;

import account.services.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class EventsController {
    @Autowired
    public EventsController(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    private final SecurityEventService securityEventService;

    @GetMapping("/events/")
    public ResponseEntity<?> getEvents() {
        return ResponseEntity.ok(securityEventService.getAllEvents());
    }
}
