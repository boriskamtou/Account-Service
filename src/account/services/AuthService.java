package account.services;

import account.entities.Users;
import account.utils.ChangePasswordSuccess;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    Users signup(Users users);
    ChangePasswordSuccess changePassword(String newPassword);

    Users findUsersByEmail(String email);
    void increaseFailedAttempts(Users user);
    void resetFailedAttempts(String email);
}
