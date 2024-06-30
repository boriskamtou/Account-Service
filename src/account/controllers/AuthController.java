package account.controllers;

import account.entities.Users;
import account.exceptions.BadRequestException;
import account.exceptions.BreachedPasswordException;
import account.exceptions.PasswordLengthException;
import account.exceptions.UserExistException;
import account.services.AuthService;
import account.utils.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody Users users) {

        if (users.getPassword() == null) {
            System.out.println("BREACHED IS NULL");
            throw new PasswordLengthException();
        }
        if (users.getPassword().length() < 12) {
            System.out.println("BREACHED IS NOT GREATER THAN 12");
            throw new PasswordLengthException();
        }
        if (Utils.breachedPassword.contains(users.getPassword())) {
            System.out.println("BREACHED PASSWORD");
            throw new BreachedPasswordException();
        }

        final Users user = authService.signup(users);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            throw new UserExistException("User exist!");
        }
    }

    @PostMapping("/changepass")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String newPassword = body.get("new_password");
        return ResponseEntity.ok(authService.changePassword(newPassword));
    }
}
