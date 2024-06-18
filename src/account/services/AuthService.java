package account.services;

import account.entities.Users;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {
    Users signup(Users users);
    void changePassword(String newPassword);
}
