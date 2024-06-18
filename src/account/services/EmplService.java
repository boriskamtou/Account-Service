package account.services;

import account.entities.Users;
import org.springframework.stereotype.Service;

@Service
public
interface  EmplService {
    Users getUser();
}
