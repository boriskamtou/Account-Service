package account.services;

import account.adapters.UserAdapter;
import account.entities.Users;
import account.repositories.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EmplServiceImpl implements EmplService {

    private final AuthRepository authRepository;

    @Autowired
    public EmplServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public Users getUser() {
        System.out.println("I Have been called");
        UserAdapter currentUser = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return authRepository.findByEmail(currentUser.getUsername().toLowerCase());
    }
}
