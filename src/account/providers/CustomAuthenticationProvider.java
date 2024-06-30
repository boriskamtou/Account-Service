//package account.providers;
//
//import account.adapters.UserAdapter;
//import account.entities.Users;
//import account.repositories.AuthRepository;
//import account.services.SecurityEventService;
//import account.utils.Constants;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//@Service
//public class CustomAuthenticationProvider implements AuthenticationManager {
//
//    private final AuthRepository authRepository;
//    private final SecurityEventService securityEventService;
//    private final PasswordEncoder passwordEncoder;
//
//    private final Map<String, Integer> loginAttempts = new HashMap<>();
//
//    @Autowired
//    public CustomAuthenticationProvider(AuthRepository authRepository, SecurityEventService securityEventService, PasswordEncoder passwordEncoder) {
//        this.authRepository = authRepository;
//        this.securityEventService = securityEventService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String username = authentication.getName();
//        String password = authentication.getCredentials().toString();
//
//        Users user = authRepository.findUsersByEmail(username.toLowerCase());
//        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
//            incrementLoginAttempts(username);
//            throw new BadCredentialsException("Invalid username or password");
//        }
//        resetLoginAttempts(username);
//        return new UsernamePasswordAuthenticationToken(username, password, new UserAdapter(user).getAuthorities());
//    }
//
//    private void incrementLoginAttempts(String username) {
//        int attempts = loginAttempts.getOrDefault(username, 0);
//        attempts++;
//        loginAttempts.put(username, attempts);
//
//        if (attempts > 5) {
//            Users user = authRepository.findUsersByEmail(username.toLowerCase());
//            if (user != null) {
//                user.setActive(false);
//                authRepository.save(user);
//                securityEventService.saveEvent(Constants.EVENT_BRUTE_FORCE, username, username, "/api/auth/login");
//                securityEventService.saveEvent(Constants.EVENT_LOCK_USER, username, username, "/api/auth/login");
//            }
//        } else {
//            securityEventService.saveEvent(Constants.EVENT_LOGIN_FAILED, username, "Anonymous", "/api/auth/login");
//        }
//    }
//    private void resetLoginAttempts(String username) {
//        loginAttempts.remove(username);
//    }
//}
