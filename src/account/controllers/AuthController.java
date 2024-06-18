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
    public ResponseEntity<?> signup( @Valid @RequestBody Users users) {

        if (users.getPassword().length() < 12) {
            throw new PasswordLengthException();
        }
        if (Utils.breachedPassword.contains(users.getPassword())) {
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
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails user) {
        String newPassword = body.get("new_password");
        authService.changePassword(newPassword);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("email", user.getUsername());
        response.put("status", "The password has been updated successfully");
        return ResponseEntity.ok(response);
    }
}
