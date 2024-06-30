package account.services;

import account.entities.Users;
import account.exceptions.UserNotActiveException;
import account.repositories.AuthRepository;
import account.adapters.UserAdapter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class AppUserServiceImpl implements UserDetailsService {
    private final AuthRepository authRepository;

    @Autowired
    public AppUserServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Users user = authRepository.findUsersByEmail(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        if (!user.isActive()) {
            throw new LockedException("User account is locked");
        }

        return new UserAdapter(user);
    }

}
