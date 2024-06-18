package account.services;

import account.adapters.UserAdapter;
import account.entities.Users;
import account.exceptions.BreachedPasswordException;
import account.exceptions.PasswordLengthException;
import account.exceptions.PasswordMatchesException;
import account.exceptions.UnauthorizedException;
import account.repositories.AuthRepository;
import account.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public AuthServiceImpl(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Users signup(Users users) {
        final Users user = authRepository.findByEmail(users.getEmail().toLowerCase());
        if (user != null) {
            System.out.println("User already exist!");
            return null;
        }

        Users userToSave = new Users();
        userToSave.setName(users.getName());
        userToSave.setLastname(users.getLastname());
        userToSave.setEmail(users.getEmail().toLowerCase());

        System.out.println("User saved!");
        return authRepository.save(userToSave);
    }

    @Override
    public void changePassword(String newPassword) {
        UserAdapter currentUser = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.getUsername() == null) {
            throw new UnauthorizedException();
        }
        Users foundedUser = authRepository.findByEmail(currentUser.getUsername());
        final String currentPassword = foundedUser.getPassword();
        if (passwordEncoder.matches(newPassword, currentPassword)) {
            throw new PasswordMatchesException();
        } else if (newPassword.length() < 12) {
            throw new PasswordLengthException();
        } else if (Utils.breachedPassword.contains(newPassword)) {
            throw new BreachedPasswordException();
        } else {
            foundedUser.setPassword(passwordEncoder.encode(newPassword));
            authRepository.save(foundedUser);
        }
    }
}
