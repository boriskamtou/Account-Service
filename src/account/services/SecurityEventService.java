package account.services;

import account.entities.SecurityEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SecurityEventService {
    List<SecurityEvent> getAllEvents();
    void saveEvent(String action, String subject, String object, String path);
}
