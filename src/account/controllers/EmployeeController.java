package account.controllers;

import account.entities.Users;
import account.exceptions.UnauthorizedException;
import account.repositories.AuthRepository;
import account.services.EmplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping("api/empl")
public class EmployeeController {

    static final Logger LOGGER = Logger.getLogger("EmployeeController");

    @Autowired
    public EmployeeController(EmplService emplService) {
        this.emplService = emplService;
    }

    private final EmplService emplService;


    @GetMapping("/payment")
    public ResponseEntity<?> employeePayment() {
        final Users foundedUser = emplService.getUser();
        if (foundedUser != null) {
            System.out.println("Founded user: " + foundedUser);
            return ResponseEntity.ok(foundedUser);
        }
        LOGGER.info("USER NOT CONNECTED - UNAUTHORIZED");
        throw new UnauthorizedException();
    }
}
