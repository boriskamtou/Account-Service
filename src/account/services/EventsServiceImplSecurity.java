package account.services;

import account.entities.SecurityEvent;
import account.repositories.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventsServiceImplSecurity implements SecurityEventService {
    private final EventsRepository eventsRepository;

    @Autowired
    public EventsServiceImplSecurity(EventsRepository eventsRepository) {
        this.eventsRepository = eventsRepository;
    }

    @Override
    public List<SecurityEvent> getAllEvents() {
        return eventsRepository.findAll();
    }

    @Override
    public void saveEvent(String action, String subject, String object, String path) {
        SecurityEvent event = new SecurityEvent(String.valueOf(LocalDateTime.now()), action, subject, object, path);
        eventsRepository.save(event);
        System.out.println("EVENT SAVED: " + action);
    }

}
