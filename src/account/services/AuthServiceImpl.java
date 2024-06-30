package account.services;

import account.adapters.UserAdapter;
import account.entities.Group;
import account.entities.Users;
import account.exceptions.BreachedPasswordException;
import account.exceptions.PasswordLengthException;
import account.exceptions.PasswordMatchesException;
import account.exceptions.UnauthorizedException;
import account.repositories.AuthRepository;
import account.repositories.GroupRepository;
import account.utils.ChangePasswordSuccess;
import account.utils.Constants;
import account.utils.Utils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventService securityEventService;

    static final Logger LOGGER = Logger.getLogger("AuthServiceImpl");

    @Autowired
    public AuthServiceImpl(
            AuthRepository authRepository,
            GroupRepository groupRepository,
            PasswordEncoder passwordEncoder,
            SecurityEventService securityEventService) {
        this.authRepository = authRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityEventService = securityEventService;
    }

    @Override
    public Users signup(Users users) {
        final Users user = authRepository.findUsersByEmail(users.getEmail().toLowerCase());
        if (user != null) {
            LOGGER.info("USER EXIST WITH ID");
            return null;
        }
        Set<Group> groups = new HashSet<>();
        if (authRepository.count() == 0) {
            groups.add(groupRepository.findGroupByGroupName("ADMINISTRATOR").orElseThrow(() -> new RuntimeException("Role not found")));
        } else {
            groups.add(groupRepository.findGroupByGroupName("USER").orElseThrow(() -> new RuntimeException("Role not found")));
        }
        Users userToSave = new Users();
        userToSave.setName(users.getName());
        userToSave.setLastname(users.getLastname());
        userToSave.setEmail(users.getEmail().toLowerCase());
        userToSave.setPassword(passwordEncoder.encode(users.getPassword()));
        userToSave.setActive(true);
        userToSave.setGroups(groups);
        System.out.println("User saved!");
        securityEventService.saveEvent(Constants.EVENT_CREATE_USER, "Anonymous", users.getEmail().toLowerCase(), "/api/auth/register");
        return authRepository.save(userToSave);
    }

    @Override
    public ChangePasswordSuccess changePassword(String newPassword) {
        LOGGER.info("New password " + newPassword);
        UserAdapter currentUser = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users user = authRepository.findUsersByEmail(currentUser.getUsername().toLowerCase());
        if (user == null) {
            LOGGER.info("USER NOT AUTHORIZED !!!");
            throw new UnauthorizedException();
        }
        final String currentPassword = user.getPassword();
        LOGGER.info("Founded User: " + currentUser.getUsername());
        if (passwordEncoder.matches(newPassword, currentPassword)) {
            LOGGER.info("PASSWORD NOT MATCH");
            throw new PasswordMatchesException();
        } else if (newPassword.length() < 12) {
            LOGGER.info("PASSWORD BAD LENGTH");
            throw new PasswordLengthException();
        } else if (Utils.breachedPassword.contains(newPassword) || Utils.breachedPassword.contains(currentPassword)) {
            LOGGER.info("PASSWORD NOT SAVED");
            throw new BreachedPasswordException();
        } else {
            LOGGER.info("SAVED PASSWORD SUCCESS");
            user.setPassword(passwordEncoder.encode(newPassword));
            authRepository.save(user);
            securityEventService.saveEvent(Constants.EVENT_CHANGE_PASSWORD, user.getEmail(), user.getEmail(), "/api/auth/changepass");
            return new ChangePasswordSuccess(user.getEmail().toLowerCase(), "The password has been updated successfully");
        }
    }

    @Override
    public Users findUsersByEmail(String email) {
        return authRepository.findUsersByEmail(email);
    }

    @Override
    @Transactional
    public void increaseFailedAttempts(Users user) {
        try {
            int newFailAttempts = user.getFailedAttempt() + 1;
            user.setFailedAttempt(newFailAttempts);
            authRepository.updateFailedAttempts(newFailAttempts, user.getEmail().toLowerCase());
            System.out.println("Increase Failed Attempt: " + user.getFailedAttempt());
        } catch (Exception e) {
            System.out.println("An Error Occurred");
            e.printStackTrace();
        }


    }

    @Override
    @Transactional
    public void resetFailedAttempts(String email) {
        authRepository.updateFailedAttempts(0, email.toLowerCase());
    }
}
