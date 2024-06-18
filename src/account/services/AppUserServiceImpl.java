package account.services;

import account.repositories.AuthRepository;
import account.adapters.UserAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl implements UserDetailsService {
    private final AuthRepository authRepository;

    @Autowired
    public AppUserServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserAdapter(authRepository.findByEmail(username.toLowerCase()));
    }
}
